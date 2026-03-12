package me.chr.hex.general.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.DTO.MenuDTO;
import me.chr.hex.extend.DTO.UserDTO;
import me.chr.hex.general.entity.Menu;
import me.chr.hex.general.entity.User;
import me.chr.hex.general.mapper.MenuMapper;
import me.chr.hex.general.service.IMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public List<Menu> batchCreate(List<MenuDTO> menuDTOList) {
        try {
            // 持久化
            List<Menu> menuList= new ArrayList<>();
            for (MenuDTO menuDTO : menuDTOList) {
                Menu menu = menuDTO.ToMenu();
                menuList.add(menu);
            }
            this.saveBatch(menuList);
            return menuList;
        }catch (Exception e){
            log.error("创建菜单失败!"+e);
            throw new BizException("创建菜单失败!");
        }
    }
}
