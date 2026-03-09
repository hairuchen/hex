package me.chr.hex.extend.controller;

import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.extend.DTO.RetrieveRequestDTO;
import me.chr.hex.extend.VO.RetrieveResponseVO;
import me.chr.hex.extend.service.GraphKnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    public CommonResult<List<RetrieveResponseVO>> retrieve(@Validated @RequestBody RetrieveRequestDTO retrieveRequestDTO) {
        return CommonResult.success(graphKnowledgeService.multiPathRetrieve(retrieveRequestDTO.getQuery(), retrieveRequestDTO.getTopN()));
    }

}
