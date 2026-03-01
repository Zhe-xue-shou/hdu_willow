package com.hdu.hdufpga.handler;

import cn.dev33.satoken.exception.SaTokenException;
import com.hdu.hdufpga.entity.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(SaTokenException.class)
  public Result handlerSaTokenException(SaTokenException e) {
    // 根据不同异常细分状态码返回不同的提示
    if (e.getCode() == 30001) {
      return Result.error("redirect 重定向 url 是一个无效地址");
    }
    if (e.getCode() == 30002) {
      return Result.error("redirect 重定向 url 不在 allowUrl 允许的范围内");
    }
    if (e.getCode() == 30004) {
      return Result.error("提供的 ticket 是无效的");
    }
    if (e.getCode() == 11001) {
      return Result.error("未能读取到有效Token");
    }
    if (e.getCode() == 11012) {
      return Result.error("Token无效");
    }

    // 默认的提示
    return Result.error("服务器繁忙，请稍后重试(SaToken错误码:" + e.getCode() + ")");
  }

  @ExceptionHandler(RuntimeException.class)
  public Result handlerRuntimeException(RuntimeException e) {
    return Result.error("服务器运行时错误，请稍后重试(Msg:" + e.getMessage() + ")");
  }
}
