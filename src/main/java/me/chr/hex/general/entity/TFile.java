package me.chr.hex.general.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * <p>
 * 文件元数据表（存储文件基础信息+处理状态）
 * </p>
 *
 * @author chr
 * @since 2026-02-02
 */
@Getter
@Setter
@ToString
@TableName("hex_t_file")
@Schema(name = "TFile", description = "文件元数据表（存储文件基础信息+处理状态）")
public class TFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件唯一标识（主键）
     */
    @Schema(description = "文件唯一标识（主键）")
    private Object id;

    /**
     * 原始文件名（如test.txt）
     */
    @Schema(description = "原始文件名（如test.txt）")
    private String fileName;

    /**
     * MinIO存储桶名称
     */
    @Schema(description = "MinIO存储桶名称")
    private String minioBucket;

    /**
     * MinIO中文件的完整对象路径（如file/xxx-xxx-xxx.txt）
     */
    @Schema(description = "MinIO中文件的完整对象路径（如file/xxx-xxx-xxx.txt）")
    private String minioObjectPath;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    /**
     * 文件类型（如txt、pdf、jpg，MIME类型）
     */
    @Schema(description = "文件类型（如txt、pdf、jpg，MIME类型）")
    private String fileType;

    /**
     * 文件上传时间
     */
    @Schema(description = "文件上传时间")
    private Date uploadTime;

    /**
     * 上传人（用户名/用户ID）
     */
    @Schema(description = "上传人（用户名/用户ID）")
    private String uploadUser;

    /**
     * 文件处理状态：1-待处理 2-处理中 3-处理成功 4-处理失败 5-已删除
     */
    @Schema(description = "文件处理状态：1-待处理 2-处理中 3-处理成功 4-处理失败 5-已删除")
    private Short status;

    /**
     * 文件处理进度（0-100，仅处理中状态有效）
     */
    @Schema(description = "文件处理进度（0-100，仅处理中状态有效）")
    private Short processProgress;

    /**
     * 处理失败原因（仅处理失败状态有效）
     */
    @Schema(description = "处理失败原因（仅处理失败状态有效）")
    private String failReason;
}
