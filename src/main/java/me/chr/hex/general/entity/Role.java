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
 * 角色实体表
 * </p>
 *
 * @author baomidou
 * @since 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("\"role\"")
@Schema(name = "Role", description = "角色实体表")
@JsonPropertyOrder({
    "id", 
    "roleName", 
    "status", 
    "createTime", 
    "updateTime", 
    "isDeleted", 
    "version", 
    "parentId", 
    "remark"
})
public class Role implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 角色ID（UUID）
     */
    @Schema(description = "角色ID（UUID）")
    @TableField("id")
    @TableId(value = "id")
    private String id;


    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    @TableField("role_name")
    private String roleName;


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
     * 父节点-父级角色ID
     */
    @Schema(description = "父节点-父级角色ID")
    @TableField("parent_id")
    private String parentId;


    /**
     * 备注
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;



}