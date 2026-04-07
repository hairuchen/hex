package me.chr.hex.general.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.DTO.PermissionDTO;
import me.chr.hex.general.entity.SysPermission;
import me.chr.hex.general.entity.Tenant;
import me.chr.hex.general.mapper.SysPermissionMapper;
import me.chr.hex.general.mapper.TenantMapper;
import me.chr.hex.general.service.ISysPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 权限实体表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
@Slf4j
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantMapper tenantMapper;

    @Value("${tenant.username}")
    private String tenantUsername;

    @Value("${tenant.password}")
    private String tenantPassword;

    @Override
    public List<SysPermission> batchCreate(List<PermissionDTO> permissionDTOList) {
        try {
            Tenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>().eq(Tenant::getUsername, tenantUsername));
            if (tenant == null) {
                throw new BizException("未找到对应的租户信息: " + tenantUsername);
            }
            if (!passwordEncoder.matches(tenantPassword, tenant.getPassword())) {
                throw new BizException("租户密码校验失败");
            }
            String creatorId = null;
            try {
                Jwt jwt = (Jwt) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
                creatorId = jwt.getClaim("jti");
            } catch (Exception e) {
                log.warn("无法获取当前用户ID，创建ID将为空: " + e.getMessage());
            }
            // 持久化
            List<SysPermission> permissionList= new ArrayList<>();
            for (PermissionDTO permissionDTO : permissionDTOList) {
                SysPermission permission = permissionDTO.toSysPermission(tenant,creatorId);
                permissionList.add(permission);
            }
            this.saveBatch(permissionList);
            // 重新查询获取完整数据（包含数据库默认值和触发器填充的字段）
            List<String> ids = permissionList.stream().map(SysPermission::getId).collect(Collectors.toList());
            return this.listByIds(ids);
        }catch (Exception e){
            log.error("创建权限失败!"+e);
            throw new BizException("创建权限失败!");
        }
    }
}
