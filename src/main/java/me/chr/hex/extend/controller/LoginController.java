package me.chr.hex.extend.controller;


import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.extend.DTO.LoginDTO;
import me.chr.hex.extend.VO.LoginVO;
import me.chr.hex.extend.service.login.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: CHR
 * @Date: create in 2026/3/12
 **/
@Slf4j
@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    @Operation(summary = "登录接口")
    public CommonResult<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return  CommonResult.success(loginService.login(loginDTO));
    }



    @PostMapping("/logout")
    @ResponseBody
    public CommonResult<Object> logout() {
        if (loginService.logout()){
            return CommonResult.success("登出成功");
        }else{
            return CommonResult.failure();
        }
    }
}
