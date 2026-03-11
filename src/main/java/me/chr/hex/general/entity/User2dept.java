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
 * 用户与部门的归属关系表
 * </p>
 *
 * @author baomidou
 * @since 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("\"user2dept\"")
@Schema(name = "User2dept", description = "用户与部门的归属关系表")
@JsonPropertyOrder({
    "id", 
    "userId", 
    "deptId", 
    "createTime"
})
public class User2dept implements Serializable {

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
     * 部门ID（关联department.id）
     */
    @Schema(description = "部门ID（关联department.id）")
    @TableField("dept_id")
    private String deptId;


    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;



}