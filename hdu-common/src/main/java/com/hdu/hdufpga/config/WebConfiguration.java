package com.hdu.hdufpga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

  @Value("${cors.allowed-origins}")
  private String[] allowedOrigins;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("POST", "GET", "OPTIONS", "DELETE", "PUT")
        .allowedHeaders("x-requested-with", "satoken", "Content-Type", "Authorization", "token")
        .allowCredentials(true);
  }
}
