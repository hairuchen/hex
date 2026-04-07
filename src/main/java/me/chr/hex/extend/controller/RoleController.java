package me.chr.hex.extend.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.extend.DTO.RoleDTO;
import me.chr.hex.general.entity.SysRole;
import me.chr.hex.general.service.ISysRoleService;
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
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private ISysRoleService sysRoleService;


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('RoleController:batchCreate')")
    @Operation(summary = "创建角色")
    public CommonResult<List<SysRole>> batchCreate(@Validated @NotEmpty(message = "角色列表[roleDTOList]不能为空") @RequestBody List<@Valid RoleDTO> roleDTOList) {
        return CommonResult.success(sysRoleService.batchCreate(roleDTOList));
    }
}
