package com.hdu.svccmn.service;

public interface TokenService<T> {
  String generateToken() throws Exception;

  Boolean checkToken(String token) throws Exception;

  T reload(String token) throws Exception;
}
