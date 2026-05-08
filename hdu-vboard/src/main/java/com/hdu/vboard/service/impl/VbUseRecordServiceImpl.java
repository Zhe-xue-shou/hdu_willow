package com.hdu.vboard.service.impl;

import com.github.yulichang.base.MPJBaseServiceImpl;
import com.hdu.hdufpga.util.TimeUtil;
import com.hdu.vboard.entity.po.VbUseRecordPO;
import com.hdu.vboard.entity.vo.VbConnectionVO;
import com.hdu.vboard.mapper.VbUseRecordMapper;
import com.hdu.vboard.service.VbUseRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VbUseRecordServiceImpl extends MPJBaseServiceImpl<VbUseRecordMapper, VbUseRecordPO> implements VbUseRecordService {
  @Override
  @Transactional
  public Boolean saveVbRecord(VbConnectionVO vbConnectionVO, int status, Long addActiveTime) {
    VbUseRecordPO vbUseRecordPO = new VbUseRecordPO();
    vbUseRecordPO.setUserName(vbConnectionVO.getUserName());
    vbUseRecordPO.setUserIp(vbConnectionVO.getUserIp());
    vbUseRecordPO.setDepartmentName(vbConnectionVO.getDepartmentName());
    vbUseRecordPO.setDuration(Math.toIntExact(addActiveTime));
    vbUseRecordPO.setBuildTime(vbConnectionVO.getBuildTime());
    vbUseRecordPO.setStatus(status);
    vbUseRecordPO.setCreateTime(TimeUtil.getNowTime());
    vbUseRecordPO.setUpdateTime(TimeUtil.getNowTime());
    return save(vbUseRecordPO);
  }
}
