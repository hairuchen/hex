package me.chr.hex.extend.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.chr.hex.core.log.Loggable;
import me.chr.hex.general.entity.Workspace2permission;

import java.util.UUID;

/**
 * 工作空间授权 DTO
 *
 * @Author: CHR
 * @Date: 2026/04/11
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(name = "WorkspacePermissionDTO", description = "工作空间授权请求 DTO")
public class WorkspacePermissionDTO implements Loggable {

    @NotBlank(message = "工作空间ID[workspaceId]不能为空")
    @Schema(description = "工作空间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String workspaceId;

    @NotBlank(message = "权限ID[permissionId]不能为空")
    @Schema(description = "权限ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String permissionId;

    public Workspace2permission toWorkspace2permission(String tenantId, String creatorId) {
        Workspace2permission workspace2permission = new Workspace2permission();
        workspace2permission.setId(UUID.randomUUID().toString());
        workspace2permission.setTenantId(tenantId);
        workspace2permission.setWorkspaceId(workspaceId);
        workspace2permission.setPermissionId(permissionId);
        workspace2permission.setCreatorId(creatorId);
        return workspace2permission;
    }
}
