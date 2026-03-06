package com.hdu.hdufpga.controller;

import com.hdu.hdufpga.entity.Result;
import com.hdu.hdufpga.entity.ro.LoginRO;
import com.hdu.hdufpga.entity.ro.VerificationCodeRO;
import com.hdu.hdufpga.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

  @Resource
  private AuthService authService;

  @PostMapping("/login")
  public Result login(@RequestBody LoginRO loginRO) {
    try {
      return Result.ok(authService.login(loginRO));
    } catch (Exception e) {
      log.error(e.getMessage());
      return Result.error(e.getMessage());
    }
  }

  @GetMapping("/logout")
  public Result logout() {
    return authService.logout();
  }

  @PostMapping("/generate-verification-code")
  public Result generateVerificationCode(@RequestBody VerificationCodeRO verificationCodeRO) throws IOException {
    authService.generateVerificationCode(verificationCodeRO);
    return Result.ok();
  }

  @GetMapping("third_login")
  public Result thirdLogin(@RequestParam String uid,
                           @RequestParam Long timestamp,
                           @RequestParam String source,
                           @RequestParam String sign) {
    try {
      return Result.ok(authService.thirdLogin(uid, timestamp, source, sign));
    } catch (Exception e) {
      log.error(e.getMessage());
      return Result.error(e.getMessage());
    }
  }
}
