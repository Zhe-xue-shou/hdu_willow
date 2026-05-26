package com.hdu.hdufpga.controller;

import cn.hutool.core.lang.Validator;
import com.hdu.hdufpga.entity.constant.CircuitBoardConstant;
import com.hdu.hdufpga.netty.NettySocketHolder;
import com.hdu.hdufpga.service.CircuitBoardService;
import com.hdu.hdufpga.utils.CircuitBoardUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
  @Resource
  CircuitBoardService circuitBoardService;

  /**
   * 记录 bit 文件到电路板
   * GET /test/recordBit?longId=1001&filePath=/path/to/top.bit&timeout=0
   */
  @GetMapping("/recordBit")
  public Map<String, Object> recordBitToCB(
      @RequestParam String longId,
      @RequestParam String filePath,
      @RequestParam(defaultValue = "0") int timeout) {

    Map<String, Object> result = new HashMap<>();
    try {
      if (Validator.isNotNull(longId) && !longId.isEmpty()) {
        // 获取缓存中对应的ChannelHandlerContext
        ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
        HashMap<String, Object> info = NettySocketHolder.getInfo(longId);
        // 配置缓存信息
        info.put(CircuitBoardConstant.IS_RECORDED, false);
        info.put(CircuitBoardConstant.COUNT, 0);
        info.put(CircuitBoardConstant.FILE_PATH, filePath);
        // 保存缓存信息
        NettySocketHolder.put(longId, info);
        log.info("instance: {}", NettySocketHolder.getInfo(longId));
        // 烧录板卡
        CircuitBoardUtil.recordBitToCB(ctx, filePath, longId, 0);
        result.put("success", true);
        result.put("message", "bit 文件发送成功");
        result.put("longId", longId);
        result.put("filePath", filePath);
      }
    } catch (Exception e) {
      log.error("recordBitToCB 执行失败", e);
      result.put("success", false);
      result.put("message", "执行失败: " + e.getMessage());
      result.put("error", e.getClass().getSimpleName());
    }
    return result;
  }

  /**
   * 发送结束信号到电路板
   * GET /test/sendEnd?longId=1001
   */
  @GetMapping("/sendEnd")
  public Map<String, Object> sendEndToCB(@RequestParam String longId) {
    Map<String, Object> result = new HashMap<>();
    try {
      ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
      if (ctx == null) {
        result.put("success", false);
        result.put("message", "未找到 longId 对应的连接: " + longId);
        return result;
      }

      CircuitBoardUtil.sendEndToCB(ctx, longId);
      result.put("success", true);
      result.put("message", "结束信号发送成功");
      result.put("longId", longId);
    } catch (Exception e) {
      log.error("sendEndToCB 执行失败", e);
      result.put("success", false);
      result.put("message", "执行失败: " + e.getMessage());
    }
    return result;
  }

  /**
   * 发送按钮状态到电路板
   * GET /test/sendButton?longId=1001&switchStatus=10100101101001010101101001011010&tapStatus=000000
   */
  @GetMapping("/sendButton")
  public Map<String, Object> sendButtonToCB(
      @RequestParam String longId,
      @RequestParam String switchStatus,
      @RequestParam String tapStatus) {

    Map<String, Object> result = new HashMap<>();
    try {
      ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
      if (ctx == null) {
        result.put("success", false);
        result.put("message", "未找到 longId 对应的连接: " + longId);
        return result;
      }

      String finalString = CircuitBoardUtil.processButtonString(switchStatus, tapStatus);
      CircuitBoardUtil.sendButtonStringToCB(ctx, finalString);

      result.put("success", true);
      result.put("message", "按钮状态发送成功");
      result.put("longId", longId);
      result.put("switchStatus", switchStatus);
      result.put("tapStatus", tapStatus);
      result.put("processedString", finalString);
    } catch (Exception e) {
      log.error("sendButtonToCB 执行失败", e);
      result.put("success", false);
      result.put("message", "执行失败: " + e.getMessage());
    }
    return result;
  }

  /**
   * 获取电路板状态信息
   * GET /test/getStatus?longId=1001
   */
  @GetMapping("/getStatus")
  public Map<String, Object> getCircuitBoardStatus(@RequestParam String longId) {
    Map<String, Object> result = new HashMap<>();
    try {
      String lightString = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.LIGHT_STATUS);
      String nixieTubeString = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.NIXIE_TUBE_STATUS);
      String buttonString = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.BUTTON_STATUS);

      result.put("success", true);
      result.put("longId", longId);
      result.put("lightStatus", lightString != null ? lightString : "无数据");
      result.put("nixieTubeStatus", nixieTubeString != null ? nixieTubeString : "无数据");
      result.put("buttonStatus", buttonString != null ? buttonString : "无数据");

      log.info("获取状态 - longId: {}, light: {}, nixieTube: {}, button: {}",
          longId, lightString, nixieTubeString, buttonString);
    } catch (Exception e) {
      log.error("getCircuitBoardStatus 执行失败", e);
      result.put("success", false);
      result.put("message", "执行失败: " + e.getMessage());
    }
    return result;
  }

  /**
   * 批量执行所有测试
   * GET /test/runAll?longId=1001&filePath=/home/zxs/Desktop/test_fpga/target/top.bit
   */
  @GetMapping("/runAll")
  public Map<String, Object> runAllTests(
      @RequestParam String longId,
      @RequestParam String filePath) {

    Map<String, Object> result = new HashMap<>();
    Map<String, Object> testResults = new HashMap<>();

    try {
      ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
      if (ctx == null) {
        result.put("success", false);
        result.put("message", "未找到 longId 对应的连接: " + longId);
        return result;
      }

      // 1. 测试 recordBitToCB
      try {
        CircuitBoardUtil.recordBitToCB(ctx, filePath, longId, 0);
        testResults.put("recordBit", "成功");
      } catch (Exception e) {
        testResults.put("recordBit", "失败: " + e.getMessage());
      }

      // 2. 测试 getLightString
      try {
        String lightString = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.LIGHT_STATUS);
        String nixieTubeString = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.NIXIE_TUBE_STATUS);
        String buttonString = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.BUTTON_STATUS);
        testResults.put("getStatus", Map.of(
            "lightStatus", lightString != null ? lightString : "无数据",
            "nixieTubeStatus", nixieTubeString != null ? nixieTubeString : "无数据",
            "buttonStatus", buttonString != null ? buttonString : "无数据"
        ));
      } catch (Exception e) {
        testResults.put("getStatus", "失败: " + e.getMessage());
      }

      // 3. 测试 sendEnd
      try {
        CircuitBoardUtil.sendEndToCB(ctx, longId);
        testResults.put("sendEnd", "成功");
      } catch (Exception e) {
        testResults.put("sendEnd", "失败: " + e.getMessage());
      }

      result.put("success", true);
      result.put("longId", longId);
      result.put("testResults", testResults);

    } catch (Exception e) {
      log.error("runAllTests 执行失败", e);
      result.put("success", false);
      result.put("message", "执行失败: " + e.getMessage());
    }
    return result;
  }

  @GetMapping("/hello")
  public String hello() {
    ChannelHandlerContext ctx = NettySocketHolder.getCtx("1001");
    ctx.writeAndFlush("Hello, world!\n");
    return "Hello, world!";
  }
}