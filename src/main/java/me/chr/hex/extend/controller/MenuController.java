package me.chr.hex.extend.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.extend.DTO.MenuDTO;
import me.chr.hex.extend.DTO.UserDTO;
import me.chr.hex.general.entity.Menu;
import me.chr.hex.general.entity.User;
import me.chr.hex.general.service.IMenuService;
import me.chr.hex.general.service.IUserService;
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
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private IMenuService menuService;


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('MenuController:batchCreate')")
    @Operation(summary = "创建菜单")
    public CommonResult<List<Menu>> batchCreate(@Validated @NotEmpty(message = "菜单列表[menuDTOList]不能为空") @RequestBody List<@Valid MenuDTO> menuDTOList) {
        return CommonResult.success(menuService.batchCreate(menuDTOList));
    }
}
