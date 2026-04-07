package me.chr.hex.general.mapper;

import me.chr.hex.general.entity.Tenant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 租户表（系统顶层隔离边界，无删除操作，通过状态控制） Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {

}

