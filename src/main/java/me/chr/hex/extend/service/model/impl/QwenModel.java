package me.chr.hex.extend.service.model.impl;


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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.chr.hex.extend.BO.VLLMParse;
import me.chr.hex.extend.controller.FileController;
import me.chr.hex.extend.service.model.AbstractModel;
import me.chr.hex.extend.service.model.AbstractObject;
import me.chr.hex.extend.service.model.EmbeddingModel;
import me.chr.hex.extend.service.model.ParseModel;
import me.chr.hex.general.entity.TFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Service
public class QwenModel implements EmbeddingModel, AbstractModel, ParseModel {

    private static final Logger logger = LoggerFactory.getLogger(QwenModel.class);

    @Value("${dashscope.api-key}")
    private String apiKey;

    @Autowired
    private AbstractObject abstractObject;

    private final ObjectMapper objectMapper=new ObjectMapper();

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

//            System.out.println(result);
            return result.getOutput().getEmbeddings().getFirst().getEmbedding();
        } catch (ApiException | NoApiKeyException e) {
            logger.info("调用失败：{}", e.getMessage());
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
            logger.info("LLM 调用失败：{}", e.getMessage());
            return null;
        }
    }

    @Override
    public AbstractObject extractEntitiesAndRelations(String chunkText) {
        Map<String, Object> map=this.getEntityAndRelation(chunkText);
        if (map == null) {
            return abstractObject;
        }
        abstractObject.setENode(map);
        abstractObject.setRx(map);

        return abstractObject;
    }

    @Deprecated
//    @Override
    public VLLMParse parse_old(String pageBase64, String frontContext, TFile fileEntity) {
        // 初始化返回对象，默认空值兜底
        VLLMParse vllmParse = new VLLMParse();
        vllmParse.setChunks(new ArrayList<>());
        vllmParse.setUnprocessed("");
        // 前置校验：输入字节为空直接返回
        if (pageBase64 == null || pageBase64.isEmpty()) {
            return vllmParse;
        }

        try {
            // 1. 前置上下文处理：空值转为空字符串，避免null异常
            String validFrontContext = frontContext == null ? "" : frontContext.trim();

            //构造图片内容节点
            Map<String, Object> imageContent = Collections.singletonMap("image", pageBase64);
            // 构造文本Prompt
            String textPrompt = String.format("你是专业的文本解析助手，严格执行以下指令：\n" +
                            "处理要求：\n" +
                            "1. 因为一个chunk可能被切分在多个页面，所以先将【上一页未处理上下文】内容拼接在【当前页内容】的第一段,形成完整文本流；\n" +
                            "2. 将完整文本流按段落切分，过滤掉页码（如-第X页-）、页眉页脚等无关内容；\n" +
                            "3. 仅返回标准JSON，无任何解释、备注，结构：{\"complete_chunks\": [\"文本段1\"]}；\n" +
                            "4. 严格忠于原文，不编造、不篡改、不增删任何字符。\n"+
                            "5. 对每一个表格,都将其内容合并放在一个段落,并用json结构化的自然语言描述\n"+
                            "6. 对于图片内容,使用自然语言描述\n"+
                            "【上一页未处理上下文】：%s\n" +
                            "【当前页内容】：图片中的文本内容\n" ,
                    validFrontContext.isEmpty() ? "无" : validFrontContext);
            Map<String, Object> textContent = Collections.singletonMap("text", textPrompt);

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(imageContent, textContent)) // 图片在前，文本Prompt在后
                    .build();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(apiKey) // 从配置读取API Key
                    .model("qwen3-vl-plus") // 官网示例的视觉模型
                    .messages(Collections.singletonList(userMessage)) // 多模态消息列表
                    .temperature(0.01F) // 低温度保证结果稳定
                    .responseFormat(ResponseFormat.from("json_object"))
                    .build();

            MultiModalConversation conv = new MultiModalConversation();
            MultiModalConversationResult result = conv.call(param);

            // 解析嵌套的JSON结果
            Object contentFirst = result.getOutput().getChoices().getFirst().getMessage().getContent().getFirst();
            logger.info("模型返回原始JSON：{}，fileId:{}", contentFirst, fileEntity.getId());
            String jsonResult = "";
            if (contentFirst instanceof Map) {
                Map<?, ?> contentMap = (Map<?, ?>) contentFirst;
                Object textObj = contentMap.get("text");
                if (textObj != null) {
                    jsonResult = textObj.toString().trim();
                }
            } else {
                jsonResult = contentFirst.toString().trim();
            }

            // 清理JSON
            jsonResult = jsonResult
                    .replaceAll("^```json|```$", "")
                    .replaceAll("\\n", "")
                    .replaceAll("\\s+", " ")
                    .trim();

            // 解析JSON为Map
            Map<String, Object> parseResult = objectMapper.readValue(jsonResult,
                    new TypeReference<Map<String, Object>>() {});

            // 填充complete_chunks
            Object chunksObj = parseResult.get("complete_chunks");
            if (chunksObj instanceof List<?>) {
                List<?> rawChunks = (List<?>) chunksObj;
                for (Object chunk : rawChunks) {
                    if (chunk instanceof String && !((String) chunk).isEmpty()) {
                        String chunkStr = ((String) chunk).trim();
                        // 过滤页码标记
                        if (!chunkStr.startsWith("-第") && !chunkStr.endsWith("-")) {
                            vllmParse.getChunks().add(chunkStr);
                        }
                    }
                }
            }

            // 填充unfinished_context
            vllmParse.setUnprocessed(vllmParse.getChunks().getLast());

            vllmParse.getChunks().removeLast();
            logger.info("解析成功：fileId={}，提取到{}个有效chunk，未处理上下文：{}",
                    fileEntity.getId(), vllmParse.getChunks().size(), vllmParse.getUnprocessed());
            return vllmParse;


        } catch (Exception e) {
            logger.info("LLM 调用失败：{}", e.getMessage());
            return null;
        }
    }

    @Override
    public VLLMParse parse(String pageBase64, String frontContext, TFile fileEntity) {
        try {
            // 1. 前置上下文处理：空值转为空字符串，避免null异常
            String validFrontContext = frontContext == null ? "" : frontContext.trim();

            // 构造图片内容节点
            Map<String, Object> imageContent = Collections.singletonMap("image", pageBase64);
            // 构造文本Prompt
            String textPrompt = String.format("你是专业的文本解析助手，严格执行以下指令：\n" +
                            "处理要求：\n" +
                            "1. 因为一个chunk可能被切分在多个页面，所以先将【上一页未处理上下文】内容拼接在【当前页内容】的第一段,形成完整文本流；\n" +
                            "2. 将完整文本流按段落切分，过滤掉页码（如-第X页-）、页眉页脚等无关内容；\n" +
                            "3. 仅返回标准JSON，无任何解释、备注，结构：{\"complete_chunks\": [\"文本段1\"]}；\n" +
                            "4. 严格忠于原文，不编造、不篡改、不增删任何字符。\n"+
                            "5. 对每一个表格,都将其内容合并放在一个段落,并用json结构化的自然语言描述\n"+
                            "6. 对于图片内容,使用自然语言描述\n"+
                            "【上一页未处理上下文】：%s\n" +
                            "【当前页内容】：图片中的文本内容\n" ,
                    validFrontContext.isEmpty() ? "无" : validFrontContext);
            Map<String, Object> textContent = Collections.singletonMap("text", textPrompt);

            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage userMessage = MultiModalMessage.builder()
                        .role(Role.USER.getValue())
                        .content(Arrays.asList(imageContent, textContent)) // 图片在前，文本Prompt在后
                        .build();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(apiKey)
                    .model("qwen-vl-max-latest")  // 此处以qwen3-vl-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
                    .messages(Collections.singletonList(userMessage))
                    .build();
            MultiModalConversationResult result = conv.call(param);

            // 解析嵌套的JSON结果
            Object contentFirst = result.getOutput().getChoices().getFirst().getMessage().getContent().getFirst();
            logger.info("模型返回原始JSON：{}，fileId:{}", contentFirst, fileEntity.getId());
            String jsonResult = "";
            return null;
        } catch (NoApiKeyException | UploadFileException e) {
            throw new RuntimeException(e);
        }
    }
}
