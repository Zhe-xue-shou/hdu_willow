package com.hdu.vboard;

import com.hdu.hdufpga.config.WebConfiguration;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(WebConfiguration.class)
@SpringBootApplication(scanBasePackages = "com.hdu")
@EnableDubbo
public class VirtualBoardApplication {
  public static void main(String[] args) {
    SpringApplication.run(VirtualBoardApplication.class, args);
  }
}