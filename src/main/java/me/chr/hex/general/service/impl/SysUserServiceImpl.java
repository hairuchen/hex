package me.chr.hex.general.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.core.security.SecurityConfig;
import me.chr.hex.extend.DTO.UserDTO;
import me.chr.hex.general.entity.SysUser;
import me.chr.hex.general.entity.Tenant;
import me.chr.hex.general.mapper.SysUserMapper;
import me.chr.hex.general.mapper.TenantMapper;
import me.chr.hex.general.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantMapper tenantMapper;

    @Value("${tenant.username}")
    private String tenantUsername;

    @Value("${tenant.password}")
    private String tenantPassword;

    @Override
    public List<SysUser> batchCreate(List<UserDTO> userDTOList) {
        try {
            // 1.幂等校验
            Set<String> usernameSet = userDTOList.stream()
                    .map(UserDTO::getUsername)
                    .collect(Collectors.toSet());
            if (usernameSet.size()!=userDTOList.size()){
                throw new BizException("有重复数据项,请检查!");
            }
            // 2.数据库校验
            List<SysUser> existUserList = this.list(new LambdaQueryWrapper<SysUser>().in(SysUser::getUsername, usernameSet));
            if (!existUserList.isEmpty()) {
                String duplicateNames = existUserList.stream()
                        .map(SysUser::getUsername)
                        .collect(Collectors.joining(", "));
                throw new BizException("以下用户名已存在，无法重复创建: " + duplicateNames);
            }

            // 3.获取租户信息
            Tenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>().eq(Tenant::getUsername, tenantUsername));
            if (tenant == null) {
                throw new BizException("未找到对应的租户信息: " + tenantUsername);
            }

            // 4.密码校验
            if (!passwordEncoder.matches(tenantPassword, tenant.getPassword())) {
                throw new BizException("租户密码校验失败");
            }

            // 4.获取当前用户ID作为创建ID
            String creatorId = null;
            try {
                Jwt jwt = (Jwt) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
                creatorId = jwt.getClaim("jti");
            } catch (Exception e) {
                log.warn("无法获取当前用户ID，创建ID将为空: " + e.getMessage());
            }

            // 5.持久化
            List<SysUser> userList = new ArrayList<>();
            for (UserDTO userDTO : userDTOList) {
                String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());
                SysUser user = userDTO.ToUser(encryptedPassword, creatorId, tenant.getId());
                userList.add(user);
            }
            this.saveBatch(userList);
            return userList;
        }catch (Exception e){
            log.error("创建用户失败!"+e);
            throw new BizException("创建用户失败!");
        }
    }
}
