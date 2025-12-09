package com.hdu.hdufpga.controller;

import com.hdu.hdufpga.annotation.CheckToken;
import com.hdu.hdufpga.entity.Result;
import com.hdu.hdufpga.entity.vo.UserVO;
import com.hdu.hdufpga.service.CbTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/token")
@Slf4j
public class TokenController {
    @Resource
    CbTokenService cbTokenService;

    //level >= 1
    @GetMapping("/generateToken")
    public Result generateToken(UserVO userVO) {
        try {
            return Result.ok(cbTokenService.generateToken(userVO));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    //level >= 1
    @PostMapping("/reload")
    @CheckToken
    public Result reload(HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            return Result.ok(cbTokenService.reload(token));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    //level >= 1
    @PostMapping("/checkToken")
    @CheckToken
    public Result checkToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            return Result.ok(cbTokenService.checkToken(token));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(e.getMessage());
        }
    }

}
