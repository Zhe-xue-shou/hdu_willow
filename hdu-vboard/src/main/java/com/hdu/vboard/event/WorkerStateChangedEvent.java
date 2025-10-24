package com.hdu.vboard.event;

import cn.hutool.json.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class WorkerStateChangedEvent extends ApplicationEvent {
  private final String token;
  private final JSONObject state;

  public WorkerStateChangedEvent(Object source, String token, JSONObject state) {
    super(source);
    this.token = token;
    this.state = state;
  }
}
