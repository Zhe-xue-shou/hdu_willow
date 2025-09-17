package com.hdu.vboard.controller;

import com.hdu.hdufpga.annotation.CheckToken;
import com.hdu.hdufpga.entity.Result;
import com.hdu.hdufpga.entity.vo.UserVO;
import com.hdu.vboard.service.VbSysFileService;
import com.hdu.vboard.service.VbTokenService;
import com.hdu.vboard.service.VirtualBoardService;

import com.hdu.svccmn.service.UserStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import cn.hutool.json.JSONObject;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/vb")
@Slf4j
public class VirtualBoardController /*extends BaseController<VirtualBoardService,> */ {
  @Resource
  VirtualBoardService virtualBoardService;

  @Resource
  VbSysFileService vbSysFileService;

  @Resource
  private UserStatisticService userStatisticService;

  @Resource
  private VbTokenService vbTokenService;

//    @Override
//    @PostMapping("/listPage")
//    public Result listPage(Integer current, Integer size) {
//        return super.listPage(current, size);
//    }

  //level >= 3
//    @Override
//    @PostMapping("/get")
//    public Result get(Integer id) {
//        return super.get(id);
//    }

//    //    @Override
//    @PostMapping("/create")
//    public Result create(@RequestBody CircuitBoardPO circuitBoardPO) {
//        return Result.error("不支持本方法");
//    }
//
//    //    @Override
//    @PostMapping("/update")
//    public Result update(@RequestBody CircuitBoardPO circuitBoardPO) {
//        return Result.error("不支持本方法");
//    }
//
//    //    @Override
//    @PostMapping("/delete")
//    public Result delete(@RequestBody CircuitBoardPO circuitBoardPO) {
//        return Result.error("不支持本方法");
//    }

  @PostMapping("/build")
  @CheckToken
  public Result build(@RequestParam("verilogFile") MultipartFile[] verilogFiles,
                      @RequestParam("bindFile") MultipartFile bindFile,
                      HttpServletRequest request) {
    String workspaceName = request.getHeader("token");
    try {
      log.info("Hello token: {}", workspaceName);
      List<String> verilogFullPaths = new ArrayList<>();
      vbSysFileService.clearWorkbenchFile(request);
      for (MultipartFile verilogFile : verilogFiles) {
        String path = vbSysFileService.saveVerilogFile(request, verilogFile);
        verilogFullPaths.add(path);
      }
      String bindFullPath = vbSysFileService.saveBindFile(request, bindFile);
      log.debug("Now to create virtual board");
      virtualBoardService.createWorkbench(workspaceName, verilogFullPaths, bindFullPath);
      log.debug("Ok to create workbench, now to check it");
      virtualBoardService.checkWorkbench(workspaceName);
      return Result.ok("Ok to build the virtual board");
    } catch (Exception e) {
      try {
        virtualBoardService.clearWorkbench(workspaceName);
      } catch (Exception ex) {
        log.error(ex.getMessage());
      }
      log.error(e.getMessage());
      return Result.error(e.getMessage());
    }
  }

  @PostMapping("/start")
  @CheckToken
  public Result start(HttpServletRequest request) {
    String token = request.getHeader("token");
    try {
      virtualBoardService.runWorkbench(token);
      log.debug("Now it's running! token: {}", token);
      JSONObject finalJson = virtualBoardService.getSignalFromVirtualBoard(token);
      log.debug(finalJson.toString());
      virtualBoardService.clearWorkbench(token);
      log.debug("Clear the workbench files!");
      return Result.ok(finalJson);
    } catch (Exception e) {
      try {
        virtualBoardService.clearWorkbench(token);
      } catch (Exception ex) {
        log.error(ex.getMessage());
      }
      log.error(e.getMessage());
      return Result.error(e.getMessage());
    }
  }

  @PostMapping("/signal")
  @CheckToken
  public Result signal(HttpServletRequest request, @RequestBody JSONObject signalJson) {
    String token = request.getHeader("token");
    try {
      log.debug("Received input signal:{}", signalJson.toString());
      virtualBoardService.sendSignal(token, signalJson.get("data").toString());
      return Result.ok(virtualBoardService.getSignalFromVirtualBoard(token));
    } catch (Exception e) {
      try {
        virtualBoardService.clearWorkbench(token);
      } catch (Exception ex) {
        log.error(ex.getMessage());
      }
      log.error(e.getMessage());
      return Result.error(e.getMessage());
    }
  }

  //level >= 1
  @PostMapping("/finish")
  @CheckToken
  public Result finish(HttpServletRequest request) {
    String token = request.getHeader("token");
    try {
      return Result.ok(virtualBoardService.stopWorkbench(token));
    } catch (Exception e) {
      log.error(e.getMessage());
      return Result.error(e.getMessage());
    }
  }
}
