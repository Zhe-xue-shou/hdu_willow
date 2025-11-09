package com.hdu.vboard.event.enums;

import cn.hutool.json.JSONObject;
import lombok.Getter;

@Getter
public enum WorkerStatesEventType {
  BUILD,
  CHANGED,
  FINISH,
}
