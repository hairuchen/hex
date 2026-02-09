package me.chr.hex.extend.service.file;


/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/

import me.chr.hex.extend.BO.FileEntity;
import me.chr.hex.extend.properties.minio.ChunkMessage;

/**
 * 消息发送接口（适配RabbitMQ/Kafka/RocketMQ等）
 */
public interface MessageProducer {
    /**
     * 发送文件待解析消息
     * @param fileEntity 持久化后的文件实体
     */
    void sendFileParseMessage(FileEntity fileEntity);

    /**
     * 发送Chunk待解析消息
     * @param chunk 段落文本内容
     */
    void sendChunkParseMessage(ChunkMessage chunk) ;

    /**
     * 获取消息队列类型
     * @return 如RABBITMQ/KAFKA
     */
    String getMqType();
}
