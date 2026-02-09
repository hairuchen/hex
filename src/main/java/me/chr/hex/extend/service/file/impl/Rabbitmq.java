package me.chr.hex.extend.service.file.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.BO.AbstractEntityNode;
import me.chr.hex.extend.BO.FileEntity;
import me.chr.hex.extend.properties.minio.ChunkMessage;
import me.chr.hex.extend.properties.neo4j.ChunkNode;
import me.chr.hex.extend.properties.neo4j.EntityNode;
import me.chr.hex.extend.properties.neo4j.KnowledgeNodeTypeEnum;
import me.chr.hex.extend.properties.rabbitmq.RabbitMqConfig;
import me.chr.hex.extend.service.file.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private FileParse fileParse;
    @Autowired
    private GraphKnowledgeService graphKnowledgeService;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private AbstractModel abstractModel;

    @Override
    public void sendFileParseMessage(FileEntity fileEntity){
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.FILE_EXCHANGE,           // 交换机
                RabbitMqConfig.FILE_PARSE_ROUTING_KEY,  // 路由键
                fileEntity.getId().toString()           // 消息体
        );
        logger.info("【{}生产者】消息发送成功，fileId: {}", this.getMqType(), fileEntity.getId());
    }

    @Override
    public void sendChunkParseMessage(ChunkMessage chunkMessage) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.CHUNK_EXCHANGE,           // 交换机
                RabbitMqConfig.CHUNK_PARSE_ROUTING_KEY,  // 路由键
                chunkMessage                                   // 消息体
        );
        logger.info("【{}生产者】消息发送成功,chunkId: {},chunkContext: {}", this.getMqType(), chunkMessage.getChunkId(),chunkMessage.getChunk());
    }

    @Override
    public void consumeFileParseMessage(String fileId) {
        try {
            logger.info("【{}消费者】文件切分开始处理，fileId: {}", this.getMqType(), fileId);
            // 1. 根据 fileId 查询数据库
            TFile fileEntity=fileRepository.getOne(new LambdaQueryWrapper<TFile>().eq(TFile::getId,fileId));
            if (fileEntity == null) {
                logger.error("【{}消费者】文件对象不存在，fileId: {}", this.getMqType(), fileId);
                return;
            }
            fileRepository.updateProcess(fileId, (short) 2);
            // 2. 从 MinIO 下载文件
            String objectPath = fileEntity.getMinioObjectPath()+fileEntity.getId(); // 从数据库获取存储路径
            byte[] fileBytes = fileStorage.download(objectPath); // 下载文件字节
            logger.info("【{}消费者】从【{}】下载文件内容成功，大小：{} byte", this.getMqType(),fileStorage.getStorageType(), fileBytes.length);
            // 3. 解析文件内容
            this.parseFile(fileBytes, fileEntity);
            logger.info("【{}消费者】文件切分完成，fileId: {}", this.getMqType(), fileId);
        } catch (Exception e) {
            logger.error("【{}消费者】处理文件切分失败，fileId: {}", this.getMqType(), fileId);
            throw new BizException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public void consumeChunkParseMessage(ChunkMessage chunkMessage) {
        try {
            logger.info("【{}消费者】chunk解析开始处理，chunkId: {}", this.getMqType(), chunkMessage.getChunkId());
            // 跳过空行
            if (chunkMessage.getChunk() == null || chunkMessage.getChunk().isBlank()) {
                return;
            }
            this.parseChunk(chunkMessage);
            //TODO:引入redis存储解析进度
            logger.info("【{}消费者】chunk解析完成,进度：x/x", this.getMqType());
        } catch (Exception e) {
            logger.error("【{}消费者】chunk解析失败，chunk内容: {}", this.getMqType(), chunkMessage.getChunk());
            throw new BizException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public String getMqType() {
        return "rabbitmq";
    }

    private void parseFile(byte[] fileBytes, TFile fileEntity) {
        try {
            logger.info("【{}消费者】开始切分，fileId: {},fileName: {}",  this.getMqType(), fileEntity.getId(),fileEntity.getFileName());

            //TODO:下沉chunk推送
            List<String> chunkList=fileParse.parse(fileBytes,fileEntity);

            if (chunkList == null || chunkList.isEmpty()) {
                logger.warn("【{}消费者】文件切分结果为空，fileId: {}", this.getMqType(), fileEntity.getId());
                fileRepository.updateProcess(fileEntity.getId().toString(), (short) 100); // 视为成功（无内容）
                return;
            }
            //构建 ChunkMessage 并推送
            List<String> chunkIds = new ArrayList<>();
            for (int i = 0; i < chunkList.size(); i++) {
                chunkIds.add(UUID.randomUUID().toString());
            }
            for (int i = 0; i < chunkList.size(); i++) {
                String chunkContent = chunkList.get(i);
                if (chunkContent == null || chunkContent.isBlank()) {
                    continue; // 跳过空 chunk
                }

                String currentId = chunkIds.get(i);
                String nextId = (i + 1 < chunkIds.size()) ? chunkIds.get(i + 1) : null;

                ChunkMessage message = new ChunkMessage(fileEntity.getId().toString(), chunkContent, currentId, nextId);
                sendChunkParseMessage(message);
            }
            // 解析完成更新进度
            fileRepository.updateProcess(fileEntity.getId().toString(), (short) 4);
            logger.info("【{}消费者】切分结果，fileId: {}, fileName: {}, 总分片数量:{}",  this.getMqType(), fileEntity.getId(),fileEntity.getFileName(),chunkList.size());
        }catch (Exception e){
            logger.error("【{}消费者】文件切分异常，fileId: {}",  this.getMqType(), fileEntity.getId());
            // 标记解析失败
            fileRepository.updateProcess(fileEntity.getId().toString(), (short) -1);
            throw new BizException("文件切分异常：" + e.getMessage());
        }
    }

    private void parseChunk(ChunkMessage chunkMessage){
        List<Double> vector=embeddingModel.embed(chunkMessage.getChunk());
        ChunkNode chunkNode = new ChunkNode(chunkMessage.getChunkId(),chunkMessage.getFileId(),chunkMessage.getChunk(), KnowledgeNodeTypeEnum.TEXT,vector,"chr");
        // 存储 chunk 节点
        graphKnowledgeService.createChunk(chunkNode);
        if (chunkMessage.getNextId() != null) {
            //存储 chunk 间关系
            graphKnowledgeService.createNextChunkRelation(chunkNode.getId(), chunkMessage.getNextId());
        }
        //进一步抽象知识图谱
        AbstractObject entityAndRelationMaps = abstractModel.extractEntitiesAndRelations(chunkNode.getContent());
        List<AbstractEntityNode> eNodeList=entityAndRelationMaps.getENode();
        //TODO:存储实体间关系
        for (AbstractEntityNode abstractEntityNode : eNodeList) {
            //存储 entity 节点
            List<Double> entityVector = embeddingModel.embed(abstractEntityNode.getName());
            //TODO:升级实体类型为细分类型
            EntityNode knowledgeEntityNode = new EntityNode(abstractEntityNode.getName(), KnowledgeNodeTypeEnum.ENTITY, entityVector, "chr");
            graphKnowledgeService.createEntityNode(knowledgeEntityNode);
            //存储 chunk 到 entity 节点
            graphKnowledgeService.createChunkToEntityRelation(chunkNode.getId(), knowledgeEntityNode.getContent());
        }
        logger.info("【{}消费者】chunk解析成功,chunkId:{},共生成 {} 个实体对象",  this.getMqType(), chunkMessage.getChunkId(), eNodeList.size());
    }
}