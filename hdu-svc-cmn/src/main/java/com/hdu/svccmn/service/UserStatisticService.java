package com.hdu.svccmn.service;

import com.hdu.hdufpga.entity.dto.UserStatisticDTO;

public interface UserStatisticService {
  UserStatisticDTO updateUserExptime(String username, Integer departmentId, Long sTime) throws Exception;

  UserStatisticDTO updateUserExptimeByToken(String token) throws Exception;
}
