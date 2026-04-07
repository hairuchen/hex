package me.chr.hex.extend.DTO;


import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.chr.hex.core.log.Loggable;
import me.chr.hex.extend.Enum.KnowledgeNodeTypeEnum;
import me.chr.hex.general.entity.SysUser;

import java.util.UUID;

/**
 * @Author: CHR
 * @Date: create in 2026/3/11
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
@Schema(name = "UserDTO", description = "用户请求 DTO对象")
public class UserDTO implements Loggable {

    /**
     * 用户名（登录账号）
     */
    @Schema(description = "用户名（登录账号/手机号）")
    @NotBlank(message = "用户名[username]不能为空")
    private String username;

    /**
     * 密码（加密存储，如BCrypt）
     */
    @Schema(description = "密码（加密存储，如BCrypt）")
    private String password;

    public SysUser ToUser(String encryptedPassword, String creatorId, String tenantId){
        SysUser user=new SysUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(this.username);
        user.setPassword(encryptedPassword);
        user.setTenantId(tenantId);
        user.setCreatorId(creatorId);
        return user;
    }

}
