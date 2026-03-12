package me.chr.hex.extend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.OSS.service.OssService;
import me.chr.hex.extend.BO.EntityNode;
import me.chr.hex.extend.BO.FileEntity;
import me.chr.hex.extend.mapper.ChunkNodeMapper;
import me.chr.hex.extend.BO.ChunkNode;
import me.chr.hex.extend.Enum.KnowledgeNodeTypeEnum;
import me.chr.hex.extend.mapper.EntityNodeMapper;
import me.chr.hex.extend.properties.rabbitmq.RabbitMqConfig;
import me.chr.hex.extend.service.*;
import me.chr.hex.general.entity.File;
import me.chr.hex.general.service.IFileService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/
@Slf4j
@Service
public class Rabbitmq implements MessageProducer, MessageConsumer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Lazy
    @Autowired
    private IFileService fileService;
    @Autowired
    private OssService ossService;

    @Autowired
    private ChunkNodeMapper chunkNodeMapper;
    @Autowired
    private EntityNodeMapper entityNodeMapper;

    @Autowired
    private FileParse fileParse;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private ParseModel parseModel;

    @Override
    public String getMqType() {
        return "RabbitMQ";
    }

    @Override
    public void sendFileParseMessage(FileEntity fileEntity){
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.FILE_EXCHANGE,           // 交换机
                    RabbitMqConfig.FILE_PARSE_ROUTING_KEY,  // 路由键
                    fileEntity.getId().toString()           // 消息体
            );
            log.info("【{}生产者】消息发送成功，fileId: {}", this.getMqType(), fileEntity.getId());
        }catch (Exception e){
            log.error("【{}生产者】消息发送失败,fileId: {}", this.getMqType(), fileEntity.getId());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendChunkParseMessage(ChunkNode chunkNode) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.CHUNK_EXCHANGE,                  // 交换机
                    RabbitMqConfig.CHUNK_PARSE_ROUTING_KEY,         // 路由键
                    chunkNode.getId()                               // 消息体
            );
            log.info("【{}生产者】消息发送成功,chunkId: {}", this.getMqType(), chunkNode.getId());
        } catch (Exception e) {
            log.error("【{}生产者】消息发送失败,chunkId: {}", this.getMqType(), chunkNode.getId());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void consumeFileParseMessage(String fileId) {
        try {
            log.info("【{}消费者】文件解析开始处理，fileId: {}", this.getMqType(), fileId);
            // 1. 根据 fileId 查询数据库
            File file=fileService.getOne(new LambdaQueryWrapper<File>().eq(File::getId,fileId));
            fileService.updateProcess(fileId, (short) 2);
            // 2. 从 MinIO 下载文件
            byte[] fileBytes = ossService.download(file.getMinioBucket(),file.getMinioObjectName()); // 下载文件字节
            // 3. 从 File 解析出 Chunk 列表
            List<String> chunkList=fileParse.parse(fileBytes,file);
            fileService.updateProcess(file.getId(), (short) 10);
            // 4. 使用 ChunkNode 持久化 Chunk 列表
            List<ChunkNode> chunkNodeList=new ArrayList<>();
            for (String str:chunkList){
                //向量化 & 归一化
                List<Double> vector=embeddingModel.normalization(str,embeddingModel.embed(str));
                ChunkNode chunkNode = new ChunkNode(file.getId(),str, KnowledgeNodeTypeEnum.TEXT,vector,"chr");
                chunkNodeMapper.save(chunkNode);
                chunkNodeList.add(chunkNode);
            }
            fileService.updateProcess(file.getId(), (short) 20);
            // 5. 持久化 ChunkNode 间 relation
            for (int i = 0; i < chunkNodeList.size(); i++) {
                String currentId = chunkNodeList.get(i).getId();
                String nextId = (i + 1 < chunkNodeList.size()) ? chunkNodeList.get(i + 1).getId() : null;
                chunkNodeMapper.createNextChunkRelation(currentId, nextId);
            }
            fileService.updateProcess(file.getId(), (short) 40);
            // 6. 数据挖掘——路径召回激活
            if(file.getIsExcavate()){
                for (ChunkNode chunkNode:chunkNodeList){
                    sendChunkParseMessage(chunkNode);
                }
            }
            // 解析完成更新进度
            fileService.updateProcess(file.getId(), (short) 100);
            log.info("【{}消费者】文件解析完成，fileId: {}", this.getMqType(), fileId);
        } catch (Exception e) {
            fileService.updateProcess(fileId, (short) -1);
            log.error("【{}消费者】处理文件解析失败，fileId: {} ", this.getMqType(), fileId , e);
        }
    }

    @Override
    public void consumeChunkParseMessage(String chunkId) {
        log.info("【{}消费者】chunk分析开始处理，chunkId: {}", this.getMqType(), chunkId);
        try {
            // 1. 获取 ChunkNode 节点对象
            ChunkNode chunkNode=chunkNodeMapper.findById(chunkId).orElse(null);
            if (chunkNode == null) {
                return;
            }
            // 2. 从 chunk 提取 EntityNode 并 持久化
            List<String> entityList=parseModel.chunkToEntity(chunkNode.getContent());
            int index=0;
            for (String str:entityList){
                str=str.toLowerCase();
                EntityNode entityNode;
                if (entityNodeMapper.existsByContent(str)){
                    entityNode = entityNodeMapper.findByContent(str).orElse(null);
                }else{
                    //向量化 & 归一化
                    List<Double> vector=embeddingModel.normalization(str,embeddingModel.embed(str));
                    entityNode=new EntityNode(str,KnowledgeNodeTypeEnum.ENTITY,vector,"chr");
                    entityNodeMapper.save(entityNode);
                }
                if (entityNode != null) {
                    entityNodeMapper.createChunkContainEntityRelation(chunkId,entityNode.getId(),index);
                }
                index++;
            }
            log.info("【{}消费者】chunk分析完成,chunkId: {}", this.getMqType(), chunkId);
        } catch (Exception e) {
            log.error("【{}消费者】chunk分析失败，chunkId: {}", this.getMqType(), chunkId);
        }
    }

}