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
import me.chr.hex.general.entity.SysRole;

import java.util.UUID;

/**
 * @Author: CHR
 * @Date: create in 2026/3/13
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "RoleDTO", description = "角色请求 DTO对象")
public class RoleDTO implements Loggable {

    @NotBlank(message = "角色名称[roleName]不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "管理员")
    private String roleName;

    @Schema(description = "父节点ID (UUID)，根节点可为空", example = "550e8400-e29b-41d4-a716-446655440001")
    private String parentId;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注信息", example = "系统管理员角色")
    private String remark;

    public SysRole toSysRole(){
        SysRole role = new SysRole();
        role.setId(UUID.randomUUID().toString());
        role.setRoleName(roleName);
        role.setParentId(parentId);
        role.setRemark(remark);
        return role;
    }
}
