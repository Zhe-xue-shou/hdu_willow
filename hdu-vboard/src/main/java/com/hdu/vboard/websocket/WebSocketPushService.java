package com.hdu.vboard.websocket;

import cn.hutool.json.JSONObject;
import com.hdu.vboard.service.VirtualBoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;

@Service
@Slf4j
public class WebSocketPushService {
  private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

  @Resource
  VirtualBoardService virtualBoardService;

  public void addSession(String id, WebSocketSession session) {
    sessions.put(id, session);
  }

  public void removeSession(String id) {
    sessions.remove(id);
  }

  public void broadcast(String message) {
    Collection<WebSocketSession> col = sessions.values();
    log.debug(message);
    for (WebSocketSession s : col) {
      if (s.isOpen()) {
        try {
          s.sendMessage(new TextMessage(message));
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
      }
    }
  }

  public void firstConnectSendStates(WebSocketSession session) {
    JSONObject message = virtualBoardService.getWorkerStatus();
    if (session != null && session.isOpen()) {
      try {
        session.sendMessage(new TextMessage(message.toString()));
        log.debug("firstStates:{}", message);
      } catch (IOException e) {
        log.debug(e.getMessage(), e);
      }
    }
  }
}
