package me.chr.hex.extend.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.extend.DTO.WorkspaceDTO;
import me.chr.hex.general.entity.Workspace;
import me.chr.hex.general.service.IWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/3/13
 **/
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    @Autowired
    private IWorkspaceService workspaceService;


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('WorkspaceController:batchCreate')")
    @Operation(summary = "创建工作空间")
    public CommonResult<List<Workspace>> batchCreate(@Validated @NotEmpty(message = "工作空间列表[workspaceDTOList]不能为空") @RequestBody List<@Valid WorkspaceDTO> workspaceDTOList) {
        return CommonResult.success(workspaceService.batchCreate(workspaceDTOList));
    }
}
