package me.chr.hex.extend.service;


import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: CHR
 * @Date: create in 2026/2/2
 **/
public interface FileService {

    void uploadFile(MultipartFile file);
}
