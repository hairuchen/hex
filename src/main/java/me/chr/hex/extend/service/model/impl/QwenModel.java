package me.chr.hex.extend.service.model.impl;


import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.chr.hex.extend.service.model.AbstractModel;
import me.chr.hex.extend.service.model.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Service
public class QwenModel implements EmbeddingModel, AbstractModel {

    @Value("${dashscope.embed.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Double> embed(String text) {
        return this.getOneVector(text);
    }

    private List<Double> getOneVector(String str){
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

    // ===================== Qwen3-Max LLM 调用（提取实体与关系） =====================
        private Map<String, Object> getEntityAndRelation(String chunkText) {
        try {
            // 1. 构造 Prompt（强制返回 JSON）
            String prompt = "你是一个知识图谱构建专家，请从以下文本中提取实体、实体类型、实体间关系。\n" +
                    "文本内容：\n" + chunkText + "\n\n" +
                    "要求：\n" +
                    "1. 实体必须包含 name 和 type（如 实体名:Spring Boot, 类型:框架）\n" +
                    "2. 关系必须包含 source、target、relation（如 source:Spring Boot, target:Spring Core, relation:依赖）\n" +
                    "3. 只返回 JSON，不要任何解释、不要多余内容\n" +
                    "4. JSON 结构如下：\n" +
                    "{\n" +
                    "  \"entities\": [{\"name\": \"\", \"type\": \"\"}],\n" +
                    "  \"relations\": [{\"source\": \"\", \"target\": \"\", \"relation\": \"\"}]\n" +
                    "}";

            // 2. 构造消息
            Message message = Message.builder()
                    .role(Role.USER.getValue())
                    .content(prompt)
                    .build();

            // 3. 调用 Qwen3-Max
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model("qwen3-max") // 你要的 qwen3max
                    .messages(Arrays.asList(message))
                    .temperature(0.1F) // 低温度，保证结果稳定
                    .resultFormat("json") // 强制返回 JSON
                    .build();

            Generation generation = new Generation();
            GenerationResult result = generation.call(param);

            // 4. 获取返回内容
            String jsonStr = result.getOutput().getChoices().get(0).getMessage().getContent();

            // 5. 转为 Map（方便后续处理）
            return objectMapper.readValue(jsonStr, new TypeReference<>() {});

        } catch (Exception e) {
            System.err.println("LLM 调用失败：" + e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> extractEntitiesAndRelations(String chunkText) {
        return this.getEntityAndRelation(chunkText);
    }
}
