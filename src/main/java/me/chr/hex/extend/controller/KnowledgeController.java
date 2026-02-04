package me.chr.hex.extend.controller;


import me.chr.hex.extend.DTO.RetrieveRequestDTO;
import me.chr.hex.extend.VO.RetrieveResponseVO;
import me.chr.hex.extend.service.kb.GraphKnowledgeService;
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
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeController.class);

    @Autowired
    private GraphKnowledgeService graphKnowledgeService;

    @PostMapping("/retrieve")
    @ResponseBody
    public List<Object> retrieve(@Validated @RequestBody RetrieveRequestDTO retrieveRequestDTO) {

        // 调用服务层进行检索
        return Collections.singletonList(graphKnowledgeService.retrieveByTextMatch(retrieveRequestDTO.getQuery(), retrieveRequestDTO.getTopN()));
    }
}
