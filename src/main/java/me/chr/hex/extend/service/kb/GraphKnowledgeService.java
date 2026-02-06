package me.chr.hex.extend.service.kb;


import me.chr.hex.extend.properties.neo4j.ChunkNode;
import me.chr.hex.extend.properties.neo4j.EntityNode;
import me.chr.hex.extend.properties.neo4j.VectorSearchResult;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
public interface GraphKnowledgeService {

    // ======================  创建节点与关系 ======================
    // 创建知识片段节点
    void createChunk(ChunkNode node);
    // 创建 NEXT_CHUNK 顺序关系
    void createNextChunkRelation(String fromChunkId, String toChunkId);

    // ======================  知识图谱实体与关系 ======================
    // 创建实体节点
    void createEntityNode(EntityNode node);

    // 创建 chunk -> ENTITY -> entity 关系
    void createChunkToEntityRelation(String chunkId, String entityContent);

    // 创建实体之间的关系（source -> relation -> target）
    void createEntityRelation(String sourceEntityContent, String targetEntityContent, String relationType);

    // ======================  召回（原子能力） ======================
    /**
     * 全文检索（按原文分词匹配）
     */
    List<ChunkNode> retrieveByTextMatch(String query, Integer topN);
    /**
     * 向量余弦相似度召回
     */
    List<VectorSearchResult> retrieveByVector(List<Double> queryVector, Integer topN);
    /**
     * 查询当前节点的上一个节点（NEXT_CHUNK 反向）
     */
    ChunkNode findPreviousChunk(String chunkId);
    /**
     * 查询当前节点的下一个节点（NEXT_CHUNK 正向）
     */
    ChunkNode findNextChunk(String chunkId);

    // ======================  多路召回（复合能力） ======================
    List<VectorSearchResult> multiPathRetrieve(String query,  Integer topN);

}
