package com.hdu.vboard.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.hdu.hdufpga.util.RedisUtil;
import com.hdu.hdufpga.util.TimeUtil;
import com.hdu.vboard.entity.bo.SimulationWorkerBO;
import com.hdu.vboard.entity.constant.VbRedisConstant;
import com.hdu.vboard.entity.vo.VbConnectionVO;
import com.hdu.vboard.event.WorkerStateChangedEvent;
import com.hdu.vboard.exception.CreateWorkbenchException;
import com.hdu.vboard.exception.MakeWorkbenchException;
import com.hdu.vboard.service.VbUseRecordService;
import com.hdu.vboard.service.VirtualBoardService;
import com.hdu.vboard.util.VbSysFileUtil;
import com.hdu.vboard.util.VirtualBoardUtil;
import com.hdu.svccmn.service.UserStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VirtualBoardServiceImpl implements VirtualBoardService {
  final ConcurrentHashMap<String, SimulationWorkerBO> simulationWorkers = new ConcurrentHashMap<>();

  @Resource
  RedisUtil redisUtil;

  @Resource
  UserStatisticService userStatisticService;

  @Resource
  ApplicationEventPublisher applicationEventPublisher;

  @Resource
  VbUseRecordService vbUseRecordService;

  @Resource
  HttpServletRequest request;

  @Value("${script.pyPath}")
  private String pyPath;

  @Value("${script.filePath}")
  private String scriptPath;

  @Value("${verilator.path}")
  private String verilatorPath;
  @Autowired
  private ConversionService conversionService;

  @Override
  public JSONObject getWorkerStatus() {
    JSONObject returnJsonObj = new JSONObject();
    // 用来装所有 worker 状态的数组
    List<JSONObject> stateList = new ArrayList<>();

    simulationWorkers.forEach((vid, worker) -> {
      JSONObject item = new JSONObject();
      item.set("token", vid);
      item.set("state", worker.getState());
      stateList.add(item);
    });

    returnJsonObj.set("states", stateList);
    return returnJsonObj;
  }

  @Override
  public Boolean createWorkbench(String token, List<String> verilogFullPaths, String bindFullPath) throws Exception {
    for (String verilogFullPath : verilogFullPaths) {
      if (!FileUtil.exist(verilogFullPath)) {
        throw new CreateWorkbenchException("verilog file does not exist");
      }
    }
    if (!FileUtil.exist(bindFullPath)) {
      throw new CreateWorkbenchException("bind file does not exist");
    }

    File workbenchDir = new File(VbSysFileUtil.getFullWorkbenchPath(""));

    if (!workbenchDir.exists()) {
      boolean created = workbenchDir.mkdirs();  // 递归创建目录
      if (!created) {
        throw new RuntimeException("cannot create dirctory: " + workbenchDir.getAbsolutePath());
      } else {
        log.info("dirctory:{} create success", workbenchDir);
      }
    }

    // 脚本路径
    String scriptFullPath = VbSysFileUtil.getRootBasePath() + scriptPath;

    List<String> command = new ArrayList<>();
    command.add(pyPath);
    command.add(scriptFullPath);

    command.add("--workspace-name");
    command.add(token);

    command.add("--verilog-files");
    command.addAll(verilogFullPaths);

    command.add("--bind-json");
    command.add(bindFullPath);

    command.add("--top-module");
    command.add("top");

    command.add("--verilator-path");
    command.add(verilatorPath);

    log.debug("python command:\n{}", command);

    ProcessBuilder builder = new ProcessBuilder(command);
    builder.directory(workbenchDir);
    builder.redirectErrorStream(true);

    Process createProcess = builder.start();

    BufferedReader reader = new BufferedReader(new InputStreamReader(createProcess.getInputStream()));

    int exitCode = createProcess.waitFor();
    if (exitCode != 0) {
      StringBuilder errMsg = new StringBuilder("Error creating simulation workspace, clear!\n");
      String line;
      while ((line = reader.readLine()) != null) {
        errMsg.append(line).append("\n");
      }
      FileUtil.del(VbSysFileUtil.getFullWorkbenchPath(token));
      throw new CreateWorkbenchException(errMsg.toString());
    }
    log.info("create simulation workbench success for token:{}", token);
    redisUtil.set(VbRedisConstant.REDIS_VB_TTL_PREFIX + token, true, VbRedisConstant.REDIS_VB_TTL_LIMIT, TimeUnit.SECONDS);

    return true;
  }

  @Override
  public Boolean checkWorkbench(String token) throws Exception {
    ProcessBuilder runBuilder = new ProcessBuilder("make");
    if (!FileUtil.exist(VbSysFileUtil.getFullWorkbenchPath(token))) {
      throw new CreateWorkbenchException("workbench does not exist");
    }

    runBuilder.directory(new File(VbSysFileUtil.getFullWorkbenchPath(token)));
    runBuilder.redirectErrorStream(true);

    Process process = runBuilder.start();
//    logProcessService.logProcess(process);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

    int exitCode = process.waitFor();
    if (exitCode != 0) {
      StringBuilder errMsg = new StringBuilder("Error making workbench,clear!\n");
      String line;
      while ((line = reader.readLine()) != null) {
        errMsg.append(line).append("\n");
      }

      throw new MakeWorkbenchException(errMsg.toString());
    }
    redisUtil.set(VbRedisConstant.REDIS_VB_TTL_PREFIX + token, true, VbRedisConstant.REDIS_VB_TTL_LIMIT, TimeUnit.SECONDS);

    return true;
  }

  @Override
  public JSONObject runWorkbench(String token) throws Exception {
    if (!redisUtil.hasKey(VbRedisConstant.REDIS_VB_TTL_PREFIX + token)) {
      throw new Exception("Connection time out! Workspace has been cleared!");
    }
    ProcessBuilder runBuilder = new ProcessBuilder("make", "run");
    String workbenchPath = VbSysFileUtil.getFullWorkbenchPath(token);
    if (!FileUtil.exist(workbenchPath)) {
      throw new CreateWorkbenchException("workbench does not exist");
    }

    runBuilder.directory(new File(workbenchPath));
    runBuilder.redirectErrorStream(true);

    Process simProcess = runBuilder.start();

    BufferedWriter simInput = new BufferedWriter(new OutputStreamWriter(simProcess.getOutputStream()));
    BufferedReader simOutput = new BufferedReader(new InputStreamReader(simProcess.getInputStream()));

    // 获取已存在的 worker
    SimulationWorkerBO oldWorker = simulationWorkers.get(token);
    if (oldWorker != null) {
      log.info("Find old simulation worker for token:{}", token);
      try {
        stopWorkbench(token, 2);
        log.info("Stop old simulation worker for token:{}", token);
      } catch (Exception e) {
        log.error("Error to stop simulation worker for token:{}", token);
      } finally {
        // 无论成功失败，都移除旧 worker 避免残留
        simulationWorkers.remove(token);
      }
    }

    SimulationWorkerBO simulationWorkerBO =
        new SimulationWorkerBO(token, simProcess, simInput, simOutput, null, true);
    log.info("Simulation process started for token: {}", token);

    VbConnectionVO vbConnectionVO = createVbConnextionVO(token);
    log.debug("vbConnectionVO:{}", vbConnectionVO);
    simulationWorkers.put(vbConnectionVO.getVid(), simulationWorkerBO);

    redisUtil.set(VbRedisConstant.REDIS_VB_CONN_PREFIX + token, vbConnectionVO, 12, TimeUnit.HOURS);
    redisUtil.set(
        VbRedisConstant.REDIS_VB_TTL_PREFIX + token,
        true,
        VbRedisConstant.REDIS_VB_TTL_LIMIT,
        TimeUnit.SECONDS
    );

//    log.debug("{} -> workerBO", vbConnectionVO.getVid());
    final JSONObject finalJsonObj = getSignalFromVirtualBoard(token);
    JSONObject firstState = updateState(token, finalJsonObj);
    log.debug("first state:{}", firstState);


//    Object a = redisUtil.get(VbRedisConstant.REDIS_VB_CONN_PREFIX);
//    log.debug("raw object:{}", a);
//    VbConnectionVO vbConnectionVO1 = Convert.convert(VbConnectionVO.class, a);
//    log.debug("after convert:{}", vbConnectionVO1);

    return finalJsonObj;
  }

  // update and broadcast
  private JSONObject updateState(String token, JSONObject jsonObj) throws Exception {
    if (jsonObj != null && jsonObj.getJSONObject("data") != null) {
      log.debug("final json:{}", jsonObj);
      log.debug("final json[data]:{}", jsonObj.getJSONObject("data"));
      JSONObject state = jsonObj.getJSONObject("data");
      log.debug("token:{}", token);
      String Vid = getVid(token);
      SimulationWorkerBO targetWorker;
      if ((targetWorker = simulationWorkers.get(Vid)) != null) {
        targetWorker.setState(state); // 更新map中的worker状态
        applicationEventPublisher.publishEvent(new WorkerStateChangedEvent(this, token, state));
        return state;
      }
    }
    return null;
  }

  @Override
  public Boolean sendSignal(String token, String signal) throws Exception {
    if (!redisUtil.hasKey(VbRedisConstant.REDIS_VB_TTL_PREFIX + token)) {
      throw new Exception("Connection time out! Workspace has been cleared!");
    }
    log.debug("token:{}", token);
    String vid = getVid(token);
    SimulationWorkerBO simulationWorkerBO = simulationWorkers.get(vid);
    if (simulationWorkerBO == null) {
      throw new MakeWorkbenchException("simulation workbench does not exist");
    }
    VirtualBoardUtil.sendSignalToVirtualBoard(simulationWorkerBO.simInput, signal);
    redisUtil.set(VbRedisConstant.REDIS_VB_TTL_PREFIX + token, true, VbRedisConstant.REDIS_VB_TTL_LIMIT, TimeUnit.SECONDS);

    return true;
  }

  @Override
  public JSONObject getSignalFromVirtualBoard(String token) throws Exception {
    log.debug("token:{}", token);
    String vid = getVid(token);
    SimulationWorkerBO simulationWorkerBO = simulationWorkers.get(vid);
    if (simulationWorkerBO == null) {
      throw new MakeWorkbenchException("simulation workbench does not exist");
    }
    redisUtil.set(VbRedisConstant.REDIS_VB_TTL_PREFIX + token, true, VbRedisConstant.REDIS_VB_TTL_LIMIT, TimeUnit.SECONDS);
    final JSONObject finalJsonObj = VirtualBoardUtil.getSignalFromVirtualBoard(simulationWorkerBO.simOutput);
    updateState(token, finalJsonObj);
    return finalJsonObj;
  }

  // 先清理文件，再停止线程，防止资源泄露
  @Override
  public Boolean stopWorkbench(String token, int status) throws Exception {
    redisUtil.del(VbRedisConstant.REDIS_VB_TTL_PREFIX + token);
    VbConnectionVO vbConnectionVO = Convert.convert(VbConnectionVO.class, redisUtil.get(VbRedisConstant.REDIS_VB_CONN_PREFIX + token));
    clearWorkbench(token);
    SimulationWorkerBO simulationWorkerBO = simulationWorkers.remove(vbConnectionVO.getVid());
    if (simulationWorkerBO == null) {
      throw new MakeWorkbenchException("simulation process:" + token + " does not exist");
    }
    simulationWorkerBO.running = false;
    simulationWorkerBO.simInput.write((char) -1);
    if (simulationWorkerBO.simulationProcess.isAlive()) {
      simulationWorkerBO.simulationProcess.destroy();
      log.info("simulation process:{} stopped!", token);
    }
    redisUtil.del(VbRedisConstant.REDIS_VB_CONN_PREFIX + token);
    log.debug("vb_connection of token: {} in redis has successfully deleted!", token);
    if (!vbUseRecordService.saveVbRecord(vbConnectionVO, status)) {
      throw new SQLException("save user vb use record error token: {}", token);
    } else {
      log.debug("Successfully insert vb record into t_vb_use_record");
    }
    return true;
  }

  // 单纯清理工作区文件
  @Override
  public Boolean clearWorkbench(String workspaceName) {
    String workbenchFullPath = VbSysFileUtil.getFullWorkbenchPath(workspaceName);
    // 不存在可能是被提前清理，不算error
    if (!FileUtil.exist(workbenchFullPath)) {
      log.warn("workbench:{} does not exist,maybe has been cleared already.", workspaceName);
      return false;
    }
    VbSysFileUtil.deleteDirectory(new File(workbenchFullPath));
    log.info("workbench:{} cleared!", workspaceName);
    return true;
  }

  public VbConnectionVO createVbConnextionVO(String token) {
    VbConnectionVO vbConnectionVO = new VbConnectionVO();
    String[] info = token.split("_");
    vbConnectionVO.setUserIp(request.getLocalAddr());
    vbConnectionVO.setUserName(info[0]);
    vbConnectionVO.setDepartmentName(info[1]);
    vbConnectionVO.setBuildTime(TimeUtil.getNowTime());
    vbConnectionVO.setVid(IdUtil.simpleUUID());
    return vbConnectionVO;
  }

  private String getVid(String token) throws Exception {
    VbConnectionVO vbConnectionVO = Convert.convert(VbConnectionVO.class, redisUtil.get(VbRedisConstant.REDIS_VB_CONN_PREFIX + token));
    if (vbConnectionVO == null) {
      throw new Exception("VbConnectionVO is not existed!");
    }
    return vbConnectionVO.getVid();
  }
}
