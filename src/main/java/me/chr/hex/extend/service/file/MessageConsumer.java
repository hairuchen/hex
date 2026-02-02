package me.chr.hex.extend.service.file;


/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
public interface MessageConsumer {
    /**
     * 消费文件解析消息
     * @param fileId 文件UUID
     */
    void consumeParseMessage(String fileId);
}
