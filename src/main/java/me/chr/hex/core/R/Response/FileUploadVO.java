package me.chr.hex.core.R.Response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.chr.hex.core.log.Loggable;

/**
 * @Author: CHR
 * @Date: create in 2026/3/9
 **/
@Data
@Schema(name = "FileUploadVO", description = "文件上传返回对象")
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class FileUploadVO{

    @Schema(description = "文件 url地址")
    private String url;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件大小")
    private String fileSize;

    @Schema(description = "文件分区")
    private String partition;

    @Schema(description = "文件对象名称")
    private String objectName;

}
