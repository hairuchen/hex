package me.chr.hex.general.service.impl;

import me.chr.hex.general.entity.User2menu;
import me.chr.hex.general.mapper.User2menuMapper;
import me.chr.hex.general.service.IUser2menuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户与权限的授权关系表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-03-11
 */
@Service
public class User2menuServiceImpl extends ServiceImpl<User2menuMapper, User2menu> implements IUser2menuService {

}
