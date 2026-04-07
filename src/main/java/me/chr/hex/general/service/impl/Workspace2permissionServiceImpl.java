package me.chr.hex.general.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.DTO.WorkspacePermissionDTO;
import me.chr.hex.general.entity.Tenant;
import me.chr.hex.general.entity.Workspace2permission;
import me.chr.hex.general.mapper.TenantMapper;
import me.chr.hex.general.mapper.Workspace2permissionMapper;
import me.chr.hex.general.service.IWorkspace2permissionService;
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
public class Workspace2permissionServiceImpl extends ServiceImpl<Workspace2permissionMapper, Workspace2permission> implements IWorkspace2permissionService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantMapper tenantMapper;

    @Value("${tenant.username}")
    private String tenantUsername;

    @Value("${tenant.password}")
    private String tenantPassword;

    @Override
    public List<Workspace2permission> grantToWorkspace(List<WorkspacePermissionDTO> dtoList) {
        try {
            // 1.幂等校验
            for (WorkspacePermissionDTO dto : dtoList) {
                Long count = this.count(new LambdaQueryWrapper<Workspace2permission>()
                        .eq(Workspace2permission::getWorkspaceId, dto.getWorkspaceId())
                        .eq(Workspace2permission::getPermissionId, dto.getPermissionId()));
                if (count > 0) {
                    throw new BizException("工作空间已拥有该权限，不能重复授权");
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
            List<Workspace2permission> result = new ArrayList<>();
            for (WorkspacePermissionDTO dto : dtoList) {
                result.add(dto.toWorkspace2permission(tenant.getId(), creatorId));
            }
            this.saveBatch(result);
            return result;
        } catch (Exception e) {
            log.error("给工作空间授权失败!" + e);
            throw new BizException("给工作空间授权失败!");
        }
    }
}
