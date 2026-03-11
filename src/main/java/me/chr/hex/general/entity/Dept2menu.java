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
 * 部门与权限的授权关系表
 * </p>
 *
 * @author baomidou
 * @since 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("\"dept2menu\"")
@Schema(name = "Dept2menu", description = "部门与权限的授权关系表")
@JsonPropertyOrder({
    "id", 
    "deptId", 
    "menuId", 
    "createTime"
})
public class Dept2menu implements Serializable {

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
     * 部门ID（关联department.id）
     */
    @Schema(description = "部门ID（关联department.id）")
    @TableField("dept_id")
    private String deptId;


    /**
     * 权限/菜单ID（关联menu.id）
     */
    @Schema(description = "权限/菜单ID（关联menu.id）")
    @TableField("menu_id")
    private String menuId;


    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;



}