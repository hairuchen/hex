package me.chr.hex.general.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.DTO.UserPermissionDTO;
import me.chr.hex.general.entity.Tenant;
import me.chr.hex.general.entity.User2permission;
import me.chr.hex.general.mapper.TenantMapper;
import me.chr.hex.general.mapper.User2permissionMapper;
import me.chr.hex.general.service.IUser2permissionService;
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
public class User2permissionServiceImpl extends ServiceImpl<User2permissionMapper, User2permission> implements IUser2permissionService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private me.chr.hex.general.mapper.SysPermissionMapper sysPermissionMapper;

    @Value("${tenant.username}")
    private String tenantUsername;

    @Value("${tenant.password}")
    private String tenantPassword;

    @Override
    public List<User2permission> grantToUser(List<UserPermissionDTO> dtoList) {
        try {
            // 1.幂等校验
            for (UserPermissionDTO dto : dtoList) {
                Long count = this.count(new LambdaQueryWrapper<User2permission>()
                        .eq(User2permission::getUserId, dto.getUserId())
                        .eq(User2permission::getPermissionId, dto.getPermissionId()));
                if (count > 0) {
                    throw new BizException("用户已拥有该权限，不能重复授权");
                }
            }

            // 2.获取租户信息并校验密码
            Tenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>().eq(Tenant::getUsername, tenantUsername));
            if (tenant == null) {
                throw new BizException("未找到对应的租户信息: " + tenantUsername);
            }
            if (!passwordEncoder.matches(tenantPassword, tenant.getPassword())) {
                throw new BizException("租户密码校验失败");
            }

            // 3.获取当前用户ID作为创建ID
            String creatorId = null;
            try {
                Jwt jwt = (Jwt) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
                creatorId = jwt.getClaim("jti");
            } catch (Exception e) {
                log.warn("无法获取当前用户ID，创建ID将为空: " + e.getMessage());
            }

            // 4.持久化
            List<User2permission> result = new ArrayList<>();
            for (UserPermissionDTO dto : dtoList) {
                result.add(dto.toUser2permission(tenant.getId(), creatorId));
            }
            this.saveBatch(result);
            return result;
        } catch (Exception e) {
            log.error("给用户授权失败!" + e);
            throw new BizException("给用户授权失败!");
        }
    }

    @Override
    public List<User2permission> grantAllPermissionsToUser(String userId) {
        try {
            // 1.获取租户信息并校验密码
            Tenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>().eq(Tenant::getUsername, tenantUsername));
            if (tenant == null) {
                throw new BizException("未找到对应的租户信息: " + tenantUsername);
            }
            if (!passwordEncoder.matches(tenantPassword, tenant.getPassword())) {
                throw new BizException("租户密码校验失败");
            }

            // 2.获取当前用户ID作为创建ID
            String creatorId = null;
            try {
                Jwt jwt = (Jwt) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
                creatorId = jwt.getClaim("jti");
            } catch (Exception e) {
                log.warn("无法获取当前用户ID，创建ID将为空: " + e.getMessage());
            }

            // 3.查询该租户下的所有权限
            List<me.chr.hex.general.entity.SysPermission> allPermissions = sysPermissionMapper.selectList(
                    new LambdaQueryWrapper<me.chr.hex.general.entity.SysPermission>()
                            .eq(me.chr.hex.general.entity.SysPermission::getTenantId, tenant.getId()));
            if (allPermissions.isEmpty()) {
                return new ArrayList<>();
            }

            // 4.查询用户已有的权限ID
            List<User2permission> existingPermissions = this.list(
                    new LambdaQueryWrapper<User2permission>()
                            .eq(User2permission::getUserId, userId)
                            .eq(User2permission::getTenantId, tenant.getId()));
            List<String> existingPermissionIds = existingPermissions.stream()
                    .map(User2permission::getPermissionId)
                    .toList();

            // 5.过滤出用户没有的权限
            List<me.chr.hex.general.entity.SysPermission> newPermissions = allPermissions.stream()
                    .filter(p -> !existingPermissionIds.contains(p.getId()))
                    .toList();

            if (newPermissions.isEmpty()) {
                log.info("用户 [{}] 已拥有该租户下的所有权限，无需重复授权", userId);
                return existingPermissions;
            }

            // 6.给用户授权
            List<User2permission> result = new ArrayList<>();
            for (me.chr.hex.general.entity.SysPermission permission : newPermissions) {
                User2permission user2permission = new User2permission();
                user2permission.setId(java.util.UUID.randomUUID().toString());
                user2permission.setTenantId(tenant.getId());
                user2permission.setUserId(userId);
                user2permission.setPermissionId(permission.getId());
                user2permission.setCreatorId(creatorId);
                result.add(user2permission);
            }
            this.saveBatch(result);

            // 7.返回所有权限（已有的 + 新授权的）
            result.addAll(existingPermissions);
            return result;
        } catch (Exception e) {
            log.error("给用户授权所有权限失败!" + e);
            throw new BizException("给用户授权所有权限失败!");
        }
    }
}
