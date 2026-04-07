package me.chr.hex.general.service;

import me.chr.hex.extend.DTO.WorkspaceDTO;
import me.chr.hex.general.entity.Workspace;
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
public interface IWorkspaceService extends IService<Workspace> {

    List<Workspace> batchCreate(List<WorkspaceDTO> workspaceDTOList);

}
