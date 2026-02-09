package me.chr.hex.extend.service.impl;

import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.BO.FileEntity;
import me.chr.hex.extend.service.FileService;
import me.chr.hex.extend.service.file.FileRepository;
import me.chr.hex.extend.service.file.FileStorage;
import me.chr.hex.extend.service.file.FileValidator;
import me.chr.hex.extend.service.file.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: CHR
 * @Date: create in 2026/2/2
 **/
@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private FileValidator fileValidator;
    @Autowired
    private FileStorage fileStorage;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MessageProducer messageProducer;

    @Override
    public void uploadFile(MultipartFile file) {
        Date date=new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String pathName="userID/"+sdf.format(date)+"/";
        Boolean storageStatus = false;
        FileEntity fileEntity = null;
        try {
            // 文件校验（调用接口）
            fileValidator.validate(file);
            logger.info("文件校验通过，文件名：{}", file.getOriginalFilename());

            // 数据持久化（调用接口）
            fileEntity = fileRepository.save(file, pathName,date,"chr");
            logger.info("文件元数据持久化成功，FileID：{}", fileEntity.getId());

            // 文件存储（调用接口）
            storageStatus = fileStorage.store(file,fileEntity);
            logger.info("文件存储成功，存储类型：{}，存储状态：{}", fileStorage.getStorageType(), storageStatus);

            // 发送消息（调用接口）
            messageProducer.sendFileParseMessage(fileEntity);
            logger.info("待解析消息发送成功，MQ类型：{}", messageProducer.getMqType());

        } catch (Exception e) {
            logger.error("文件上传异常！");
            if (fileEntity!=null && !storageStatus){
                fileStorage.delete(pathName+fileEntity.getId());
                fileRepository.removeById(fileEntity.getId().toString());
            }
            throw new BizException("文件上传失败：" + e.getMessage());
        }
    }
}
