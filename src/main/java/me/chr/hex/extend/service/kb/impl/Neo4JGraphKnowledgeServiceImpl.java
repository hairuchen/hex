package me.chr.hex.extend.service.kb.impl;


import me.chr.hex.extend.DTO.RetrieveRequestDTO;
import me.chr.hex.extend.VO.RetrieveResponseVO;
import me.chr.hex.extend.controller.KnowledgeController;
import me.chr.hex.extend.mapper.KnowledgeChunkRepository;
import me.chr.hex.extend.properties.neo4j.KnowledgeChunkNode;
import me.chr.hex.extend.service.kb.GraphKnowledgeService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Service
public class Neo4JGraphKnowledgeServiceImpl implements GraphKnowledgeService {

    private static final Logger logger = LoggerFactory.getLogger(Neo4JGraphKnowledgeServiceImpl.class);

    @Autowired
    private KnowledgeChunkRepository knowledgeChunkRepository;

    @Override
    public void createChunk(KnowledgeChunkNode node) {
        knowledgeChunkRepository.save(node);
    }

    @Override
    public void createNextChunkRelation(String fromChunkId, String toChunkId) {
        try {
            knowledgeChunkRepository.createNextChunkRelation(fromChunkId, toChunkId);
        } catch (Exception e) {
            logger.error("创建 NEXT_CHUNK 关系失败: from={}, to={}", fromChunkId, toChunkId, e);
            throw e; // 或按需处理
        }
    }

    @Override
    public List<KnowledgeChunkNode> retrieveByTextMatch(String query, Integer topN) {
        if (query == null || query.trim().isEmpty() || topN == null || topN <= 0) {
            return Collections.emptyList();
        }
        try {
            return knowledgeChunkRepository.findTopByContentContaining(query, topN);
        } catch (Exception e) {
            logger.error("文本匹配失败: query={}, topN={}", query, topN, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<KnowledgeChunkNode> retrieveByVector(List<Double> queryVector, Integer topN) {
        return List.of();
    }

    @Override
    public KnowledgeChunkNode findPreviousChunk(String chunkId) {
        return null;
    }

    @Override
    public KnowledgeChunkNode findNextChunk(String chunkId) {
        return null;
    }





}
