package me.chr.hex.general.service.impl;

import me.chr.hex.general.entity.Role;
import me.chr.hex.general.mapper.RoleMapper;
import me.chr.hex.general.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色实体表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-03-11
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

}
