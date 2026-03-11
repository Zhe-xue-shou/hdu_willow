package com.hdu.svccmn.service.impl;

import com.hdu.hdufpga.entity.po.UserPO;
import com.hdu.hdufpga.service.UserService;
import com.hdu.svccmn.service.UserStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Slf4j
public class UserStatisticServiceImpl implements UserStatisticService {
  @DubboReference(check = false)
  private UserService userService;

  @Override
  @Transactional
  public void updateUserExptime(String username, Integer departmentId, Long sTime) throws Exception {
    long curTime = System.currentTimeMillis();

    if (sTime == null || sTime <= 0 || sTime >= curTime) {
      throw new Exception("stime is null or stime > curtime");
    }
    Duration expTime = Duration.ofMillis(curTime - sTime);

    long expTimeInMillis = expTime.toMillis();

    // get UserPO by username and department ID
    UserPO userPO = userService.getUserByUserName(username, departmentId);

    if (userPO == null) {
      log.error("Cannot find userPO in database for username: {}, departmentId: {}",
          username, departmentId);
      return;
    }

    long currentTotalMillis = userPO.getTotActiveTime();
    userPO.setTotActiveTime(currentTotalMillis + expTimeInMillis);

    int currentCount = userPO.getTotExpCnt();
    userPO.setTotExpCnt(currentCount + 1);

    userService.updateById(userPO);
    log.info("Updated user {} statistics: +{} ms, +1 exp",
        username, expTimeInMillis);
  }
}
