package me.chr.hex.extend.service.impl;


import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.VO.RetrieveResponseVO;
import me.chr.hex.extend.mapper.ChunkNodeMapper;
import me.chr.hex.extend.mapper.EntityNodeMapper;
import me.chr.hex.extend.properties.kb.RetrieveWeightProperties;
import me.chr.hex.extend.BO.ChunkNode;
import me.chr.hex.extend.service.EmbeddingModel;
import me.chr.hex.extend.service.GraphKnowledgeService;
import me.chr.hex.extend.service.QuicklyAbstractModel;
import me.chr.hex.extend.service.RerankModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private EntityNodeMapper entityNodeMapper;

    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired(required = false)
    private RerankModel rerankModel;
    @Autowired(required = false)
    private QuicklyAbstractModel quicklyAbstractModel;
    @Autowired
    private RetrieveWeightProperties retrieveWeightProperties;
    // 定义每路召回的数量，可根据配置调整
    private static final int RECALL_PER_STRATEGY = 30;


    /*
    * 值域:0/1
     */
    @Override
    public List<RetrieveResponseVO> retrieveByText(String query) {
        try {
            List<ChunkNode> chunkNodeList=chunkNodeMapper.findByContentContainingLimit(query.trim());
            List<RetrieveResponseVO> retrieveResponseVOList=new ArrayList<>();
            for (ChunkNode chunkNode:chunkNodeList){
                RetrieveResponseVO retrieveResponseVO=new RetrieveResponseVO(chunkNode);
                retrieveResponseVOList.add(retrieveResponseVO);
            }
            return retrieveResponseVOList;
        } catch (Exception e) {
            log.error("文本匹配失败: query="+query+e);
            throw new BizException("文本匹配失败!");
        }
    }

    /*
    * 值域:0到正无穷
     */
    @Override
    public List<RetrieveResponseVO> retrieveByKeyword(String query, Integer topN) {
        try {
            return chunkNodeMapper.findTopByFullText(query,topN);
        } catch (Exception e) {
            log.error("文本匹配失败: query={}", query, e);
            throw new BizException("分词匹配失败!");
        }
    }

    /*
     * 值域:0到1（归一化后）
     */
    @Override
    public List<RetrieveResponseVO> retrieveByVector(String query, Integer topN) {
        try {
            List<Double> queryVector=embeddingModel.normalization(query,embeddingModel.embed(query));
            return chunkNodeMapper.findTopByVectorSimilarityWithScore(queryVector, topN);
        } catch (Exception e) {
            log.error("向量检索失败: query={} , topN={}",query,topN,e);
            throw new BizException("向量检索失败!");
        }
    }

    /*
     * 此处返回为路径列表 混合检索 不应 聚合
     * TODO: 或许此处可以利用分词器来强化实体节点入参的处理
     */
    @Override
    public RetrieveResponseVO retrieveByPath1(String oneNode, String twoNode, Integer distance) {
        List<ChunkNode> chunkNodeList=chunkNodeMapper.findChunksByShortestPath(oneNode.toLowerCase(),twoNode.toLowerCase(),distance);
        if (chunkNodeList==null||chunkNodeList.isEmpty()){
            return null;
        }
        ChunkNode first=chunkNodeList.getFirst();
        for (ChunkNode chunkNode:chunkNodeList){
            if (chunkNode!=first){
                first.setContent(first.getContent()+chunkNode.getContent());
            }
        }
        return new RetrieveResponseVO(first);
    }
    @Override
    public RetrieveResponseVO retrieveByPath2(String query, Integer distance) {
        if (quicklyAbstractModel == null) {
            throw new BizException("QuicklyAbstractModel快速抽象模型 未注入,请求失败");
        }
        HashMap<String,String> hashMap=quicklyAbstractModel.quicklyAbstractEntity(query);
        if (hashMap.get("oneNode")==null||hashMap.get("twoNode")==null){
            throw new BizException("QuicklyAbstractModel快速抽象模型 解析错误,请求失败");
        }

        return this.retrieveByPath1(hashMap.get("oneNode"),hashMap.get("twoNode"),distance);
    }

    @Override
    public List<RetrieveResponseVO> hybridRetrieval(String query, Integer topN) {
        if (rerankModel == null) {
            log.warn("RerankModel 未注入，降级为普通向量检索");
            return retrieveByVector(query, topN);
        }
        return this.hybridRetrievalByRerankModel(query,topN);
    }

    /*
     * 算法模式:
     * 值域维度相同 成比例 ——> 缩放
     * 值域维度相同 不成比例 ——> 归一化
     * 值域维度不同 ——> RRF
     * 输入限制: K级
     */
    public List<RetrieveResponseVO> hybridRetrievalByAlgorithm(String query, Integer topN) {

        return List.of();
    }

    /*
     * 重排模型模式:
     * 全部搞里头 ——> RerankModel ——> topN
     * 输入限制: 50-200
     */
    public List<RetrieveResponseVO> hybridRetrievalByRerankModel(String query, Integer topN) {
        List<RetrieveResponseVO> textResults = retrieveByText(query);
        List<RetrieveResponseVO> keywordResults = retrieveByKeyword(query, RECALL_PER_STRATEGY);
        List<RetrieveResponseVO> vectorResults = retrieveByVector(query, RECALL_PER_STRATEGY);
        RetrieveResponseVO pathResult=retrieveByPath2(query,10);

        Map<String, RetrieveResponseVO> contentVoMap = new HashMap<>();
        Stream.of(textResults, keywordResults, vectorResults)
                .flatMap(List::stream)
                .filter(vo -> vo.getChunkNode() != null && vo.getChunkNode().getContent() != null)
                .forEach(vo -> contentVoMap.put(vo.getChunkNode().getContent(), vo));
        contentVoMap.put(pathResult.getChunkNode().getContent(),pathResult);

        List<HashMap<String, Object>> rerankResults=rerankModel.rerank(query,new ArrayList<>(contentVoMap.keySet()),RECALL_PER_STRATEGY);
        List<RetrieveResponseVO> finalResults = new ArrayList<>();
        for (HashMap<String, Object> item : rerankResults) {
            String content = (String) item.get("text");
            Double newScore = (item.get("score") instanceof Number) ? ((Number) item.get("score")).doubleValue() : 0.0;
            RetrieveResponseVO vo = contentVoMap.get(content);
            if (vo != null) {
                // 更新分数
                vo.setScore(newScore);
                // 加入结果集 (此时顺序即为重排后的高分到低分顺序)
                finalResults.add(vo);
            } else {
                log.warn("Rerank 返回的内容未在召回结果中找到: {}", content);
            }
        }
        return finalResults;
    }




}
