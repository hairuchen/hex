package me.chr.hex.extend.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.FileUploadVO;
import me.chr.hex.core.log.Loggable;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: CHR
 * @Date: create in 2026/3/6
 **/
@EqualsAndHashCode(callSuper = true)
@Slf4j
@NoArgsConstructor
@Schema(name = "FileUploadDTO", description = "文件上传请求对象")
@Data
public class FileUploadDTO extends FileUploadVO implements Loggable {

    @Schema(description = "是否开始解析")
    private Boolean isParse;

    @Schema(description = "是否开启数据挖掘")
    private Boolean isExcavate;

    @Override
    public String toString() {
        return "FileUploadDTO{"+super.toString()+"}";
    }
}
