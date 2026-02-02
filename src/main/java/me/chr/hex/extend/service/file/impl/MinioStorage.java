package me.chr.hex.extend.service.file.impl;


import io.minio.*;
import io.minio.errors.MinioException;
import lombok.SneakyThrows;
import me.chr.hex.extend.properties.minio.MinioProperties;
import me.chr.hex.extend.service.file.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/
@Service
public class MinioStorage implements FileStorage {

    @Autowired
    private MinioProperties minioProperties;


    @SneakyThrows
    @Override
    public Boolean store(MultipartFile file,String pathName)  {
        // 1. 从配置对象获取参数，初始化 MinioClient
        MinioClient minioClient=this.getMinioClient();

        // 2. 文件名与后缀
        String originalFilename = file.getOriginalFilename();

        try {
            // 4. 检查桶是否存在，不存在则创建
            this.validatorBucket(minioClient);

            // 5. 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .object(pathName+originalFilename)
                                .stream(inputStream, file.getSize(), -1) // -1 表示自动分片
                                .contentType(file.getContentType()) // 保留原始 MIME 类型
                                .build()
                );
            }
            return true;
        } catch (MinioException e) {
            throw new Exception("MinIO 上传失败: " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    private void validatorBucket(MinioClient minioClient){
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build()
        );
        if (!bucketExists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build()
            );
        }
    }

    @Override
    public String getStorageType() {
        return "minio";
    }

    @Override
    @SneakyThrows
    public void delete(String pathName) {
        if (pathName == null || pathName.trim().isEmpty()) {
            throw new IllegalArgumentException("pathName不能为空");
        }
        MinioClient minioClient=this.getMinioClient();

        try {
            // 删除单个对象
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(pathName)
                            .build()
            );
        } catch (MinioException e) {
            throw new RuntimeException("MinIO 删除失败: " + e.getMessage(), e);
        }

    }

    private MinioClient getMinioClient(){
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        return minioClient;
    }


    @Override
    public byte[] download(String pathName){
        if (pathName == null || pathName.trim().isEmpty()) {
            throw new IllegalArgumentException("pathName 不能为空");
        }

        MinioClient minioClient = this.getMinioClient();

        // 获取文件流
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(pathName)
                        .build()
        )) {
            // 流转为字节数组返回
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("MinIO 下载失败: " + e.getMessage(),e);
        }
    }
}
