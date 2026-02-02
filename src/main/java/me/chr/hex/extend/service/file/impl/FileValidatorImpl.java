package me.chr.hex.extend.service.file.impl;


import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.service.file.FileValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/
@Service
public class FileValidatorImpl implements FileValidator {

    @Override
    public void validate(MultipartFile file) throws BizException {

    }

}
