package me.chr.hex.extend.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.extend.DTO.UserDTO;
import me.chr.hex.general.entity.SysUser;
import me.chr.hex.general.service.ISysUserService;
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
 * @Date: create in 2026/3/11
 **/
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ISysUserService sysUserService;


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('UserController:batchCreate')")
    @Operation(summary = "创建用户")
    public CommonResult<List<SysUser>> batchCreate(@Validated @NotEmpty(message = "用户列表[userDTOList]不能为空") @RequestBody List<@Valid UserDTO> userDTOList) {
        return CommonResult.success(sysUserService.batchCreate(userDTOList));
    }

}
