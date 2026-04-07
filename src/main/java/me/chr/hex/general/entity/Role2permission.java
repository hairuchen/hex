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
@TableName("role2permission")
@Schema(name = "Role2permission", description = "")
@JsonPropertyOrder({
    "id", 
    "tenantId", 
    "roleId", 
    "permissionId", 
    "creatorId", 
    "createTime"
})
public class Role2permission implements Serializable {

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
    @TableField("role_id")
    private String roleId;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("permission_id")
    private String permissionId;


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
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;



}