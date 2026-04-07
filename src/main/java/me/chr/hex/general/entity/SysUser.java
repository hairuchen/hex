package me.chr.hex.general.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import java.io.Serial;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * <p>
 * 
 * </p>
 *
 * @author baomidou
 * @since 2026-04-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("sys_user")
@Schema(name = "SysUser", description = "")
@JsonPropertyOrder({
    "id", 
    "tenantId", 
    "username", 
    "password", 
    "fullName", 
    "email", 
    "phone", 
    "avatar", 
    "lastLoginTime", 
    "lastLoginIp", 
    "status", 
    "createTime", 
    "updateTime", 
    "creatorId", 
    "remark"
})
public class SysUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 
     */
    @Schema(description = "")
    @TableId(value = "id")
    private String id;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("tenant_id")
    private String tenantId;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("username")
    private String username;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("password")
    private String password;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("full_name")
    private String fullName;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("email")
    private String email;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("phone")
    private String phone;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("avatar")
    private String avatar;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("last_login_ip")
    private String lastLoginIp;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("status")
    private Short status;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("creator_id")
    private String creatorId;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("remark")
    private String remark;



}