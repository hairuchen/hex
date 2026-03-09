package me.chr.hex.extend.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.ResponseFormat;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.extend.BO.ImageChunkParseResult;
import me.chr.hex.extend.service.EmbeddingModel;
import me.chr.hex.extend.service.ParseModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Slf4j
@Service
public class QwenModel implements ParseModel,EmbeddingModel {

    @Value("${dashscope.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper=new ObjectMapper();

    @Override
    public ImageChunkParseResult base64ToChunks(String pageBase64, String frontContext){
        return this.oneBase64ToChunks(pageBase64, frontContext);
    }

    //TODO: 建议此处使用阿里百炼工作流中的文件解析节点实现
    private ImageChunkParseResult oneBase64ToChunks(String pageBase64, String frontContext) {
        try {
            // 1. 前置上下文处理：空值转为空字符串，避免null异常
            String validFrontContext = frontContext == null ? "" : frontContext.trim();

            // 构造 Prompt
            String systemPrompt = String.format("""
                        你是专业的文本解析助手，严格执行以下指令：
                        0. 首先拼接上一页最后一段内容在图片内容最前方：%s ；
                        1. 提取图片中的所有文本内容，按段落切分，过滤页码、页眉页脚等无关内容；
                        2. 表格内容合并为一个段落，用结构化自然语言描述（例：表格包含列A/列B，行1：值1/值2，行2：值3/值4）；
                        3. 图片内容用自然语言描述（例：图片包含一个流程图，展示XX流程）；
                        4. 仅返回标准JSON，结构：{"chunks": ["文本段1", "文本段2"]}，无任何多余内容；
                        5. 严格忠于原文，不编造、不篡改字符。
                    """, validFrontContext.isEmpty() ? "无" : validFrontContext);
            Map<String, Object> textContent = Collections.singletonMap("text", systemPrompt);
            // 构造图片内容节点
            Map<String, Object> imageContent = Collections.singletonMap("image", pageBase64);

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(imageContent, textContent)) // 图片在前，文本Prompt在后
                    .build();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(apiKey)
                    .model("qwen-vl-max-latest")  // 此处以qwen-vl-max-latest为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
                    .messages(Collections.singletonList(userMessage))
                    .responseFormat(ResponseFormat.from(ResponseFormat.JSON_OBJECT))
                    .build();
            MultiModalConversationResult result = new MultiModalConversation().call(param);

            // 解析嵌套的JSON结果
            Object content  = result.getOutput().getChoices().getFirst().getMessage().getContent().getFirst();
            log.debug("模型返回原始JSON：{}", content);
            ImageChunkParseResult ImageChunkParseResult = new ImageChunkParseResult(new ArrayList<>(),"");
            String jsonStr = content instanceof Map ? ((Map<?,?>)content).get("text").toString().trim() : content.toString().trim();
            // 清理可能的代码块标记
            jsonStr = jsonStr.replaceAll("^```json|```$", "").trim();

            Map<String, Object> resultMap = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            List<?> rawChunks = (List<?>) resultMap.get("chunks");

            // 提取有效 chunk
            for (Object chunk : rawChunks) {
                if (chunk instanceof String && !((String) chunk).isEmpty()) {
                    ImageChunkParseResult.getChunks().add(((String) chunk).trim());
                }
            }

            return ImageChunkParseResult;
        } catch (NoApiKeyException | UploadFileException | ApiException e){
            throw new RuntimeException("QwenModel 调用失败,请检查API KEY!"+e);
        }catch (Exception e){
            throw new RuntimeException("QwenModel远程调用异常:" , e);
        }
    }

    @Override
    public List<Double> embed(String text) {
        return this.getOneVector(text);
    }

    private List<Double> getOneVector(String str) {
        try {
            // 构建请求参数
            TextEmbeddingParam param = TextEmbeddingParam
                    .builder()
                    .apiKey(apiKey)
                    .model("text-embedding-v4")  // 使用text-embedding-v4模型
                    .texts(Collections.singletonList(str))  // 输入文本
                    .parameter("dimension", 1024)  // 指定向量维度（仅 text-embedding-v3及 text-embedding-v4支持该参数）
                    .build();

            // 创建模型实例并调用
            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult result = textEmbedding.call(param);

            return result.getOutput().getEmbeddings().getFirst().getEmbedding();
        } catch (ApiException | NoApiKeyException e) {
            throw new RuntimeException("QwenModel远程调用异常:" , e);
        }
    }

    @Override
    public List<String> chunkToEntity(String chunk) {
        return this.getEntity(chunk);
    }

    private List<String> getEntity(String chunk) {
        try {
            // 1. 构造 Prompt（强制返回 JSON）
            String prompt = "你是一个知识图谱构建专家，请从以下文本中提取实体。\n" +
                    "文本内容：\n" + chunk + "\n\n" +
                    "要求：\n" +
                    "1. 实体必须包含 name（如 实体名:Spring Boot）\n" +
                    "2. 只返回 JSON，不要任何解释、不要多余内容\n" +
                    "3. JSON 结构如下：\n" +
                    "{\n" +
                    "  \"entities\": [{\"name\": \"\"}],\n" +
                    "}";

            // 2. 构造消息
            Message message = Message.builder()
                    .role(Role.USER.getValue())
                    .content(prompt)
                    .build();

            // 3. 调用 Qwen3-Max
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model("qwen3-max")
                    .messages(Collections.singletonList(message))
                    .temperature(0.1F) // 低温度，保证结果稳定
                    .resultFormat("json") // 强制返回 JSON
                    .build();

            Generation generation = new Generation();
            GenerationResult result = generation.call(param);

            // 4. 获取返回内容
            String jsonStr = result.getOutput().getChoices().getFirst().getMessage().getContent();
            jsonStr = jsonStr.replaceAll("^```json|```$", "").trim();

            Map<String, Object> resultMap = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            List<Map<String, String>> entities = (List<Map<String, String>>) resultMap.get("entities");

            List<String> entityNames = new ArrayList<>();
            if (entities != null) {
                for (Map<String, String> entity : entities) {
                    String name = entity.get("name");
                    if (name != null && !name.trim().isEmpty()) {
                        entityNames.add(name.trim());
                    }
                }
            }
            return entityNames;

        } catch (Exception e) {
            throw new RuntimeException("QwenModel远程调用异常:" , e);
        }
    }



    // ===================== Qwen3-Max LLM 调用（提取实体与关系） =====================
    @Deprecated
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
                    .model("qwen3-max")
                    .messages(Collections.singletonList(message))
                    .temperature(0.1F) // 低温度，保证结果稳定
                    .resultFormat("json") // 强制返回 JSON
                    .build();

            Generation generation = new Generation();
            GenerationResult result = generation.call(param);

            // 4. 获取返回内容
            String jsonStr = result.getOutput().getChoices().getFirst().getMessage().getContent();

            // 5. 转为 Map（方便后续处理）
            return objectMapper.readValue(jsonStr, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.info("LLM 调用失败：{}", e.getMessage());
            return null;
        }
    }


}
