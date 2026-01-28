package com.hdu.vboard.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hdu.hdufpga.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("t_vb_use_record")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class VbUseRecordPO extends BaseEntity {
  private String userName;

  private String userIp;

  @TableField("school_name")
  private String departmentName;

  private Integer duration;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date buildTime;

  private Integer status;
}
