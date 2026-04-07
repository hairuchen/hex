package me.chr.hex.general.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.DTO.WorkspaceDTO;
import me.chr.hex.general.entity.Workspace;
import me.chr.hex.general.mapper.WorkspaceMapper;
import me.chr.hex.general.service.IWorkspaceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 工作空间表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
@Slf4j
@Service
public class WorkspaceServiceImpl extends ServiceImpl<WorkspaceMapper, Workspace> implements IWorkspaceService {

    @Override
    public List<Workspace> batchCreate(List<WorkspaceDTO> workspaceDTOList) {
        try {
            // 持久化
            List<Workspace> workspaceList= new ArrayList<>();
            for (WorkspaceDTO workspaceDTO : workspaceDTOList) {
                Workspace workspace = workspaceDTO.toWorkspace();
                workspaceList.add(workspace);
            }
            this.saveBatch(workspaceList);
            return workspaceList;
        }catch (Exception e){
            log.error("创建工作空间失败!"+e);
            throw new BizException("创建工作空间失败!");
        }
    }
}
