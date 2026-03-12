package me.chr.hex.extend.service;


import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
public interface EmbeddingModel {

    /**
     * 单条文本向量化
     * @param text 待向量化的文本
     * @return 浮点型向量列表
     */
    List<Double> embed(String text);

    /**
     * 归一化
     * @param vectors 待归一化的向量集合
     * @return 向量集合
     */
    List<Double> normalization(String chunk,List<Double> vectors);
}
