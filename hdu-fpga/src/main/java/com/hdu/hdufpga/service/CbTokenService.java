package com.hdu.hdufpga.service;

import com.hdu.hdufpga.entity.vo.UserConnectionVO;
import com.hdu.svccmn.service.TokenService;

public interface CbTokenService extends TokenService<UserConnectionVO> {
    UserConnectionVO reload(String token) throws Exception;
}
