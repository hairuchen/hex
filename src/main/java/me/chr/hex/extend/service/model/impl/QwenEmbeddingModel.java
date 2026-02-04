package me.chr.hex.extend.service.model.impl;


import me.chr.hex.core.qwen.QwenEmbedV4;
import me.chr.hex.extend.service.model.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Service
public class QwenEmbeddingModel implements EmbeddingModel {

    @Autowired
    private QwenEmbedV4 qwenEmbedV4;

    @Override
    public List<Double> embed(String text) {
        return qwenEmbedV4.getOneVector(text);
    }
}
