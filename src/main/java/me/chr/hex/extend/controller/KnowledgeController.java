package me.chr.hex.extend.controller;

import me.chr.hex.extend.DTO.RetrieveRequestDTO;
import me.chr.hex.extend.service.kb.GraphKnowledgeService;
import me.chr.hex.extend.service.model.AbstractModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        logger.info("召回查询,召回query:"+retrieveRequestDTO.getQuery()+" top:"+retrieveRequestDTO.getTopN());
        // 调用服务层进行检索
        return Collections.singletonList(graphKnowledgeService.multiPathRetrieve(retrieveRequestDTO.getQuery(), retrieveRequestDTO.getTopN()));
    }

    @Autowired
    private AbstractModel abstractModel;
    @PostMapping("/test")
    @ResponseBody
    public Map<String, Object> test(@Validated @RequestBody RetrieveRequestDTO retrieveRequestDTO) {
        logger.info("test接口调用:"+retrieveRequestDTO.getQuery()+" top:"+retrieveRequestDTO.getTopN());
        Map<String, Object> entityAndRelationMaps = abstractModel.extractEntitiesAndRelations(retrieveRequestDTO.getQuery());

        // 调用服务层进行检索
        return entityAndRelationMaps;
    }
}
