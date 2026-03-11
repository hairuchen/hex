package me.chr.hex.extend.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.extend.DTO.UserDTO;
import me.chr.hex.general.entity.User;
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
 * @Date: create in 2026/3/11
 **/
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('UserController:batchCreate')")
    public CommonResult<List<User>> batchCreate(@Validated @NotEmpty(message = "用户列表[userDTOList]不能为空") @RequestBody List<@Valid UserDTO> userDTOList) {
        return CommonResult.success(userService.batchCreate(userDTOList));
    }

}
