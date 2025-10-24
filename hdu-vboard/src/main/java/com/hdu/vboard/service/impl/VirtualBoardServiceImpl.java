package com.hdu.vboard.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.hdu.hdufpga.util.RedisUtil;
import com.hdu.vboard.entity.bo.SimulationWorkerBO;
import com.hdu.vboard.entity.constant.VbRedisConstant;
import com.hdu.vboard.exception.CreateWorkbenchException;
import com.hdu.vboard.exception.MakeWorkbenchException;
import com.hdu.vboard.service.VirtualBoardService;
import com.hdu.vboard.util.VbSysFileUtil;
import com.hdu.vboard.util.VirtualBoardUtil;
import com.hdu.svccmn.service.UserStatisticService;
import com.hdu.vboard.websocket.WebSocketPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
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
  WebSocketPushService webSocketPushService;

  @Value("${script.pyPath}")
  private String pyPath;

  @Value("${script.filePath}")
  private String scriptPath;

  @Value("${verilator.path}")
  private String verilatorPath;

  @Override
  public JSONObject getWorkerStatus() {
    JSONObject returnJsonObj = new JSONObject();
    // 用来装所有 worker 状态的数组
    List<JSONObject> stateList = new ArrayList<>();

    simulationWorkers.forEach((token, worker) -> {
      JSONObject item = new JSONObject();
      item.set("token", token);
      item.set("state", worker.getState());
      stateList.add(item);
    });

    returnJsonObj.set("states", stateList);
    return returnJsonObj;
  }

  @Override
  public Boolean createWorkbench(String workspaceName, List<String> verilogFullPaths, String bindFullPath) throws Exception {
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
    command.add(workspaceName);

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
      FileUtil.del(VbSysFileUtil.getFullWorkbenchPath(workspaceName));
      throw new CreateWorkbenchException(errMsg.toString());
    }
    log.info("create simulation workbench success for token:{}", workspaceName);
    redisUtil.set(VbRedisConstant.REDIS_VB_TTL_PREFIX + workspaceName, true, VbRedisConstant.REDIS_VB_TTL_LIMIT, TimeUnit.SECONDS);

    return true;
  }

  @Override
  public Boolean checkWorkbench(String workspaceName) throws Exception {
    ProcessBuilder runBuilder = new ProcessBuilder("make");
    if (!FileUtil.exist(VbSysFileUtil.getFullWorkbenchPath(workspaceName))) {
      throw new CreateWorkbenchException("workbench does not exist");
    }

    runBuilder.directory(new File(VbSysFileUtil.getFullWorkbenchPath(workspaceName)));
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
    redisUtil.set(VbRedisConstant.REDIS_VB_TTL_PREFIX + workspaceName, true, VbRedisConstant.REDIS_VB_TTL_LIMIT, TimeUnit.SECONDS);

    return true;
  }

  @Override
  public JSONObject runWorkbench(String workspaceName) throws Exception {
    if (!redisUtil.hasKey(VbRedisConstant.REDIS_VB_TTL_PREFIX + workspaceName)) {
      throw new Exception("Connection time out! Workspace has been cleared!");
    }
    ProcessBuilder runBuilder = new ProcessBuilder("make", "run");
    String workbenchPath = VbSysFileUtil.getFullWorkbenchPath(workspaceName);
    if (!FileUtil.exist(workbenchPath)) {
      throw new CreateWorkbenchException("workbench does not exist");
    }

    runBuilder.directory(new File(workbenchPath));
    runBuilder.redirectErrorStream(true);

    Process simProcess = runBuilder.start();

    BufferedWriter simInput = new BufferedWriter(new OutputStreamWriter(simProcess.getOutputStream()));
    BufferedReader simOutput = new BufferedReader(new InputStreamReader(simProcess.getInputStream()));

    // 获取已存在的 worker
    SimulationWorkerBO oldWorker = simulationWorkers.get(workspaceName);
    if (oldWorker != null) {
      log.info("Find old simulation worker for token:{}", workspaceName);
      try {
        stopWorkbench(workspaceName);
        log.info("Stop old simulation worker for token:{}", workspaceName);
      } catch (Exception e) {
        log.error("Error to stop simulation worker for token:{}", workspaceName);
      } finally {
        // 无论成功失败，都移除旧 worker 避免残留
        simulationWorkers.remove(workspaceName);
      }
    }

    SimulationWorkerBO simulationWorkerBO =
        new SimulationWorkerBO(workspaceName, simProcess, simInput, simOutput, null, true);
    log.info("Simulation process started for token: {}", workspaceName);
    simulationWorkers.put(workspaceName, simulationWorkerBO);
    final JSONObject finalJsonObj = getSignalFromVirtualBoard(workspaceName);
    JSONObject firstState = updateState(workspaceName, finalJsonObj);
    log.debug("first state:{}", firstState);
    redisUtil.set(
        VbRedisConstant.REDIS_VB_TTL_PREFIX + workspaceName,
        true,
        VbRedisConstant.REDIS_VB_TTL_LIMIT,
        TimeUnit.SECONDS
    );
    return finalJsonObj;
  }

  // update and broadcast
  private JSONObject updateState(String workspaceName, JSONObject jsonObj) {
    if (jsonObj != null && jsonObj.getJSONObject("data") != null) {
      log.debug("final json:{}", jsonObj);
      log.debug("final json[data]:{}", jsonObj.getJSONObject("data"));
      JSONObject state = jsonObj.getJSONObject("data");
      SimulationWorkerBO targetWorker;
      if ((targetWorker = simulationWorkers.get(workspaceName)) != null) {
        targetWorker.setState(state);
        JSONObject broadcastJsonObj = new JSONObject();
        broadcastJsonObj.set("token", workspaceName);
        broadcastJsonObj.set("state", targetWorker.getState());
        webSocketPushService.broadcast(broadcastJsonObj.toString());
        return broadcastJsonObj;
      }
    }
    return null;
  }

  @Override
  public Boolean sendSignal(String workspaceName, String signal) throws Exception {
    if (!redisUtil.hasKey(VbRedisConstant.REDIS_VB_TTL_PREFIX + workspaceName)) {
      throw new Exception("Connection time out! Workspace has been cleared!");
    }
    SimulationWorkerBO simulationWorkerBO = simulationWorkers.get(workspaceName);
    if (simulationWorkerBO == null) {
      throw new MakeWorkbenchException("simulation workbench does not exist");
    }
    VirtualBoardUtil.sendSignalToVirtualBoard(simulationWorkerBO.simInput, signal);
    redisUtil.set(VbRedisConstant.REDIS_VB_TTL_PREFIX + workspaceName, true, VbRedisConstant.REDIS_VB_TTL_LIMIT, TimeUnit.SECONDS);

    return true;
  }

  @Override
  public JSONObject getSignalFromVirtualBoard(String workspaceName) throws Exception {
    SimulationWorkerBO simulationWorkerBO = simulationWorkers.get(workspaceName);
    if (simulationWorkerBO == null) {
      throw new MakeWorkbenchException("simulation workbench does not exist");
    }
    redisUtil.set(VbRedisConstant.REDIS_VB_TTL_PREFIX + workspaceName, true, VbRedisConstant.REDIS_VB_TTL_LIMIT, TimeUnit.SECONDS);
    final JSONObject finalJsonObj = VirtualBoardUtil.getSignalFromVirtualBoard(simulationWorkerBO.simOutput);
    updateState(workspaceName, finalJsonObj);
    return finalJsonObj;
  }

  // 先清理文件，再停止线程，防止资源泄露
  @Override
  public Boolean stopWorkbench(String workspaceName) throws Exception {
    redisUtil.del(VbRedisConstant.REDIS_VB_TTL_PREFIX + workspaceName);
    clearWorkbench(workspaceName);
    SimulationWorkerBO simulationWorkerBO = simulationWorkers.remove(workspaceName);
    if (simulationWorkerBO == null) {
      throw new MakeWorkbenchException("simulation process:" + workspaceName + " does not exist");
    }
    simulationWorkerBO.running = false;
    simulationWorkerBO.simInput.write((char) -1);
    if (simulationWorkerBO.simulationProcess.isAlive()) {
      simulationWorkerBO.simulationProcess.destroy();
      log.info("simulation process:{} stopped!", workspaceName);
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
}
