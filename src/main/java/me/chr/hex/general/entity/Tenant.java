package me.chr.hex.general.entity;

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
 * 租户表（系统顶层隔离边界，无删除操作，通过状态控制）
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("\"tenant\"")
@Schema(name = "Tenant", description = "租户表（系统顶层隔离边界，无删除操作，通过状态控制）")
@JsonPropertyOrder({
    "id", 
    "tenantName", 
    "username", 
    "password", 
    "status", 
    "createTime", 
    "updateTime", 
    "remark"
})
public class Tenant implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 租户ID（UUID主键）
     */
    @Schema(description = "租户ID（UUID主键）")
    @TableId(value = "id")
    private String id;


    /**
     * 租户名称（企业/组织名称）
     */
    @Schema(description = "租户名称（企业/组织名称）")
    @TableField("tenant_name")
    private String tenantName;


    /**
     * 租户管理员账号（登录名）
     */
    @Schema(description = "租户管理员账号（登录名）")
    @TableField("username")
    private String username;


    /**
     * 租户管理员密码（加密存储）
     */
    @Schema(description = "租户管理员密码（加密存储）")
    @TableField("password")
    private String password;


    /**
     * 状态（1-启用，0-关闭）
     */
    @Schema(description = "状态（1-启用，0-关闭）")
    @TableField("status")
    private Short status;


    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;


    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;


    /**
     * 备注
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;



}