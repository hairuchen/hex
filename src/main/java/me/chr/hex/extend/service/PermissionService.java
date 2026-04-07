package me.chr.hex.extend.service;


import me.chr.hex.extend.BO.Permission;
import me.chr.hex.general.entity.SysUser;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/3/12
 **/
public interface PermissionService {

    /**
     * 获取用户的所有权限标识
     */
    List<Permission> getUserPermission(String userId);

    /**
     * 获取租户的所有权限（租户拥有其下所有权限）
     * @param tenantId 租户ID
     * @return 该租户下的所有权限
     */
    List<Permission> getTenantAllPermissions(String tenantId);
}
