package com.hdu.vboard.service.impl;

import com.hdu.hdufpga.service.AbstractSsoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@DubboService(group = "vboardSso")
public class VboardSsoService implements AbstractSsoService {
    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public String getApplicationName() {
        log.info("getApplication name {}", applicationName);
        return applicationName;
    }
}
