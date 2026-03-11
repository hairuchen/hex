package me.chr.hex.extend.service.login;


import me.chr.hex.extend.DTO.LoginDTO;
import me.chr.hex.extend.VO.LoginVO;

/**
 * @Author: CHR
 * @Date: create in 2026/3/12
 **/
public interface LoginService {
    /**
     * 登录逻辑
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 登出逻辑
     */
    Boolean logout();
}
