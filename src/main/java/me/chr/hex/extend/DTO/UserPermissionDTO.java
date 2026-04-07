package me.chr.hex.extend.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.chr.hex.core.log.Loggable;
import me.chr.hex.general.entity.User2permission;

import java.util.UUID;

/**
 * 用户授权 DTO
 *
 * @Author: CHR
 * @Date: 2026/04/11
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(name = "UserPermissionDTO", description = "用户授权请求 DTO")
public class UserPermissionDTO implements Loggable {

    @NotBlank(message = "用户ID[userId]不能为空")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @NotBlank(message = "权限ID[permissionId]不能为空")
    @Schema(description = "权限ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String permissionId;

    public User2permission toUser2permission(String tenantId, String creatorId) {
        User2permission user2permission = new User2permission();
        user2permission.setId(UUID.randomUUID().toString());
        user2permission.setTenantId(tenantId);
        user2permission.setUserId(userId);
        user2permission.setPermissionId(permissionId);
        user2permission.setCreatorId(creatorId);
        return user2permission;
    }
}
