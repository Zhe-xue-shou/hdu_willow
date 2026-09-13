package com.hdu.hdufpga.websocket;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONObject;
import com.hdu.hdufpga.entity.constant.CircuitBoardConstant;
import com.hdu.hdufpga.entity.constant.RedisConstant;
import com.hdu.hdufpga.entity.vo.UserConnectionVO;
import com.hdu.hdufpga.netty.NettySocketHolder;
import com.hdu.hdufpga.service.CircuitBoardService;
import com.hdu.hdufpga.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WebSocketPushService {
  private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

  @Resource
  RedisUtil redisUtil;

  @Resource
  CircuitBoardService circuitBoardService;

  public void firstConnectSendStates(String longId) {
    pushEvent(longId);
  }

  public String addSession(String token, WebSocketSession session) {
    UserConnectionVO userConnectionVO = Convert.convert(UserConnectionVO.class,
        redisUtil.get(RedisConstant.REDIS_CONN_PREFIX + token));
    String longId = userConnectionVO.getLongId();
    sessions.put(longId, session);
    return longId;
  }

  public void removeSession(String token) {
    UserConnectionVO userConnectionVO = Convert.convert(UserConnectionVO.class,
        redisUtil.get(RedisConstant.REDIS_CONN_PREFIX + token));
    String longId = userConnectionVO.getLongId();
    sessions.remove(longId);
  }

  public void pushEvent(String longId) {
    var session = sessions.get(longId);
    try {
      var lightString = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.LIGHT_STATUS);
      var processedBtnStr = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.BUTTON_STATUS);
      var isRecorded = (Boolean) NettySocketHolder.getValue(longId, CircuitBoardConstant.IS_RECORDED);
      var finalJson = new JSONObject();
      finalJson.set("light", lightString);
      finalJson.set("btn", processedBtnStr);
      finalJson.set("isRecorded", isRecorded);
      var message = finalJson.toString();
      log.info("token:{} send:{}", longId, message);
      session.sendMessage(new TextMessage(message));
    } catch (Exception e) {
      log.error("{}", e);
      throw new RuntimeException(e);
    }

  }
}
