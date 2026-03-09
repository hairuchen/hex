package me.chr.hex.core.R.Request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.chr.hex.core.log.Loggable;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: CHR
 * @Date: create in 2026/3/9
 **/
@Data
@Schema(name = "FileUploadRequestDTO", description = "文件上传请求对象")
@NoArgsConstructor
public class FileUploadDTO implements Loggable {

    @NotNull(message = "文件不能为空")
    @Schema(description = "上传的文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    @Schema(description = "文件目录/分区", example = "/hex", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分区目录不能为空")
    private String partition;

    @Override
    public String toString() {
        return "FileUploadDTO{" +
                "file=" + file.getOriginalFilename() +
                ", partition='" + partition + '\'' +
                '}';
    }
}
