package me.chr.hex.extend.service;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
public interface MessageConsumer {

    /**
     * 获取消息队列类型
     * @return 如RABBITMQ/KAFKA
     */
    String getMqType();
    /**
     * 消费文件解析消息
     * @param fileId 文件 UUID
     */
    void consumeFileParseMessage(String fileId);

    /**
     * 消费文件解析消息
     * @param chunkId 段落 UUID
     */
    void consumeChunkParseMessage(String chunkId);
}
