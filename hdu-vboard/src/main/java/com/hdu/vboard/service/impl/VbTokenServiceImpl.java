package com.hdu.vboard.service.impl;

import com.hdu.hdufpga.entity.constant.RedisConstant;
import com.hdu.hdufpga.util.RedisUtil;
import com.hdu.vboard.service.VbTokenService;
import com.hdu.svccmn.service.impl.TokenServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class VbTokenServiceImpl extends TokenServiceImpl<Boolean> implements VbTokenService {
  @Resource
  RedisUtil redisUtil;

  @Override
  public Boolean reload(String token) throws Exception {
    return redisUtil.set(RedisConstant.REDIS_TTL_PREFIX + token, true, RedisConstant.REDIS_TTL_LIMIT, TimeUnit.SECONDS);
  }
}
