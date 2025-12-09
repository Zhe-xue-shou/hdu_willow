package com.hdu.svccmn.service;

import com.hdu.hdufpga.entity.vo.UserVO;

public interface TokenService<T> {
  String generateToken(UserVO userVO) throws Exception;

  Boolean checkToken(String token) throws Exception;

  T reload(String token) throws Exception;
}
