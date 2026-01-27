package com.hdu.vboard.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hdu.hdufpga.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

  private Integer fileUploadTime;

  private Integer status;
}
