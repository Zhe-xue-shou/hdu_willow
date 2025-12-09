package com.hdu.svccmn.service;

import com.hdu.hdufpga.entity.vo.UserVO;

public interface UserStatisticService {
  void storeUserByToken(String token, UserVO userVO);

  void updateUserExptime(String token, Long sTime);

}
