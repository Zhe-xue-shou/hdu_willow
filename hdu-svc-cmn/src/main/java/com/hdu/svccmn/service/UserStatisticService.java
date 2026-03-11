package com.hdu.svccmn.service;

public interface UserStatisticService {
  void updateUserExptime(String username, Integer departmentId, Long sTime) throws Exception;
}
