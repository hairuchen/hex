package me.chr.hex.core.OSS.service;

import me.chr.hex.core.R.Response.BizException;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: CHR
 * @Date: create in 2026/2/2
 * 文件校验接口（可扩展不同校验规则）
 **/
public interface FileValidator {
    /**
     * 校验文件合法性
     * @param file 上传的文件
     * @throws BizException 校验失败抛出业务异常
     */
    void validate(MultipartFile file) throws BizException;
}
