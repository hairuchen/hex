package me.chr.hex.extend.service.file;


import me.chr.hex.extend.properties.minio.ChunkMessage;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
public interface MessageConsumer {
    /**
     * 消费文件解析消息
     * @param fileId 文件UUID
     */
    void consumeFileParseMessage(String fileId);

    /**
     * 消费文件解析消息
     * @param chunkMessage 段落文本
     */
    void consumeChunkParseMessage(ChunkMessage chunkMessage);
}
