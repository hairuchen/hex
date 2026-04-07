package me.chr.hex.general.service;

import me.chr.hex.extend.DTO.WorkspacePermissionDTO;
import me.chr.hex.general.entity.Workspace2permission;
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
public interface IWorkspace2permissionService extends IService<Workspace2permission> {

    /**
     * 给工作空间授权
     * @param dtoList 授权列表
     * @return 授权结果
     */
    List<Workspace2permission> grantToWorkspace(List<WorkspacePermissionDTO> dtoList);
}
