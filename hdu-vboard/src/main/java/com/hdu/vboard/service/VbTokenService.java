package com.hdu.vboard.service;

public interface VbTokenService {
  String generateToken() throws Exception;

  Boolean checkToken(String token) throws Exception;

  Boolean reload(String token) throws Exception;
}
