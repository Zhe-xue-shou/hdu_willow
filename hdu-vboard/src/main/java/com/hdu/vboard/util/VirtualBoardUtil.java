package com.hdu.vboard.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hdu.vboard.entity.bo.SimulationWorkerBO;
import com.hdu.vboard.websocket.SimulationWebSocketHandler;
import com.hdu.vboard.websocket.WebSocketPushService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.BufferedWriter;

@Slf4j
public class VirtualBoardUtil {
  public static void sendSignalToVirtualBoard(BufferedWriter simInput, String signal_json) throws Exception {
    if (simInput == null) {
      throw new Exception("simInput is null");
    }
    simInput.write(signal_json + "\n");
    simInput.flush();
  }

  public static JSONObject getSignalFromVirtualBoard(BufferedReader simOutput) throws Exception {
    String line;
    if (simOutput == null) {
      throw new Exception("simOutput is null");
    }
    if ((line = simOutput.readLine()) != null) {
      JSONObject signalData = JSONUtil.parseObj(line);
      JSONObject outputJson = new JSONObject();
      outputJson.set("data", signalData);
      return outputJson;
    } else {
      return null;
    }
  }
}
