package com.hdu.hdufpga.service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@Lazy
@Slf4j
public class SsoService {

    private final Set<AbstractSsoService> ssoServices = new HashSet<>();

    @DubboReference(group = "accountSso", check = false)
    private AbstractSsoService accountSsoService;

    @DubboReference(group = "fpgaSso", check = false)
    private AbstractSsoService fpgaSsoService;

    @DubboReference(group = "interruptSso", check = false)
    private AbstractSsoService interruptSsoService;

    @DubboReference(group = "recordSso", check = false)
    private AbstractSsoService recordSsoService;

    @DubboReference(group="vboardSso",check = false)
    private AbstractSsoService vboardSsoService;

    // todo!()

    private void init() {
        if (ssoServices.isEmpty()) {
            try {
                addServiceSafely(accountSsoService, "accountSso");
                addServiceSafely(fpgaSsoService, "fpgaSso");
                addServiceSafely(interruptSsoService, "interruptSso");
                addServiceSafely(recordSsoService, "recordSso");
                addServiceSafely(vboardSsoService, "vboardSso");

                log.info("SSO服务初始化完成，共加载 {} 个服务", ssoServices.size());
            } catch (Exception e) {
                log.warn("SSO服务初始化过程中出现异常，已加载 {} 个服务: {}", ssoServices.size(), e.getMessage());
            }
        }
    }

    private void addServiceSafely(AbstractSsoService service, String serviceName) {
        try {
            if (service != null) {
                // 尝试调用方法验证服务是否可用
                String appName = service.getApplicationName();
                ssoServices.add(service);
                log.debug("成功加载SSO服务: {} -> {}", serviceName, appName);
            }
        } catch (Exception e) {
            log.warn("加载SSO服务 {} 失败: {}", serviceName, e.getMessage());
        }
    }

    public AbstractSsoService getSsoService(String applicationName) {
        init();
        for (AbstractSsoService ssoService : ssoServices) {
            if (Objects.nonNull(ssoService) && StrUtil.equals(ssoService.getApplicationName(), applicationName)) {
                return ssoService;
            }
        }
        return null;
    }

    public Set<AbstractSsoService> getAllServices() {
        init();
        return ssoServices;
    }
}
