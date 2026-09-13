package com.hdu.vboard;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.hdu")
@EnableDubbo
@EnableTransactionManagement
public class VirtualBoardApplication {
  public static void main(String[] args) {
    SpringApplication.run(VirtualBoardApplication.class, args);
  }
}