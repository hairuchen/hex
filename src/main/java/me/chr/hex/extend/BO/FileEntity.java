package me.chr.hex.extend.BO;


import me.chr.hex.general.entity.TFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/
public class FileEntity extends TFile {

    public FileEntity(){
        this.setId(UUID.randomUUID());
    }

    public FileEntity(MultipartFile file, String bucketName,String minioPath, Date date, String userId){
        this();
        this.setFileName(file.getOriginalFilename());
        this.setFileSize(file.getSize());
        this.setMinioObjectPath(minioPath);
        this.setMinioBucket(bucketName);

        this.setUploadTime(date);
        this.setUploadUser(userId);

        this.setProcessProgress((short) 0);
    }
}
