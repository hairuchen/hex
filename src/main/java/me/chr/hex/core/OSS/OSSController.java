package me.chr.hex.core.OSS;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.OSS.service.OssService;
import me.chr.hex.core.R.Request.FileUploadDTO;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.core.R.Response.FileUploadVO;
import me.chr.hex.core.R.Response.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: CHR
 * @Date: create in 2026/3/9
 **/
@Slf4j
@RestController
@RequestMapping("/oss")
@Tag(name = "文件管理")
public class OSSController {

    @Autowired
    private OssService ossService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public CommonResult<FileUploadVO> upload(@Validated @ModelAttribute FileUploadDTO dto) {
        try {
            return CommonResult.success(ossService.upload(dto.getFile(),dto.getPartition()));
        } catch (Exception e) {
            return CommonResult.failure(ResultCode.INTERNAL_SERVER_ERROR,new FileUploadVO());
        }
    }
}
