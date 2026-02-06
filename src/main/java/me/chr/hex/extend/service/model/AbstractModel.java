package me.chr.hex.extend.service.model;


import java.util.Map;

/**
 * @Author: CHR
 * @Date: create in 2026/2/5
 **/
public interface AbstractModel {

    /**
     * 统一的 LLM 调用方法：提取实体与关系
     * @param chunkText 文本片段
     * @return 包含 entities、relations 的 Map
     */
    Map<String, Object> extractEntitiesAndRelations(String chunkText);
}
