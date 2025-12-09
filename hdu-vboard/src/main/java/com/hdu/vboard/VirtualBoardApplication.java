package com.hdu.vboard;

import com.hdu.hdufpga.config.WebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(WebConfiguration.class)
@SpringBootApplication(scanBasePackages = "com.hdu")
public class VirtualBoardApplication {
  public static void main(String[] args) {
    SpringApplication.run(VirtualBoardApplication.class, args);
  }
}