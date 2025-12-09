package com.hdu.svccmn.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import com.hdu.hdufpga.entity.constant.RedisConstant;
import com.hdu.hdufpga.entity.vo.UserVO;
import com.hdu.hdufpga.util.RedisUtil;
import com.hdu.svccmn.exception.TokenExpiredException;
import com.hdu.svccmn.service.TokenService;
import com.hdu.svccmn.service.UserStatisticService;
import com.hdu.svccmn.util.ParamUtil;
import com.hdu.svccmn.exception.IdentifyException;
import com.hdu.svccmn.exception.NullTokenException;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

abstract public class TokenServiceImpl<T> implements TokenService<T> {
  @Resource
  RedisUtil redisUtil;

  @Resource
  UserStatisticService userStatisticService;

  @Override
  public String generateToken(UserVO userVO) throws Exception {
    if (!ParamUtil.CheckUserInfoLegal(userVO)) {
      throw new IdentifyException("身份信息有误");
    }// 检查传递的userVO参数是否为空
    String salt = IdUtil.simpleUUID(); // 随机生成一个uuid
    String token = ParamUtil.generateUserToken(userVO, salt); //生成token
    redisUtil.set(RedisConstant.REDIS_TTL_PREFIX + token, true, RedisConstant.REDIS_TTL_LIMIT, TimeUnit.SECONDS);
    redisUtil.set(RedisConstant.REDIS_EXP_START_TIME_PREFIX + token, System.currentTimeMillis(), RedisConstant.REDIS_TTL_LIMIT, TimeUnit.SECONDS);
    userStatisticService.storeUserByToken(token, userVO);
    return token;
  }

  @Override
  public Boolean checkToken(String token) throws Exception {
    if (Validator.isNull(token)) {
      throw new NullTokenException("token为空");
    }
    Boolean res = redisUtil.hasKey(RedisConstant.REDIS_TTL_PREFIX + token);
    if (res) {
      return true;
    }
    throw new TokenExpiredException("token已过期");
  }
}
