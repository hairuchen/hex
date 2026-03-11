package me.chr.hex.general.mapper;

import me.chr.hex.general.entity.User2dept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户与部门的归属关系表 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2026-03-12
 */
@Mapper
public interface User2deptMapper extends BaseMapper<User2dept> {

}

