package com.hdu.vboard.service;

import com.github.yulichang.base.MPJBaseService;
import com.hdu.vboard.entity.po.VbUseRecordPO;

public interface VbUseRecordService extends MPJBaseService<VbUseRecordPO> {
  Boolean saveVbRecord(String token, int status);
}
