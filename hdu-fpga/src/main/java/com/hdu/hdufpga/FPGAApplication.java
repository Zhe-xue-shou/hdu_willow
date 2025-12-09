package com.hdu.hdufpga;

import com.hdu.svccmn.service.impl.UserStatisticServiceImpl;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableDubbo
@Import(UserStatisticServiceImpl.class)
public class FPGAApplication {

  public static void main(String[] args) {
    SpringApplication.run(FPGAApplication.class, args);
  }

}