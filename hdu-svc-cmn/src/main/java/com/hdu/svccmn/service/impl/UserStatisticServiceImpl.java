package com.hdu.svccmn.service.impl;

import com.hdu.hdufpga.entity.constant.RedisConstant;
import com.hdu.hdufpga.entity.dto.UserStatisticDTO;
import com.hdu.hdufpga.entity.po.UserPO;
import com.hdu.hdufpga.service.UserService;
import com.hdu.hdufpga.util.RedisUtil;
import com.hdu.svccmn.service.UserStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;

@Service
@Slf4j
public class UserStatisticServiceImpl implements UserStatisticService {
  @DubboReference(check = false)
  private UserService userService;

  @Resource
  private RedisUtil redisUtil;

  @Override
  @Transactional
  public UserStatisticDTO updateUserExptime(String username, Integer departmentId, Long sTime) throws Exception {
    long curTime = System.currentTimeMillis();

    if (sTime == null) {
      return null;
    }

    if (sTime <= 0 || sTime >= curTime) {
      throw new Exception("stime > curtime");
    }
    Duration expTime = Duration.ofMillis(curTime - sTime);

    long expTimeInMillis = expTime.toMillis();

    // get UserPO by username and department ID
    UserPO userPO = userService.getUserByUserName(username, departmentId);

    if (userPO == null) {
      log.error("Cannot find userPO in database for username: {}, departmentId: {}",
          username, departmentId);
      throw new Exception("找不到用户");
    }

    long currentTotalMillis = userPO.getTotActiveTime();
    userPO.setTotActiveTime(currentTotalMillis + expTimeInMillis);

    int currentCount = userPO.getTotExpCnt();
    userPO.setTotExpCnt(currentCount + 1);

    userService.updateById(userPO);
    log.info("Updated user {} statistics: +{} ms, +1 exp",
        username, expTimeInMillis);
    return UserStatisticDTO.builder()
        .addExpCnt(1)
        .addActiveTime(expTimeInMillis)
        .totActiveTime(currentTotalMillis + expTimeInMillis)
        .totExpCnt(currentCount + 1)
        .build();
  }

  @Override
  public UserStatisticDTO updateUserExptimeByToken(String token) throws Exception {
    String[] tokenInfo = token.split("_");

    if (tokenInfo.length < 4) {
      log.error("experience token invalid! token value:{}", token);
      throw new Exception("实验Token解析失败");
    }

    String username = tokenInfo[0];
    Integer departmentId = Integer.parseInt(tokenInfo[2]);

    Long startTime =
        (Long) redisUtil.get(RedisConstant.REDIS_EXP_START_TIME_PREFIX + token);

    return updateUserExptime(username, departmentId, startTime);
  }
}
