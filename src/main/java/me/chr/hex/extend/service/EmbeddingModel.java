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
}
