package me.chr.hex.extend.service.impl;


import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.VO.RetrieveResponseVO;
import me.chr.hex.extend.mapper.ChunkNodeMapper;
import me.chr.hex.extend.properties.kb.RetrieveWeightProperties;
import me.chr.hex.extend.BO.ChunkNode;
import me.chr.hex.extend.service.EmbeddingModel;
import me.chr.hex.extend.service.GraphKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Service
@Slf4j
public class Neo4JGraphKnowledgeServiceImpl implements GraphKnowledgeService {

    @Autowired
    private ChunkNodeMapper chunkNodeMapper;

    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private RetrieveWeightProperties retrieveWeightProperties;


    @Override
    public List<RetrieveResponseVO> retrieveByTextMatch(String query, Integer topN) {
        if (query == null || query.trim().isEmpty() || topN == null || topN <= 0) {
            return Collections.emptyList();
        }
        try {
            List<ChunkNode> chunkNodeList=chunkNodeMapper.findTopByContentContaining(query, topN);
            List<RetrieveResponseVO> retrieveResponseVOList=new ArrayList<>();
            for (ChunkNode chunkNode:chunkNodeList){
                RetrieveResponseVO retrieveResponseVO=new RetrieveResponseVO(chunkNode);
                retrieveResponseVOList.add(retrieveResponseVO);
            }
            return retrieveResponseVOList;
        } catch (Exception e) {
            log.error("文本匹配失败: query="+query+", topN="+topN+"\n"+e);
            throw new BizException("文本匹配失败: query="+query+", topN="+topN);
        }
    }

    @Override
    public List<RetrieveResponseVO> retrieveByVector(String query, Integer topN) {
        try {
            List<Double> queryVector=embeddingModel.embed(query);
            return chunkNodeMapper.findTopByVectorSimilarityWithScore(queryVector, topN);
        } catch (Exception e) {
            log.error("向量检索失败: query="+query+", topN="+topN+"\n"+e);
            throw new BizException("向量检索失败: query="+query+", topN="+topN);
        }
    }

    @Override
    public List<RetrieveResponseVO> multiPathRetrieve(String query, Integer topN) {
        //分词检索
        List<RetrieveResponseVO> textList=this.retrieveByTextMatch(query,topN*2);
        //向量检索
        List<RetrieveResponseVO> vectorList=this.retrieveByVector(query,topN*2);
        //多路合并
        List<RetrieveResponseVO> mergeList=this.merge(textList,vectorList);
        return mergeList.stream()
                .limit(topN)
                .toList();
    }

    private List<RetrieveResponseVO> merge(List<RetrieveResponseVO> textList,List<RetrieveResponseVO> vectorList){
        Map<String, RetrieveResponseVO> map = new HashMap<>();

        // 先把 list1 全部放入
        for (RetrieveResponseVO retrieveResponseVO : textList) {
            String id = retrieveResponseVO.getChunkNode().getId();
            map.put(id, retrieveResponseVO);
        }
        // 再合并 list2
        for (RetrieveResponseVO retrieveResponseVO : vectorList) {
            String id = retrieveResponseVO.getChunkNode().getId();
            if (map.containsKey(id)) {
                // 已存在 → 分数相加
                RetrieveResponseVO textObject = map.get(id);
                textObject.setScore(textObject.getScore()*retrieveWeightProperties.getTextWeight() + retrieveResponseVO.getScore()*retrieveWeightProperties.getVectorWeight());
            } else {
                // 不存在 → 直接加入
                retrieveResponseVO.setScore(0*retrieveWeightProperties.getTextWeight()+retrieveResponseVO.getScore()*retrieveWeightProperties.getVectorWeight());
                map.put(id, retrieveResponseVO);
            }
        }
        // 转 list 并按分数降序返回
        List<RetrieveResponseVO> result = new ArrayList<>(map.values());
        result.sort(Comparator.comparingDouble(RetrieveResponseVO::getScore).reversed());
        return result;
    }

}
