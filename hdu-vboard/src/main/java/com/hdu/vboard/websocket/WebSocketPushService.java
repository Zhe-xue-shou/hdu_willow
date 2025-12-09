package com.hdu.vboard.websocket;

import cn.hutool.json.JSONObject;
import com.hdu.vboard.event.WorkerStateEvent;
import com.hdu.vboard.service.VirtualBoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
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

  @EventListener
  public void broadcast(WorkerStateEvent event) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.set("token", event.getToken());
    switch (event.getType()) {
      case BUILD:
        jsonObject.set("type", 1);
        break;
      case CHANGED:
        jsonObject.set("type", 2);
        jsonObject.set("state", event.getState());
        break;
      case FINISH:
        jsonObject.set("type", 3);
        break;
    }
    String message = jsonObject.toString();
    log.debug(message);
    Collection<WebSocketSession> col = sessions.values();
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
    JSONObject message = virtualBoardService.getWorkerStatus().set("type",0);
    if (session != null && session.isOpen()) {
      try {
        session.sendMessage(new TextMessage(message.toString()));
        log.debug("firstStates:{}", message);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
  }
}
