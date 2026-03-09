package me.chr.hex.general.service;

import me.chr.hex.extend.BO.FileEntity;
import me.chr.hex.extend.DTO.FileUploadDTO;
import me.chr.hex.general.entity.File;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 文件元数据表（存储文件基础信息+处理状态） 服务类
 * </p>
 *
 * @author baomidou
 * @since 2026-03-09
 */
public interface IFileService extends IService<File> {

    FileEntity uploadFile(FileUploadDTO file);

    /**
     * 更新进度状态
     */
    void updateProcess(String fileId,Short process);

}
