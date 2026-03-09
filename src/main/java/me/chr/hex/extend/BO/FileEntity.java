package me.chr.hex.extend.BO;


import me.chr.hex.general.entity.File;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/
public class FileEntity extends File {

    public FileEntity(){
        this.setId(UUID.randomUUID().toString());
    }

    public FileEntity(String fileName,Long fileSize, String bucketName,String objectName,String minioPath,String userId,Boolean isExcavate){
        this();
        this.setFileName(fileName);
        this.setFileSize(fileSize);

        this.setMinioBucket(bucketName);
        this.setMinioObjectName(objectName);
        this.setMinioImgPath(minioPath);

        this.setUploadTime(LocalDateTime.now());
        this.setUploadUser(userId);

        this.setProcessProgress((short) 0);
        this.setIsExcavate(isExcavate);
    }
}
