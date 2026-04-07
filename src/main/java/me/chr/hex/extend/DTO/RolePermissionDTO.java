package me.chr.hex.extend.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.chr.hex.core.log.Loggable;
import me.chr.hex.general.entity.Role2permission;

import java.util.UUID;

/**
 * 角色授权 DTO
 *
 * @Author: CHR
 * @Date: 2026/04/11
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(name = "RolePermissionDTO", description = "角色授权请求 DTO")
public class RolePermissionDTO implements Loggable {

    @NotBlank(message = "角色ID[roleId]不能为空")
    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleId;

    @NotBlank(message = "权限ID[permissionId]不能为空")
    @Schema(description = "权限ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String permissionId;

    public Role2permission toRole2permission(String tenantId, String creatorId) {
        Role2permission role2permission = new Role2permission();
        role2permission.setId(UUID.randomUUID().toString());
        role2permission.setTenantId(tenantId);
        role2permission.setRoleId(roleId);
        role2permission.setPermissionId(permissionId);
        role2permission.setCreatorId(creatorId);
        return role2permission;
    }
}
