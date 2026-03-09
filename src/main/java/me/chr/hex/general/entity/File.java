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


/**
 * <p>
 * 文件元数据表（存储文件基础信息+处理状态）
 * </p>
 *
 * @author baomidou
 * @since 2026-03-10
 */
@Data
@ToString
@TableName("file")
@Schema(name = "File", description = "文件元数据表（存储文件基础信息+处理状态）")
@JsonPropertyOrder({
    "id", 
    "fileName", 
    "minioBucket", 
    "minioObjectName", 
    "minioImgPath", 
    "fileSize", 
    "fileType", 
    "uploadTime", 
    "uploadUser", 
    "status", 
    "processProgress", 
    "failReason", 
    "isExcavate"
})
public class File implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 文件唯一标识（主键）
     */
    @Schema(description = "文件唯一标识（主键）")
    @TableField("id")
    @TableId(value = "id")
    private String id;


    /**
     * 原始文件名（如test.txt）
     */
    @Schema(description = "原始文件名（如test.txt）")
    @TableField("file_name")
    private String fileName;


    /**
     * MinIO存储桶名称
     */
    @Schema(description = "MinIO存储桶名称")
    @TableField("minio_bucket")
    private String minioBucket;


    /**
     * MinIO文件对象名称
     */
    @Schema(description = "MinIO文件对象名称")
    @TableField("minio_object_name")
    private String minioObjectName;


    /**
     * MinIO中文件的访问路径
     */
    @Schema(description = "MinIO中文件的访问路径")
    @TableField("minio_img_path")
    private String minioImgPath;


    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）")
    @TableField("file_size")
    private Long fileSize;


    /**
     * 文件类型（如txt、pdf、jpg，MIME类型）
     */
    @Schema(description = "文件类型（如txt、pdf、jpg，MIME类型）")
    @TableField("file_type")
    private String fileType;


    /**
     * 文件上传时间
     */
    @Schema(description = "文件上传时间")
    @TableField("upload_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime uploadTime;


    /**
     * 上传人（用户名/用户ID）
     */
    @Schema(description = "上传人（用户名/用户ID）")
    @TableField("upload_user")
    private String uploadUser;


    /**
     * 文件处理状态：1-待处理 2-处理中 3-处理成功 4-处理失败 5-已删除
     */
    @Schema(description = "文件处理状态：1-待处理 2-处理中 3-处理成功 4-处理失败 5-已删除")
    @TableField("status")
    private Short status;


    /**
     * 文件处理进度（0-100，仅处理中状态有效）
     */
    @Schema(description = "文件处理进度（0-100，仅处理中状态有效）")
    @TableField("process_progress")
    private Short processProgress;


    /**
     * 处理失败原因（仅处理失败状态有效）
     */
    @Schema(description = "处理失败原因（仅处理失败状态有效）")
    @TableField("fail_reason")
    private String failReason;


    /**
     * 
     */
    @Schema(description = "")
    @TableField("is_excavate")
    private Boolean isExcavate;



}