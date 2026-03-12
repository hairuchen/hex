package me.chr.hex.extend.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.extend.BO.FileEntity;
import me.chr.hex.extend.DTO.FileUploadDTO;

import me.chr.hex.extend.service.MessageProducer;
import me.chr.hex.general.entity.File;
import me.chr.hex.general.service.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: CHR
 * @Date: create in 2026/1/29
 **/
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private IFileService fileService;

    @Autowired
    private MessageProducer messageProducer;

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('FileController:uploadFile')")
    @Operation(summary = "上传文件")
    public CommonResult<FileEntity> uploadFile(@RequestBody @Valid FileUploadDTO fileUploadDTO) {
        FileEntity fileEntity=fileService.uploadFile(fileUploadDTO);
        // 发送消息（调用接口）
        if (fileUploadDTO.getIsParse()!=null&& fileUploadDTO.getIsParse()){
            messageProducer.sendFileParseMessage(fileEntity);
            return CommonResult.success("文件已提交，正在后台解析处理...",fileEntity);
        }

        return CommonResult.success(fileEntity);
    }
}
