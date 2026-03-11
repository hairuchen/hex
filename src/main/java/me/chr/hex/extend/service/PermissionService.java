package me.chr.hex.extend.service;


import me.chr.hex.extend.BO.Permission;
import me.chr.hex.general.entity.User;

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
}
