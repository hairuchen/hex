package me.chr.hex.extend.service.file;


/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/

import me.chr.hex.extend.BO.FileEntity;

/**
 * 消息发送接口（适配RabbitMQ/Kafka/RocketMQ等）
 */
public interface MessageProducer {
    /**
     * 发送文件待解析消息
     * @param fileEntity 持久化后的文件实体
     * @throws Exception 发送失败抛出异常
     */
    void sendParseMessage(FileEntity fileEntity) throws Exception;

    /**
     * 获取消息队列类型
     * @return 如RABBITMQ/KAFKA
     */
    String getMqType();
}
