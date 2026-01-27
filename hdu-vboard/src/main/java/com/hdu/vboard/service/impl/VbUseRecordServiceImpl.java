package com.hdu.vboard.service.impl;

import cn.hutool.extra.servlet.ServletUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.hdu.vboard.entity.po.VbUseRecordPO;
import com.hdu.vboard.mapper.VbUseRecordMapper;
import com.hdu.vboard.service.VbUseRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Service
public class VbUseRecordServiceImpl extends MPJBaseServiceImpl<VbUseRecordMapper, VbUseRecordPO> implements VbUseRecordService {

  @Resource
  HttpServletRequest request;

  @Override
  public Boolean saveVbRecord(String token, int status) {
    String[] info = token.split("_");
    VbUseRecordPO vbUseRecordPO = new VbUseRecordPO();
    vbUseRecordPO.setUserName(info[0]);
    vbUseRecordPO.setUserIp(ServletUtil.getClientIP(request));
    vbUseRecordPO.setDepartmentName(info[1]);
    vbUseRecordPO.setDuration(114514);
    vbUseRecordPO.setFileUploadTime(1);
    vbUseRecordPO.setStatus(status);
    return save(vbUseRecordPO);
  }
}
