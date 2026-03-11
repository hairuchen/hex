package me.chr.hex.general.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.OSS.service.OssService;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.BO.FileEntity;
import me.chr.hex.extend.DTO.FileUploadDTO;
import me.chr.hex.extend.service.MessageProducer;
import me.chr.hex.general.entity.File;
import me.chr.hex.general.mapper.FileMapper;
import me.chr.hex.general.service.IFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 文件元数据表（存储文件基础信息+处理状态） 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-03-09
 */
@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

    @Autowired
    private OssService ossService;

    @Override
    public FileEntity uploadFile(FileUploadDTO file) {
        try {
            if (!ossService.exists(file.getPartition(), file.getObjectName())){
                throw new BizException("请调用OSS服务处理文件!");
            }
            if (this.exists(new LambdaQueryWrapper<File>().eq(File::getMinioObjectName,file.getObjectName()))){
                throw new BizException("请勿重复上传!");
            }
            // 持久化
            FileEntity fileEntity=new FileEntity(file.getFileName(),Long.valueOf(file.getFileSize()),file.getPartition(),file.getObjectName(),file.getUrl(),"chr",file.getIsExcavate());
            this.save(fileEntity);
            log.info("文件上传成功，FileID:{}", fileEntity.getId());

            return fileEntity;
        } catch (Exception e) {
            log.error("文件上传失败!\n"+e);
            throw new BizException("文件上传失败!");
        }
    }

    @Override
    public void updateProcess(String fileId, Short process) {
        File fileEntity=this.getOne(new LambdaQueryWrapper<File>().eq(File::getId,fileId));
        fileEntity.setProcessProgress(process);
        this.updateById(fileEntity);
    }
}
