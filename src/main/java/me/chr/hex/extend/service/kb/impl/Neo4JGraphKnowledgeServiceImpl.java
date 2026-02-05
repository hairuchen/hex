package me.chr.hex.extend.service.kb.impl;


import me.chr.hex.extend.DTO.RetrieveRequestDTO;
import me.chr.hex.extend.VO.RetrieveResponseVO;
import me.chr.hex.extend.controller.KnowledgeController;
import me.chr.hex.extend.mapper.KnowledgeChunkRepository;
import me.chr.hex.extend.properties.kb.RetrieveWeightProperties;
import me.chr.hex.extend.properties.neo4j.KnowledgeChunkNode;
import me.chr.hex.extend.properties.neo4j.VectorSearchResult;
import me.chr.hex.extend.service.kb.GraphKnowledgeService;
import me.chr.hex.extend.service.model.EmbeddingModel;
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
import java.util.*;
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
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private RetrieveWeightProperties retrieveWeightProperties;

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
    public List<VectorSearchResult> retrieveByVector(List<Double> queryVector, Integer topN) {
        // 参数校验：向量为空、topN 非法时返回空列表
        if (queryVector == null || queryVector.isEmpty() || topN == null || topN <= 0) {
            logger.warn("向量检索参数非法: queryVector={}, topN={}", queryVector, topN);
            return Collections.emptyList();
        }
        try {
            // 调用 Mapper 层向量查询方法
            return knowledgeChunkRepository.findTopByVectorSimilarityWithScore(queryVector, topN);
        } catch (Exception e) {
            logger.error("向量检索失败: queryVector={}, topN={}", queryVector, topN, e);
            return Collections.emptyList();
        }
    }

    @Override
    public KnowledgeChunkNode findPreviousChunk(String chunkId) {
        return null;
    }

    @Override
    public KnowledgeChunkNode findNextChunk(String chunkId) {
        return null;
    }

    @Override
    public List<VectorSearchResult> multiPathRetrieve(String query, Integer topN) {
        List<Double> vector=embeddingModel.embed(query);
        List<KnowledgeChunkNode> textList=this.retrieveByTextMatch(query,topN*2);
        List<VectorSearchResult> textResultList = textList.stream()
                .map(VectorSearchResult::new)  // 用你新增的构造函数，自动设为1分
                .toList();
        List<VectorSearchResult> vectorList=this.retrieveByVector(vector,topN*2);
        List<VectorSearchResult> mergeList=this.merge(textResultList,vectorList);
        return mergeList.stream()
                .limit(topN)
                .toList();
    }

    private List<VectorSearchResult> merge(List<VectorSearchResult> textList,List<VectorSearchResult> vectorList){
        Map<String, VectorSearchResult> map = new HashMap<>();

        // 先把 list1 全部放入
        for (VectorSearchResult res : textList) {
            String id = res.getChunk().getId();
            map.put(id, res);
        }
        // 再合并 list2
        for (VectorSearchResult vectorSearchResult : vectorList) {
            String id = vectorSearchResult.getChunk().getId();
            if (map.containsKey(id)) {
                // 已存在 → 分数相加
                VectorSearchResult textObject = map.get(id);
                textObject.setScore(textObject.getScore()*retrieveWeightProperties.getTextWeight() + vectorSearchResult.getScore()*retrieveWeightProperties.getVectorWeight());
            } else {
                // 不存在 → 直接加入
                vectorSearchResult.setScore(0*retrieveWeightProperties.getTextWeight()+vectorSearchResult.getScore()*retrieveWeightProperties.getVectorWeight());
                map.put(id, vectorSearchResult);
            }
        }
        // 转 list 并按分数降序返回
        List<VectorSearchResult> result = new ArrayList<>(map.values());
        result.sort(Comparator.comparingDouble(VectorSearchResult::getScore).reversed());
        return result;
    }


}
