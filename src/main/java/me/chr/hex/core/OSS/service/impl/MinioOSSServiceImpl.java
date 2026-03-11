package me.chr.hex.core.OSS.service.impl;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.OSS.service.FileValidator;
import me.chr.hex.core.OSS.service.OssService;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.core.R.Response.FileUploadVO;
import me.chr.hex.extend.properties.minio.MinioProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @Author: CHR
 * @Date: create in 2026/3/9
 **/
@Slf4j
@Service
public class MinioOSSServiceImpl implements OssService {
    @Autowired
    private MinioProperties minioProperties;
    @Autowired
    private FileValidator fileValidator;

    @Override
    public String getOssType() {
        return "Minio OSS";
    }

    @Override
    @Transactional
    public FileUploadVO upload(MultipartFile file, String bucket) {
        log.info("OSS 存储服务由: {} 提供",this.getOssType());
        /*============================构建文件============================*/
        // 获取文件基本信息
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀（如 .jpg、.pdf）
        String suffix = StringUtils.isBlank(originalFilename) ? "" :
                originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成唯一文件名（避免重复）
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;

        /*============================构建minio============================*/
        MinioClient minioClient=this.getMinioClient();
        this.validatorBucket(minioClient,bucket);

        /*============================存储============================*/
        try (InputStream inputStream = file.getInputStream()){
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName) // 存储到 MinIO 的文件名
                            .stream(file.getInputStream(), file.getSize(), -1) // 文件流 + 大小
                            .contentType(file.getContentType()) // 文件类型
                            .build()
            );
//            return minioProperties.getEndpoint() + "/" + bucket + "/" + objectName;
            String url=minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(3600) // URL有效期，单位：秒
                            .build());
            log.info("文件: {},存储成功: {} ",originalFilename,url);
            return new FileUploadVO(url,originalFilename,String.valueOf(file.getSize()),bucket,objectName);
        } catch (ErrorResponseException | ServerException | InsufficientDataException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            log.error("文件上传到 "+this.getOssType()+" 失败!\n"+e);
            throw new BizException("文件上传到 "+this.getOssType()+" 失败!");
        }
    }

    private MinioClient getMinioClient(){
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    @SneakyThrows
    private void validatorBucket(MinioClient minioClient,String bucket){
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build()
        );
        if (!bucketExists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucket).build()
            );
        }
    }



    @Override
    @SneakyThrows
    public void delete(String pathName) {
        if (pathName == null || pathName.trim().isEmpty()) {
            throw new IllegalArgumentException("pathName 不能为空");
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

    @Override
    public byte[] download(String partition,String objectName){
        if (partition == null || partition.trim().isEmpty()) {
            throw new IllegalArgumentException("文件分区 partition 不能为空");
        }
        if (objectName == null || objectName.trim().isEmpty()) {
            throw new IllegalArgumentException("对象名称 objectName 不能为空");
        }

        MinioClient minioClient = this.getMinioClient();

        // 获取文件流
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(partition)
                        .object(objectName)
                        .build()
        )) {
            // 流转为字节数组返回
            byte[] fileBytes = inputStream.readAllBytes();
            log.info("{} OSS服务下载成功,分区: {},名称: {},大小: {} byte",this.getOssType(),partition,objectName,fileBytes.length);
            return fileBytes;
        } catch (Exception e) {
            throw new RuntimeException(this.getOssType()+" 服务 下载文件失败,分区: "+partition+"名称:"+objectName+e);
        }
    }

    @Override
    public boolean exists(String bucketName, String objectName) {
        try {
            this.getMinioClient().statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true; // 文件存在
        } catch (Exception e) {
            return false;
        }
    }


}
