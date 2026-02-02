package me.chr.hex.extend.service.kb;


import me.chr.hex.extend.dao.KnowledgeChunkNode;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
public interface KnowledgeService {
    // 创建知识片段节点
    void createChunk(KnowledgeChunkNode node);

    // 创建 NEXT_CHUNK 顺序关系
    void createNextChunkRelation(String fromChunkId, String toChunkId);
}
