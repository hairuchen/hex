package me.chr.hex.general.service;

import me.chr.hex.extend.DTO.PermissionDTO;
import me.chr.hex.general.entity.SysPermission;
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
public interface ISysPermissionService extends IService<SysPermission> {

    List<SysPermission> batchCreate(List<PermissionDTO> permissionDTOList);

}
