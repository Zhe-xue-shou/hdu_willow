package com.hdu.vboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import javax.sql.DataSource;

@Configuration
@RefreshScope
public class DataSourceConfig {
//    @Bean
//    @ConfigurationProperties(prefix = "datasource")
//    public DataSource dataSource() {
//        return DataSourceBuilder.create().build();
//    }
}