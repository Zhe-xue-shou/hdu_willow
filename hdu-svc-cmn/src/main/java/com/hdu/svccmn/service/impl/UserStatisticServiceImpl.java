package com.hdu.svccmn.service.impl;

import com.hdu.hdufpga.entity.po.UserPO;
import com.hdu.hdufpga.entity.vo.UserVO;
import com.hdu.hdufpga.service.UserService;
import com.hdu.svccmn.service.UserStatisticService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserStatisticServiceImpl implements UserStatisticService {
  @DubboReference(check = false)
  private UserService userService;

  private final ConcurrentHashMap<String, UserVO> tokTable = new ConcurrentHashMap<>();

  @Override
  @Transactional
  public void updateUserExptime(@NonNull String token, @Nullable Long sTime) {
    UserVO userVO = tokTable.remove(token); // remove the token from the table

    if (userVO == null) {
      log.error("an unexpect error has occurred: user with token {} is null", token);
      return;
    }
    long curTime = System.currentTimeMillis();

    if (sTime == null || sTime <= 0 || sTime >= curTime) {
      log.error("an unexpect error has occurred: user with token {} has no exp time", token);
      return;
    }
    Duration expTime = Duration.ofMillis(curTime - sTime);

    // todo: is it needed to also modify the UserVO?
    Duration currentTotalTime = userVO.getTotActiveTime();
    userVO.setTotActiveTime(currentTotalTime.plus(expTime));

    int currentExpCount = userVO.getTotExpCnt();
    userVO.setTotExpCnt(currentExpCount + 1);

    long expTimeInMillis = expTime.toMillis();

    // get UserPO by username and department ID
    UserPO userPO = userService.getUserByUserName(
        userVO.getUsername(),
        userVO.getUserDepartmentId());

    if (userPO == null) {
      log.error("Cannot find userPO in database for username: {}, departmentId: {}",
          userVO.getUsername(), userVO.getUserDepartmentId());
      return;
    }

    long currentTotalMillis = userPO.getTotActiveTime();
    userPO.setTotActiveTime(currentTotalMillis + expTimeInMillis);

    int currentCount = userPO.getTotExpCnt();
    userPO.setTotExpCnt(currentCount + 1);

    userService.updateById(userPO);
    log.info("Updated user {} statistics: +{} ms, +1 exp",
        userVO.getUsername(), expTimeInMillis);
  }

  @Override
  public void storeUserByToken(String token, UserVO userVO) {
    tokTable.put(token, userVO);
  }
}
