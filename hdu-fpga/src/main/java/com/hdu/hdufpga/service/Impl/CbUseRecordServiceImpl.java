package com.hdu.hdufpga.service.Impl;

import com.github.yulichang.base.MPJBaseServiceImpl;
import com.hdu.hdufpga.entity.constant.AccountConstant;
import com.hdu.hdufpga.entity.po.CbUseRecordPO;
import com.hdu.hdufpga.entity.vo.UserConnectionVO;
import com.hdu.hdufpga.mapper.CbUseRecordMapper;
import com.hdu.hdufpga.service.CbUseRecordService;
import com.hdu.hdufpga.util.TimeUtil;
import org.springframework.stereotype.Service;

@Service
public class CbUseRecordServiceImpl extends MPJBaseServiceImpl<CbUseRecordMapper, CbUseRecordPO> implements CbUseRecordService {

  @Override
  public Boolean saveUseRecord(UserConnectionVO userConnectionVO, Long addActiveTime) {
    CbUseRecordPO cbUseRecordPO = new CbUseRecordPO();
    String token = userConnectionVO.getToken();
    String[] info = token.split(AccountConstant.token_split_char);
    cbUseRecordPO.setCbId(userConnectionVO.getLongId());
    cbUseRecordPO.setCbIp(userConnectionVO.getCbIp());
    cbUseRecordPO.setUserName(info[0]);
    cbUseRecordPO.setUserIp(userConnectionVO.getUserIp());
    cbUseRecordPO.setDepartmentName(info[1]);
    cbUseRecordPO.setFileUploadTime(1);
    cbUseRecordPO.setDuration(Math.toIntExact(addActiveTime));
    cbUseRecordPO.setCreateTime(TimeUtil.getNowTime());
    cbUseRecordPO.setUpdateTime(TimeUtil.getNowTime());
    return save(cbUseRecordPO);
  }
}
