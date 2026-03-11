package me.chr.hex.extend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.chr.hex.extend.BO.Permission;
import me.chr.hex.extend.mapper.PermissionMapper;
import me.chr.hex.extend.service.PermissionService;
import me.chr.hex.general.entity.Menu;
import me.chr.hex.general.entity.Role;
import me.chr.hex.general.entity.User;
import me.chr.hex.general.mapper.DepartmentMapper;
import me.chr.hex.general.mapper.MenuMapper;
import me.chr.hex.general.mapper.RoleMapper;
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
    private RoleMapper roleMapper;
    @Autowired
    private MenuMapper menuMapper;
    @Autowired
    private DepartmentMapper deptMapper;

    @Override
    public List<Permission> getUserPermission(String userId) {
        HashSet<Permission> permissionHashSet=new HashSet<>();

        // -------------------------- 路径1：用户直接关联的权限 --------------------------
        permissionHashSet.addAll(this.collectUserPermissions(userId));
        // -------------------------- 路径2：用户→角色→权限 --------------------------
        permissionHashSet.addAll(this.collectRolePermissions(userId));
        // -------------------------- 路径3：用户→部门→权限 --------------------------
        permissionHashSet.addAll(this.collectDeptPermissions(userId));
        // -------------------------- 路径4：用户→部门→角色→权限 --------------------------
        permissionHashSet.addAll(this.collectDeptRolePermissions(userId));

        return  new ArrayList<>(permissionHashSet);
    }

    // -------------------------- 路径1：用户直接关联的权限 --------------------------
    private List<Permission> collectUserPermissions(String userId) {
        return permissionMapper.selectMenusByUserId(userId);
    }

    // -------------------------- 路径2：用户→角色→权限 --------------------------
    private List<Permission> collectRolePermissions(String userId) {
        List<HashMap<String,String>> hashMapList=permissionMapper.selectRole2MenuByUserId(userId);
        HashSet<String> hashSet=new HashSet<>();
        for (HashMap<String,String> hashMap:hashMapList){
            hashSet.add(hashMap.get("menu_id"));
        }
        if(hashSet.isEmpty()){
            return new ArrayList<>();
        }
        List<Menu> menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>().in(Menu::getId, hashSet));
        return menus.stream()
                .map(menu -> {
                    Permission permission = new Permission();
                    BeanUtils.copyProperties(menu, permission);
                    return permission;
                })
                .collect(Collectors.toList());
    }

    // -------------------------- 路径3：用户→部门→权限 --------------------------
    private List<Permission> collectDeptPermissions(String userId) {
        List<HashMap<String,String>> hashMapList=permissionMapper.selectDept2MenuByUserId(userId);
        HashSet<String> hashSet=new HashSet<>();
        for (HashMap<String,String> hashMap:hashMapList){
            hashSet.add(hashMap.get("menu_id"));
        }
        if(hashSet.isEmpty()){
            return new ArrayList<>();
        }
        List<Menu> menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>().in(Menu::getId, hashSet));
        return menus.stream()
                .map(menu -> {
                    Permission permission = new Permission();
                    BeanUtils.copyProperties(menu, permission);
                    return permission;
                })
                .collect(Collectors.toList());
    }

    // -------------------------- 路径4：用户→部门→角色→权限 --------------------------
    private List<Permission> collectDeptRolePermissions(String userId) {
        List<HashMap<String,String>> hashMapList=permissionMapper.selectDept2Role2MenuByUserId(userId);
        HashSet<String> hashSet=new HashSet<>();
        for (HashMap<String,String> hashMap:hashMapList){
            hashSet.add(hashMap.get("menu_id"));
        }
        if(hashSet.isEmpty()){
            return new ArrayList<>();
        }
        List<Menu> menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>().in(Menu::getId, hashSet));
        return menus.stream()
                .map(menu -> {
                    Permission permission = new Permission();
                    BeanUtils.copyProperties(menu, permission);
                    return permission;
                })
                .collect(Collectors.toList());
    }


}
