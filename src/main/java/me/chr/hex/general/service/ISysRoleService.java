package me.chr.hex.general.service;

import me.chr.hex.extend.DTO.RoleDTO;
import me.chr.hex.general.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
public interface ISysRoleService extends IService<SysRole> {

    List<SysRole> batchCreate(List<RoleDTO> roleDTOList);

}
