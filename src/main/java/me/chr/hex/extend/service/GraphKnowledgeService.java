package me.chr.hex.extend.service;

import me.chr.hex.extend.VO.RetrieveResponseVO;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
public interface GraphKnowledgeService {

    // ======================  召回（原子能力） ======================
    /**
     * 全文检索（按原文分词匹配）
     */
    List<RetrieveResponseVO> retrieveByTextMatch(String query, Integer topN);
    /**
     * 向量余弦相似度召回
     */
    List<RetrieveResponseVO> retrieveByVector(String query, Integer topN);

    /**
     * TODO:路径检索
     * 路径召回
     */

    // ======================  多路召回（复合能力） ======================
    List<RetrieveResponseVO> multiPathRetrieve(String query,  Integer topN);

}
