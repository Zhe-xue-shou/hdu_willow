package com.hdu.vboard.service;

import com.github.yulichang.base.MPJBaseService;
import com.hdu.vboard.entity.po.VbUseRecordPO;
import com.hdu.vboard.entity.vo.VbConnectionVO;

public interface VbUseRecordService extends MPJBaseService<VbUseRecordPO> {
  Boolean saveVbRecord(VbConnectionVO vbConnectionVO, int status);
}
