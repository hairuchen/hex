package me.chr.hex.extend.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.extend.DTO.PermissionDTO;
import me.chr.hex.extend.DTO.RolePermissionDTO;
import me.chr.hex.extend.DTO.UserPermissionDTO;
import me.chr.hex.extend.DTO.WorkspacePermissionDTO;
import me.chr.hex.general.entity.Role2permission;
import me.chr.hex.general.entity.SysPermission;
import me.chr.hex.general.entity.User2permission;
import me.chr.hex.general.entity.Workspace2permission;
import me.chr.hex.general.service.IRole2permissionService;
import me.chr.hex.general.service.ISysPermissionService;
import me.chr.hex.general.service.IUser2permissionService;
import me.chr.hex.general.service.IWorkspace2permissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/3/13
 **/
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private ISysPermissionService sysPermissionService;

    @Autowired
    private IUser2permissionService user2permissionService;

    @Autowired
    private IRole2permissionService role2permissionService;

    @Autowired
    private IWorkspace2permissionService workspace2permissionService;


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PermissionController:batchCreate')")
    @Operation(summary = "创建权限")
    public CommonResult<List<SysPermission>> batchCreate(@Validated @NotEmpty(message = "权限列表[permissionDTOList]不能为空") @RequestBody List<@Valid PermissionDTO> permissionDTOList) {
        return CommonResult.success(sysPermissionService.batchCreate(permissionDTOList));
    }

    @PostMapping("/grant/user")
    @PreAuthorize("hasAuthority('PermissionController:grantToUser')")
    @Operation(summary = "用户授权")
    public CommonResult<List<User2permission>> grantToUser(@Validated @NotEmpty(message = "授权列表不能为空") @RequestBody List<@Valid UserPermissionDTO> dtoList) {
        return CommonResult.success(user2permissionService.grantToUser(dtoList));
    }

    @PostMapping("/grant/role")
    @PreAuthorize("hasAuthority('PermissionController:grantToRole')")
    @Operation(summary = "角色授权")
    public CommonResult<List<Role2permission>> grantToRole(@Validated @NotEmpty(message = "授权列表不能为空") @RequestBody List<@Valid RolePermissionDTO> dtoList) {
        return CommonResult.success(role2permissionService.grantToRole(dtoList));
    }

    @PostMapping("/grant/workspace")
    @PreAuthorize("hasAuthority('PermissionController:grantToWorkspace')")
    @Operation(summary = "工作空间授权")
    public CommonResult<List<Workspace2permission>> grantToWorkspace(@Validated @NotEmpty(message = "授权列表不能为空") @RequestBody List<@Valid WorkspacePermissionDTO> dtoList) {
        return CommonResult.success(workspace2permissionService.grantToWorkspace(dtoList));
    }

    @PostMapping("/grant/user/all")
//    @PreAuthorize("hasAuthority('PermissionController:grantAllPermissionsToUser')")
    @Operation(summary = "给用户授权所有权限")
    public CommonResult<List<User2permission>> grantAllPermissionsToUser(@RequestBody java.util.Map<String, String> params) {
        String userId = params.get("userId");
        if (userId == null || userId.isEmpty()) {
            throw new me.chr.hex.core.R.Response.BizException("用户ID[userId]不能为空");
        }
        return CommonResult.success(user2permissionService.grantAllPermissionsToUser(userId));
    }
}
