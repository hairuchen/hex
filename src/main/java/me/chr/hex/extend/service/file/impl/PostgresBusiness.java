package me.chr.hex.extend.service.file.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.chr.hex.extend.BO.FileEntity;
import me.chr.hex.extend.properties.minio.MinioProperties;
import me.chr.hex.extend.service.file.FileRepository;
import me.chr.hex.general.entity.TFile;
import me.chr.hex.general.mapper.TFileMapper;
import me.chr.hex.general.service.ITFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/
@Service
public class PostgresBusiness extends ServiceImpl<TFileMapper, TFile> implements FileRepository,IService<TFile> {

    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private ITFileService fileService;

    @Override
    public FileEntity save(MultipartFile file, String pathName, Date date, String userId) {
        FileEntity fileEntity=new FileEntity(file,minioProperties.getBucketName(),pathName+file.getOriginalFilename(),date,userId);
        fileService.save(fileEntity);
        return fileEntity;
    }

    @Override
    public void updateProcess(String fileId, Short process) {
        TFile fileEntity=fileService.getOne(new LambdaQueryWrapper<TFile>().eq(TFile::getId,fileId));
        fileEntity.setProcessProgress(process);
        fileService.updateById(fileEntity);
    }
}
