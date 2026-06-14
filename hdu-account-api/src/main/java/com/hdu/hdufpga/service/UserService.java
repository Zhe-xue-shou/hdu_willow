package com.hdu.hdufpga.service;

import com.github.yulichang.base.MPJBaseService;
import com.hdu.hdufpga.entity.dto.UserStatisticDTO;
import com.hdu.hdufpga.entity.po.UserPO;
import com.hdu.hdufpga.entity.vo.UserVO;

import java.util.Date;
import java.util.List;


public interface UserService extends MPJBaseService<UserPO> {
  List<Integer> getIdByUserName(List<String> poList, Integer departmentId);

  Long getUserCountByDate(Date startDate, Date endDate);

  UserPO getUserByUserName(String userName, Integer departmentId);

  // 该方法会简单地调用super.updateById
  // 这是为了兼容以前的updateById方法 那个方法会对密码使用md5加密
  public boolean updateByIdWithoutChangePassword(UserPO entity);

  UserVO UserPO2UserVO(UserPO userPO);

  UserVO createThirdUser(String uid, String source);

  UserStatisticDTO getCurrentUserStatistics() throws Exception;
}
