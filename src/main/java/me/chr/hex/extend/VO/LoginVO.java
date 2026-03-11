package me.chr.hex.extend.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.User;

/**
 * @Author: CHR
 * @Date: create in 2026/3/12
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVO {

    @Schema(description = "用户对象")
    private User user;

    @Schema(description = "用户本次登录 Token令牌")
    private String token;

//    @Schema(description = "用户当前权限列表")
//    private UserPermission permission;
}
