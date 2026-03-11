package me.chr.hex.general.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * 用户实体表
 * </p>
 *
 * @author baomidou
 * @since 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("\"user\"")
@Schema(name = "User", description = "用户实体表")
@JsonPropertyOrder({
    "id", 
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
    "isDeleted", 
    "version", 
    "parentId", 
    "remark"
})
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 用户ID（UUID）
     */
    @Schema(description = "用户ID（UUID）")
    @TableField("id")
    @TableId(value = "id")
    private String id;


    /**
     * 用户名（登录账号）
     */
    @Schema(description = "用户名（登录账号）")
    @TableField("username")
    private String username;


    /**
     * 密码（加密存储，如BCrypt）
     */
    @Schema(description = "密码（加密存储，如BCrypt）")
    @TableField("password")
    private String password;


    /**
     * 用户姓名（真实姓名）
     */
    @Schema(description = "用户姓名（真实姓名）")
    @TableField("full_name")
    private String fullName;


    /**
     * 电子邮箱
     */
    @Schema(description = "电子邮箱")
    @TableField("email")
    private String email;


    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    @TableField("phone")
    private String phone;


    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    @TableField("avatar")
    private String avatar;


    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;


    /**
     * 最后登录IP地址
     */
    @Schema(description = "最后登录IP地址")
    @TableField("last_login_ip")
    private String lastLoginIp;


    /**
     * 状态（0-禁用，1-正常，2-锁定）
     */
    @Schema(description = "状态（0-禁用，1-正常，2-锁定）")
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
     * 逻辑删除标识(0-正常，1-删除)
     */
    @Schema(description = "逻辑删除标识(0-正常，1-删除)")
    @TableField("is_deleted")
    private Short isDeleted;


    /**
     * 版本号
     */
    @Schema(description = "版本号")
    @TableField("version")
    private String version;


    /**
     * 父节点-创建人ID
     */
    @Schema(description = "父节点-创建人ID")
    @TableField("parent_id")
    private String parentId;


    /**
     * 备注
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;



}