package me.chr.hex.extend.service.file;


/**
 * @Author: CHR
 * @Date: create in 2026/2/2
 **/

import com.baomidou.mybatisplus.extension.service.IService;
import me.chr.hex.extend.BO.FileEntity;
import me.chr.hex.general.entity.TFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 文件数据持久化接口（适配PostgreSQL/MySQL等）
 */
public interface FileRepository extends IService<TFile> {
    /**
     * 保存文件元数据到数据库
     * @param file 上传的文件
     * @param pathName 存储路径，建议为用户唯一id+时间，示例：userID/time/，使用需要补全文件名
     * @param date 处理时间
     * @param userId 操作用户id
     * @return 持久化后的文件实体（含UUID主键）
     */
    FileEntity save(MultipartFile file, String pathName, Date date, String userId);

    /**
     * 更新进度状态
     */
    void updateProcess(String fileId,Short process);
}
