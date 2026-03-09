package me.chr.hex.core.OSS.service;


import me.chr.hex.core.R.Response.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: CHR
 * @Date: create in 2026/3/9
 **/
public interface OssService {

    String getOssType();

    FileUploadVO upload(MultipartFile file, String partition);

    /**
     * 删除文件
     */
    void delete(String pathName);

    /**
     * 从存储介质下载文件，返回字节数组
     * @param partition 文件分区
     * @param objectName 文件对象名称
     * @return 文件字节数组
     */
    byte[] download(String partition,String objectName);

    boolean exists(String bucketName, String objectName);
}
