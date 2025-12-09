package com.hdu.hdufpga.service.Impl;

import com.hdu.hdufpga.entity.constant.RedisConstant;
import com.hdu.hdufpga.entity.vo.UserConnectionVO;
import com.hdu.hdufpga.service.CbTokenService;
import com.hdu.hdufpga.service.WaitingService;
import com.hdu.hdufpga.util.RedisUtil;
import com.hdu.svccmn.service.impl.TokenServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class CbTokenServiceImpl extends TokenServiceImpl<UserConnectionVO> implements CbTokenService{
  @Resource
  RedisUtil redisUtil;

  @Resource
  WaitingService waitingService;

  @Override
  public UserConnectionVO reload(String token) throws Exception {
    redisUtil.set(RedisConstant.REDIS_TTL_PREFIX + token, true, RedisConstant.REDIS_TTL_LIMIT, TimeUnit.SECONDS);
    return waitingService.userInQueue(token);
  }
}
