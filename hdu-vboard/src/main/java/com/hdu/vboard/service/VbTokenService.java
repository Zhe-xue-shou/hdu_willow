package com.hdu.vboard.service;

import com.hdu.svccmn.service.TokenService;

public interface VbTokenService extends TokenService<Boolean> {
  Boolean reload(String token) throws Exception;
}
