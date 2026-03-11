package me.chr.hex.extend.service.login.impl;


import me.chr.hex.extend.DTO.LoginDTO;
import me.chr.hex.extend.VO.LoginVO;
import me.chr.hex.extend.service.login.LoginService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @Author: CHR
 * 单端登录
 * @Date: create in 2026/3/12
 **/
@Service
@ConditionalOnProperty(name = "project.configuration.login.isSingle", havingValue = "true")
public class SingleLoginServiceImpl extends AbstractLoginServiceImpl implements LoginService {

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        return super.doLogin(loginDTO);
    }

    @Override
    public Boolean logout() {
        return null;
    }
}
