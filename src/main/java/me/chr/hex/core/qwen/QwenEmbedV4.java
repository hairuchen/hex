package me.chr.hex.core.qwen;


import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/2
 **/
@Component
public class QwenEmbedV4 {

    @Value("${dashscope.embed.api-key}")
    private String apiKey;

    public List<Double> getOneVector(String str){
        try {
            // 构建请求参数
            TextEmbeddingParam param = TextEmbeddingParam
                    .builder()
                    .apiKey(apiKey)
                    .model("text-embedding-v4")  // 使用text-embedding-v4模型
                    .texts(Arrays.asList(str))  // 输入文本
                    .parameter("dimension", 1024)  // 指定向量维度（仅 text-embedding-v3及 text-embedding-v4支持该参数）
                    .build();

            // 创建模型实例并调用
            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult result = textEmbedding.call(param);

//            System.out.println(result);
            return result.getOutput().getEmbeddings().getFirst().getEmbedding();
        } catch (ApiException | NoApiKeyException e) {
            System.out.println("调用失败：" + e.getMessage());
        }
        return null;
    }
}
