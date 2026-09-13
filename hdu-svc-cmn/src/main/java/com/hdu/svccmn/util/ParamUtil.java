package com.hdu.svccmn.util;

import com.hdu.hdufpga.entity.constant.AccountConstant;
import com.hdu.hdufpga.entity.vo.UserVO;
import com.mysql.cj.util.StringUtils;

public class ParamUtil {
  public static Boolean CheckUserInfoLegal(UserVO userVO) {
    return !StringUtils.isNullOrEmpty(userVO.getUsername()) && !StringUtils.isNullOrEmpty(userVO.getUserDepartmentName());
  }

  public static String generateUserToken(UserVO userVO, String salt) {
    if (!StringUtils.isNullOrEmpty(salt)) {
      return String.join(AccountConstant.token_split_char, userVO.getUsername(), userVO.getUserDepartmentName(), userVO.getUserDepartmentId().toString(), salt);
    } else {
      throw new IllegalArgumentException("salt为空,请重新生成");
    }
  }
}
