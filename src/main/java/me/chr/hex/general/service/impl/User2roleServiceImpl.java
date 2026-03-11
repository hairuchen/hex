package me.chr.hex.general.service.impl;

import me.chr.hex.general.entity.User2role;
import me.chr.hex.general.mapper.User2roleMapper;
import me.chr.hex.general.service.IUser2roleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户与角色的分配关系表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-03-11
 */
@Service
public class User2roleServiceImpl extends ServiceImpl<User2roleMapper, User2role> implements IUser2roleService {

}
