package com.hdu.vboard.event;

import cn.hutool.json.JSONObject;
import com.hdu.vboard.event.enums.WorkerStatesEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
public class WorkerStateEvent extends ApplicationEvent {
  private WorkerStatesEventType type;
  private final String token;
  private final JSONObject state;

  public WorkerStateEvent(Object source, WorkerStatesEventType type, String token, JSONObject state) {
    super(source);
    this.type = type;
    this.token = token;
    this.state = state;
  }
}
