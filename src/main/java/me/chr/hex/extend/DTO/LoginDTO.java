package me.chr.hex.extend.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.chr.hex.core.log.Loggable;

/**
 * @Author: CHR
 * @Date: create in 2026/3/12
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class LoginDTO implements Loggable {
    @Schema(description = "账号")
    private String username;

    @Schema(description = "密码")
    private String password;
}
