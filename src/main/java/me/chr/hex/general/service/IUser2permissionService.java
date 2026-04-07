package me.chr.hex.general.service;

import me.chr.hex.extend.DTO.UserPermissionDTO;
import me.chr.hex.general.entity.User2permission;
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
public interface IUser2permissionService extends IService<User2permission> {

    /**
     * 给用户授权
     * @param dtoList 授权列表
     * @return 授权结果
     */
    List<User2permission> grantToUser(List<UserPermissionDTO> dtoList);

    /**
     * 给用户授权所有权限
     * @param userId 用户ID
     * @return 授权结果
     */
    List<User2permission> grantAllPermissionsToUser(String userId);
}
