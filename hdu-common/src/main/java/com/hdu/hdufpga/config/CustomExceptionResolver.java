package com.hdu.hdufpga.config;

import cn.dev33.satoken.exception.SaTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdu.hdufpga.entity.Result;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CustomExceptionResolver implements HandlerExceptionResolver {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public ModelAndView resolveException(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      Object handler,
      @NonNull Exception e) {

    Result result;

    // ===== SaToken异常处理 =====
    if (e instanceof SaTokenException) {
      SaTokenException ex = (SaTokenException) e;

      if (ex.getCode() == 30001) {
        result = Result.error("redirect 重定向 url 是一个无效地址");
      } else if (ex.getCode() == 30002) {
        result = Result.error("redirect 重定向 url 不在 allowUrl 允许范围");
      } else if (ex.getCode() == 30004) {
        result = Result.error("提供的 ticket 是无效的");
      } else if (ex.getCode() == 11001 || ex.getCode() == 11011) {
        result = Result.error("未能读取到有效Token");
      } else if (ex.getCode() == 11012) {
        result = Result.error("Token无效");
      } else if (ex.getCode() == 11051) {
        result = Result.error("缺少指定权限");
      } else {
        result = Result.error("服务器繁忙，请稍后重试(SaToken错误码:" + ex.getCode() + ")");
      }
    }

    // ===== Runtime异常 =====
    else if (e instanceof RuntimeException) {
      RuntimeException ex = (RuntimeException) e;
      result = Result.error("服务器运行时错误，请稍后重试(Msg:" + ex.getMessage() + ")");
    }

    // ===== 兜底异常 =====
    else {
      result = Result.error("系统异常：" + e.getMessage() + "异常类型：" + e.getClass());
    }

    try {
      response.setCharacterEncoding("UTF-8");
      response.setContentType("application/json;charset=UTF-8");

      // 写JSON响应
      response.getWriter().write(objectMapper.writeValueAsString(result));
    } catch (Exception writeEx) {
      writeEx.printStackTrace();
    }

    // 返回空表示异常已处理
    return new ModelAndView();
  }
}