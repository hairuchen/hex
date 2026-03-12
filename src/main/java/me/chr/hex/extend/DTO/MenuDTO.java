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
import me.chr.hex.general.entity.Menu;

import java.util.UUID;

/**
 * @Author: CHR
 * @Date: create in 2026/3/13
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MenuDTO", description = "菜单请求 DTO对象")
public class MenuDTO implements Loggable {

    @NotBlank(message = "菜单名称[menuName]不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户管理")
    private String menuName;

    @Size(max = 100, message = "控制器名称长度不能超过100个字符")
    @Schema(description = "控制器名称 (Controller Class Name)", example = "UserController")
    @NotBlank(message = "菜单名称[controllerName]不能为空")
    private String controllerName;

    @Size(max = 100, message = "方法名称长度不能超过100个字符")
    @Schema(description = "方法名称 (Function/Method Name)", example = "listUsers")
    @NotBlank(message = "菜单名称[functionName]不能为空")
    private String functionName;

    @Schema(description = "父节点ID (UUID)，根节点可为空", example = "550e8400-e29b-41d4-a716-446655440001")
    private String parentId;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注信息", example = "用于管理系统用户")
    private String remark;

    public Menu ToMenu(){
        Menu menu=new Menu();
        menu.setId(UUID.randomUUID().toString());
        menu.setMenuName(menuName);
        menu.setControllerName(controllerName);
        menu.setFunctionName(functionName);
        menu.setParentId(parentId);
        menu.setRemark(remark);
        return menu;
    }
}
