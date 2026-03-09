package me.chr.hex.core.OSS.service.impl;


import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.core.OSS.service.FileValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/
@Slf4j
@Service
public class FileValidatorImpl implements FileValidator {

    @Override
    public void validate(MultipartFile file) throws BizException {
        log.info("文件校验完成！");
    }

}
