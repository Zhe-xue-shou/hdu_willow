package com.hdu.svccmn.service;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface SaTokenRoleService extends StpInterface {
  List<String> getPermissionList(Object var1, String var2);

  List<String> getRoleList(Object var1, String var2);
}
