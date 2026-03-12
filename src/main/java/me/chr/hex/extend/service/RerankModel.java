package me.chr.hex.extend.service;


import java.util.HashMap;
import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/3/16
 **/
public interface RerankModel {
    /**
     * 对文档列表进行重排序打分
     * @param query 查询语句
     * @param documents 待排序的文档内容列表
     * @return 对应文档的得分列表 (顺序与documents一致)
     */
    List<HashMap<String, Object>> rerank(String query, List<String> documents,Integer size);
}
