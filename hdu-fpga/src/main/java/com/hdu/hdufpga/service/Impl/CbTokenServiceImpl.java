package com.hdu.hdufpga.service.Impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import com.hdu.hdufpga.entity.constant.RedisConstant;
import com.hdu.hdufpga.entity.vo.UserConnectionVO;
import com.hdu.hdufpga.entity.vo.UserVO;
import com.hdu.hdufpga.service.CbTokenService;
import com.hdu.hdufpga.service.WaitingService;
import com.hdu.hdufpga.util.RedisUtil;
import com.hdu.svccmn.exception.IdentifyException;
import com.hdu.svccmn.exception.NullTokenException;
import com.hdu.svccmn.exception.TokenExpiredException;
import com.hdu.svccmn.util.ParamUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class CbTokenServiceImpl implements CbTokenService {
  @Resource
  RedisUtil redisUtil;

  @Resource
  WaitingService waitingService;

  @Override
  public String generateToken() throws Exception {
    UserVO userVO = (UserVO) StpUtil.getSession().get("user");
    if (!ParamUtil.CheckUserInfoLegal(userVO)) {
      throw new IdentifyException("身份信息有误");
    }// 检查传递的userVO参数是否为空
    String salt = StpUtil.getTokenValue(); // 随机生成一个uuid
    String token = ParamUtil.generateUserToken(userVO, salt); //生成token
    redisUtil.set(RedisConstant.REDIS_TTL_PREFIX + token, true, RedisConstant.REDIS_TTL_LIMIT, TimeUnit.SECONDS);
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

  @Override
  public UserConnectionVO reload(String token) throws Exception {
    redisUtil.set(RedisConstant.REDIS_TTL_PREFIX + token, true, RedisConstant.REDIS_TTL_LIMIT, TimeUnit.SECONDS);
    long leftTime = redisUtil.getExpire(RedisConstant.REDIS_CONN_SHADOW_PREFIX + token, TimeUnit.SECONDS);
    if (leftTime > 0) {
      UserConnectionVO userConnectionVO = Convert.convert(UserConnectionVO.class, redisUtil.get(RedisConstant.REDIS_CONN_PREFIX + token));
      if (userConnectionVO != null) {
        // 如果还是冻结状态
        if (userConnectionVO.getIsFrozen()) {
          return null;
        }
        userConnectionVO.setLeftSecond(leftTime);
      }
      return userConnectionVO;
    } else if (leftTime == -1) {
      throw new Exception("ttl错误被设置为永久");
    }
    return null;
  }
}
