package me.chr.hex.general.service;

import me.chr.hex.extend.DTO.MenuDTO;
import me.chr.hex.extend.DTO.UserDTO;
import me.chr.hex.general.entity.Menu;
import com.baomidou.mybatisplus.extension.service.IService;
import me.chr.hex.general.entity.User;

import java.util.List;

/**
 * <p>
 * 菜单/权限实体表 服务类
 * </p>
 *
 * @author baomidou
 * @since 2026-03-11
 */
public interface IMenuService extends IService<Menu> {
    List<Menu> batchCreate(List<MenuDTO> menuDTOList);
}
