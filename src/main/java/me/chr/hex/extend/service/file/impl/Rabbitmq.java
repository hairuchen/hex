package me.chr.hex.extend.service.file.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.chr.hex.extend.BO.AbstractEntityNode;
import me.chr.hex.extend.BO.FileEntity;
import me.chr.hex.extend.BO.AbstractRelationEdge;
import me.chr.hex.extend.properties.neo4j.ChunkNode;
import me.chr.hex.extend.properties.neo4j.EntityNode;
import me.chr.hex.extend.properties.neo4j.KnowledgeNodeTypeEnum;
import me.chr.hex.extend.properties.rabbitmq.RabbitMqConfig;
import me.chr.hex.extend.service.file.FileRepository;
import me.chr.hex.extend.service.file.FileStorage;
import me.chr.hex.extend.service.file.MessageConsumer;
import me.chr.hex.extend.service.file.MessageProducer;
import me.chr.hex.extend.service.kb.GraphKnowledgeService;
import me.chr.hex.extend.service.model.AbstractModel;
import me.chr.hex.extend.service.model.AbstractObject;
import me.chr.hex.extend.service.model.EmbeddingModel;
import me.chr.hex.general.entity.TFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/
@Service
public class Rabbitmq implements MessageProducer, MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(Rabbitmq.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileStorage fileStorage;
    @Autowired
    private GraphKnowledgeService graphKnowledgeService;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private AbstractModel abstractModel;

    @Override
    public void sendParseMessage(FileEntity fileEntity) throws Exception {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.FILE_EXCHANGE,       // 交换机
                RabbitMqConfig.FILE_PARSE_ROUTING_KEY, // 路由键
                fileEntity.getId().toString()                            // 消息体
        );
        logger.info("RabbitMQ 消息发送成功，fileId: {}", fileEntity.getId());
    }

    @Override
    public void consumeParseMessage(String fileId) {
        try {
            logger.info("【消费】开始处理文件解析，fileId: {}", fileId);
            // 1. 根据 fileId 查询数据库
            TFile fileEntity=fileRepository.getOne(new LambdaQueryWrapper<TFile>().eq(TFile::getId,fileId));
            if (fileEntity == null) {
                logger.error("【消费】文件记录不存在，fileId: {}", fileId);
                return;
            }
            fileRepository.updateProcess(fileId, (short) 2);
            // 2. 从 MinIO 下载文件
            String objectPath = fileEntity.getMinioObjectPath()+fileEntity.getId(); // 从数据库获取存储路径
            byte[] fileBytes = fileStorage.download(objectPath); // 下载文件字节
            logger.info("【消费】从 MinIO 下载文件成功，大小：{} byte", fileBytes.length);
            // 3. 解析文件内容
            Boolean parseStatus = parseFile(fileBytes, fileEntity);

            logger.info("【消费】文件解析完成，fileId: {}", fileId);

        } catch (Exception e) {
            logger.error("【消费】处理失败，fileId: {}", fileId, e);
             throw e;
        }
    }

    @Override
    public String getMqType() {
        return "rabbitmq";
    }

    private Boolean parseFile(byte[] bytes, TFile file) {
        String fileId = file.getId().toString();
        fileRepository.updateProcess(fileId, (short) 4);
        logger.info("【解析】文件处理中，fileId: {}", fileId);

        // ===================== 2. 按换行符切割文本 =====================
        String content = new String(bytes, StandardCharsets.UTF_8);
        String[] chunks = content.split("\n");
        // ===================== 3. 存入 Neo4j（知识片段 + 顺序关系） =====================
        String previousChunkId = null;
        int index = 0;

        for (String chunk : chunks) {
            // 跳过空行
            if (chunk == null || chunk.isBlank()) continue;

            // 构建知识片段节点
            List<Double> vector=embeddingModel.embed(chunk);
            ChunkNode node = new ChunkNode(fileId,chunk.trim(), KnowledgeNodeTypeEnum.TEXT,vector,"chr");

            // 保存到 Neo4j
            graphKnowledgeService.createChunk(node);

            // 构建 NEXT_CHUNK 顺序关系
            if (previousChunkId != null) {
                graphKnowledgeService.createNextChunkRelation(previousChunkId, node.getId());
            }

            //进一步抽象知识图谱
            AbstractObject entityAndRelationMaps = abstractModel.extractEntitiesAndRelations(node.getContent());
            List<AbstractEntityNode> eNodeList=entityAndRelationMaps.getENode();
            for (AbstractEntityNode abstractEntityNode :eNodeList){
                List<Double> entityVector=embeddingModel.embed(abstractEntityNode.getName());
                EntityNode knowledgeEntityNode=new EntityNode(abstractEntityNode.getName(), KnowledgeNodeTypeEnum.ENTITY,entityVector,"chr");
                graphKnowledgeService.createEntityNode(knowledgeEntityNode);
                index++;
                graphKnowledgeService.createChunkToEntityRelation(node.getId(), knowledgeEntityNode.getContent());
                index++;
            }
//            List<AbstractRelationEdge> abstractRelationEdgeList =entityAndRelationMaps.getRx();
            index++;
            previousChunkId = node.getId();
        }

        // ===================== 4. 更新状态：处理成功 =====================
        fileRepository.updateProcess(fileId, (short) 100);
        logger.info("【解析】文件处理成功，共生成 {} 个知识片段", index);

        return true;
    }
}
