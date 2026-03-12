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
     * 文本检索（模糊匹配）
     */
    List<RetrieveResponseVO> retrieveByText(String query);

    /**
     * 分词检索
     */
    List<RetrieveResponseVO> retrieveByKeyword(String query,Integer topN);

    /**
     * 向量检索
     */
    List<RetrieveResponseVO> retrieveByVector(String query, Integer topN);

    /**
     * 路径检索1
     */
    RetrieveResponseVO retrieveByPath1(String oneNode,String twoNode, Integer distance);
    /**
     * 路径检索2
     */
    RetrieveResponseVO retrieveByPath2(String query, Integer distance);

    // ======================  混合检索 ======================
    List<RetrieveResponseVO> hybridRetrieval(String query,  Integer topN);

}
