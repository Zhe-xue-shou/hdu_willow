package com.hdu.hdufpga;

import com.hdu.hdufpga.config.WebConfiguration;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Import(WebConfiguration.class)
@SpringBootApplication(scanBasePackages = "com.hdu")
@EnableDubbo
public class HduAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(HduAuthApplication.class, args);
    }
}