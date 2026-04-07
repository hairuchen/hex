package me.chr.hex.extend.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.chr.hex.core.log.Loggable;
import me.chr.hex.general.entity.Workspace;

import java.util.UUID;

/**
 * @Author: CHR
 * @Date: create in 2026/3/13
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "WorkspaceDTO", description = "工作空间请求 DTO对象")
public class WorkspaceDTO implements Loggable {

    @NotBlank(message = "工作空间名称[workspaceName]不能为空")
    @Size(max = 100, message = "工作空间名称长度不能超过100个字符")
    @Schema(description = "工作空间名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "研发团队")
    private String workspaceName;

    @Schema(description = "父节点ID (UUID)，根节点可为空", example = "550e8400-e29b-41d4-a716-446655440001")
    private String parentId;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注信息", example = "用于管理研发团队的工作空间")
    private String remark;

    public Workspace toWorkspace(){
        Workspace workspace = new Workspace();
        workspace.setId(UUID.randomUUID().toString());
        workspace.setWorkspaceName(workspaceName);
        workspace.setParentId(parentId);
        workspace.setRemark(remark);
        return workspace;
    }
}
