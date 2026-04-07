package me.chr.hex.extend.service.login.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.chr.hex.extend.BO.Permission;
import me.chr.hex.extend.mapper.PermissionMapper;
import me.chr.hex.extend.service.PermissionService;
import me.chr.hex.general.entity.SysPermission;
import me.chr.hex.general.mapper.WorkspaceMapper;
import me.chr.hex.general.mapper.SysPermissionMapper;
import me.chr.hex.general.mapper.SysRoleMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: CHR
 * @Date: create in 2026/3/12
 **/
@Primary
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Autowired
    private WorkspaceMapper workspaceMapper;

    @Override
    public List<Permission> getUserPermission(String userId) {
        HashSet<Permission> permissionHashSet=new HashSet<>();

        // -------------------------- 路径1：用户直接关联的权限 --------------------------
        permissionHashSet.addAll(this.collectUserPermissions(userId));
        // -------------------------- 路径2：用户→角色→权限 --------------------------
        permissionHashSet.addAll(this.collectRolePermissions(userId));
        // -------------------------- 路径3：用户→工作空间→权限 --------------------------
        permissionHashSet.addAll(this.collectWorkspacePermissions(userId));
        // -------------------------- 路径4：用户→工作空间→角色→权限 --------------------------
        permissionHashSet.addAll(this.collectWorkspaceRolePermissions(userId));

        return  new ArrayList<>(permissionHashSet);
    }

    @Override
    public List<Permission> getTenantAllPermissions(String tenantId) {
        // 租户拥有其租户下的所有权限
        List<SysPermission> allPermissions = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getTenantId, tenantId)
                        .eq(SysPermission::getStatus, 1)
                        .eq(SysPermission::getIsDeleted, 0));

        return allPermissions.stream()
                .map(sysPermission -> {
                    Permission permission = new Permission();
                    BeanUtils.copyProperties(sysPermission, permission);
                    return permission;
                })
                .collect(Collectors.toList());
    }

    // -------------------------- 路径1：用户直接关联的权限 --------------------------
    private List<Permission> collectUserPermissions(String userId) {
        return permissionMapper.selectPermissionsByUserId(userId);
    }

    // -------------------------- 路径2：用户→角色→权限 --------------------------
    private List<Permission> collectRolePermissions(String userId) {
        List<HashMap<String,String>> hashMapList=permissionMapper.selectRole2PermissionByUserId(userId);
        HashSet<String> hashSet=new HashSet<>();
        for (HashMap<String,String> hashMap:hashMapList){
            hashSet.add(hashMap.get("permission_id"));
        }
        if(hashSet.isEmpty()){
            return new ArrayList<>();
        }
        List<SysPermission> permissions = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, hashSet));
        return permissions.stream()
                .map(sysPermission -> {
                    Permission permission = new Permission();
                    BeanUtils.copyProperties(sysPermission, permission);
                    return permission;
                })
                .collect(Collectors.toList());
    }

    // -------------------------- 路径3：用户→工作空间→权限 --------------------------
    private List<Permission> collectWorkspacePermissions(String userId) {
        List<HashMap<String,String>> hashMapList=permissionMapper.selectWorkspace2PermissionByUserId(userId);
        HashSet<String> hashSet=new HashSet<>();
        for (HashMap<String,String> hashMap:hashMapList){
            hashSet.add(hashMap.get("permission_id"));
        }
        if(hashSet.isEmpty()){
            return new ArrayList<>();
        }
        List<SysPermission> permissions = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, hashSet));
        return permissions.stream()
                .map(sysPermission -> {
                    Permission permission = new Permission();
                    BeanUtils.copyProperties(sysPermission, permission);
                    return permission;
                })
                .collect(Collectors.toList());
    }

    // -------------------------- 路径4：用户→工作空间→角色→权限 --------------------------
    private List<Permission> collectWorkspaceRolePermissions(String userId) {
        List<HashMap<String,String>> hashMapList=permissionMapper.selectWorkspace2Role2PermissionByUserId(userId);
        HashSet<String> hashSet=new HashSet<>();
        for (HashMap<String,String> hashMap:hashMapList){
            hashSet.add(hashMap.get("permission_id"));
        }
        if(hashSet.isEmpty()){
            return new ArrayList<>();
        }
        List<SysPermission> permissions = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, hashSet));
        return permissions.stream()
                .map(sysPermission -> {
                    Permission permission = new Permission();
                    BeanUtils.copyProperties(sysPermission, permission);
                    return permission;
                })
                .collect(Collectors.toList());
    }


}
