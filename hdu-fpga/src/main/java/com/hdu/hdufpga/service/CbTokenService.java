package com.hdu.hdufpga.service;

import com.hdu.hdufpga.entity.vo.UserConnectionVO;

public interface CbTokenService {
    String generateToken() throws Exception;

    Boolean checkToken(String token) throws Exception;

    UserConnectionVO reload(String token) throws Exception;
}
