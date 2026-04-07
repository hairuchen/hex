package me.chr.hex.general.service;

import me.chr.hex.extend.DTO.RolePermissionDTO;
import me.chr.hex.general.entity.Role2permission;
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
public interface IRole2permissionService extends IService<Role2permission> {

    /**
     * 给角色授权
     * @param dtoList 授权列表
     * @return 授权结果
     */
    List<Role2permission> grantToRole(List<RolePermissionDTO> dtoList);
}
