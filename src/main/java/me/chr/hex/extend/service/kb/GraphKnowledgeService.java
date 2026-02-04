package me.chr.hex.extend.service.kb;


import me.chr.hex.extend.DTO.RetrieveRequestDTO;
import me.chr.hex.extend.VO.RetrieveResponseVO;
import me.chr.hex.extend.properties.neo4j.KnowledgeChunkNode;
import org.neo4j.driver.types.Node;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
public interface GraphKnowledgeService {

    // ====================== 1. 创建节点与关系 ======================
    // 创建知识片段节点
    void createChunk(KnowledgeChunkNode node);
    // 创建 NEXT_CHUNK 顺序关系
    void createNextChunkRelation(String fromChunkId, String toChunkId);

    // ====================== 2. 多路召回（原子能力） ======================
    /**
     * 全文检索（按原文分词匹配）
     */
    List<KnowledgeChunkNode> retrieveByTextMatch(String query, Integer topN);
    /**
     * 向量余弦相似度召回
     */
    List<KnowledgeChunkNode> retrieveByVector(List<Double> queryVector, Integer topN);
    /**
     * 查询当前节点的上一个节点（NEXT_CHUNK 反向）
     */
    KnowledgeChunkNode findPreviousChunk(String chunkId);
    /**
     * 查询当前节点的下一个节点（NEXT_CHUNK 正向）
     */
    KnowledgeChunkNode findNextChunk(String chunkId);

}
