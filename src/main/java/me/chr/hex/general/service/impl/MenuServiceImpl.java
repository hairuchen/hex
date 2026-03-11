package me.chr.hex.general.service.impl;

import me.chr.hex.general.entity.Menu;
import me.chr.hex.general.mapper.MenuMapper;
import me.chr.hex.general.service.IMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 菜单/权限实体表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-03-11
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {

}
