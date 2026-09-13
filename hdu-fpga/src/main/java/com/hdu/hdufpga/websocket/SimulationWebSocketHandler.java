package com.hdu.hdufpga.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;

@Component
@Slf4j
public class SimulationWebSocketHandler extends TextWebSocketHandler {

  @Resource
  private WebSocketPushService pushService;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    String token = (String) session.getAttributes().get("token");
    String longId = pushService.addSession(token, session);
    log.info("[WebSocket] 连接建立: {}", session.getId());
    pushService.firstConnectSendStates(longId);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    String token = (String) session.getAttributes().get("token");
    pushService.removeSession(token);
    log.info("[WebSocket] 连接关闭: {}", session.getId());
  }
}
