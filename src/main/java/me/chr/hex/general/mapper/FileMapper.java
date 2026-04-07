package me.chr.hex.general.mapper;

import me.chr.hex.general.entity.File;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件元数据表（存储文件基础信息+处理状态） Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {

}

