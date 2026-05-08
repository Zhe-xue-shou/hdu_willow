package com.hdu.hdufpga.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatisticDTO {
  private Long totActiveTime; // 总实验时间（毫秒）
  private Integer totExpCnt;  // 总实验次数
  private Long addActiveTime; // 增加的实验时间（毫秒）
  private Integer addExpCnt;  // 增加的实验次数（一般为1）
}
