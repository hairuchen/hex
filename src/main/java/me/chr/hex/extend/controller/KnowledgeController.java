package me.chr.hex.extend.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.extend.DTO.RetrieveDTO;
import me.chr.hex.extend.VO.RetrieveResponseVO;
import me.chr.hex.extend.service.GraphKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Slf4j
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    @Autowired
    private GraphKnowledgeService graphKnowledgeService;

    @PostMapping("/retrieve")
    @PreAuthorize("hasAuthority('KnowledgeController:retrieve')")
    @Operation(summary = "知识库混合检索")
    public CommonResult<List<RetrieveResponseVO>> retrieve(@Validated @RequestBody RetrieveDTO retrieveDTO) {
        return CommonResult.success(graphKnowledgeService.hybridRetrieval(retrieveDTO.getQuery(), retrieveDTO.getTopN()));
    }


}
