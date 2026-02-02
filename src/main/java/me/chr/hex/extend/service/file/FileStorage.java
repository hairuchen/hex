package me.chr.hex.extend.service.file;


/**
 * @Author: CHR
 * @Date: create in 2026/2/2
 **/

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储接口（适配MinIO/阿里OSS/本地存储等）
 */
public interface FileStorage {
    /**
     * 存储文件到指定存储介质
     * @param file 上传的文件
     * @param pathName 存储路径，建议为用户唯一id+时间，示例：userID/time/
     * @return 存储后的唯一标识（如MinIO的objectPath/OSS的objectKey）
     */
    Boolean store(MultipartFile file,String pathName);

    /**
     * 获取存储介质类型（用于标识实现类）
     * @return 如MINIO/OSS/LOCAL
     */
    String getStorageType();

    /**
     * 删除文件
     */
    void delete(String pathName);

    /**
     * 从存储介质下载文件，返回字节数组
     * @param pathName 文件路径（objectPath）
     * @return 文件字节数组
     * @throws Exception 下载失败抛出异常
     */
    byte[] download(String pathName);
}
