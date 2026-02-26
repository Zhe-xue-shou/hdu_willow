package com.hdu.vboard.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.extern.slf4j.Slf4j;

@RestController
@RefreshScope  // 支持配置动态刷新
@Slf4j
@RequestMapping("/config")
public class ConfigController {

    // 直接从 Nacos 配置中读取值
    @Value("${spring.application.name:unknown}")
    private String appName;

    @Value("${redis.host:localhost}")  // 冒号后是默认值
    private String redisHost;

    @Value("${redis.port:6379}")
    private Integer redisPort;

    @Value("${spring.datasource.url:NOT_LOADED_FROM_NACOS}")
    private String dbUrl;

    @Value("${seata.service.grouplist.default}")
    private String seataAddress;

    @GetMapping("/check")
    public String checkConfig() {
        log.info("AppName:{}", appName);
        log.info("Redis配置 - 地址: {}, 端口: {}", redisHost, redisPort);
        log.info("数据库URL: {}", dbUrl);
        log.info("Seata地址: {}", seataAddress);

        return String.format("AppName:%s Redis: %s:%d, DB: %s, Seata: %s",
                appName, redisHost, redisPort, dbUrl, seataAddress);
    }

    @GetMapping("/redis")
    public String getRedisConfig() {
        return String.format("Redis服务器: %s:%d", redisHost, redisPort);
    }
}