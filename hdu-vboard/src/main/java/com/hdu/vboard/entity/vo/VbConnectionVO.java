package com.hdu.vboard.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VbConnectionVO {
  private String userName;
  private String userIp;
  private String departmentName;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date buildTime;

//  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//  private Date updateTime;
}
