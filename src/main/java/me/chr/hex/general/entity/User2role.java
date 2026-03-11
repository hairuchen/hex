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
 * 用户与角色的分配关系表
 * </p>
 *
 * @author baomidou
 * @since 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("\"user2role\"")
@Schema(name = "User2role", description = "用户与角色的分配关系表")
@JsonPropertyOrder({
    "id", 
    "userId", 
    "roleId", 
    "createTime"
})
public class User2role implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 关系ID（UUID）
     */
    @Schema(description = "关系ID（UUID）")
    @TableField("id")
    @TableId(value = "id")
    private String id;


    /**
     * 用户ID（关联user.id）
     */
    @Schema(description = "用户ID（关联user.id）")
    @TableField("user_id")
    private String userId;


    /**
     * 角色ID（关联role.id）
     */
    @Schema(description = "角色ID（关联role.id）")
    @TableField("role_id")
    private String roleId;


    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;



}