package me.chr.hex.extend.controller;

import lombok.extern.slf4j.Slf4j;
import me.chr.hex.extend.properties.rabbitmq.RabbitMqConfig;
import me.chr.hex.extend.service.MessageConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: CHR
 * @Date: create in 2026/3/9
 **/
@Slf4j
@Component
public class MQController {

    @Autowired
    private MessageConsumer messageConsumer;

    /**
     * 监听队列，实时消费
     * 这是一个监听方法，不是对外开放的接口
     */
    @RabbitListener(queues = RabbitMqConfig.FILE_PARSE_QUEUE)
    public void receiveFileMessage(String fileId) {
        log.info("消费服务由: {} 提供",messageConsumer.getMqType());
        messageConsumer.consumeFileParseMessage(fileId);
    }

    /**
     * 监听队列，实时消费
     * 这是一个监听方法，不是对外开放的接口
     */
    @RabbitListener(queues = RabbitMqConfig.CHUNK_PARSE_QUEUE)
    public void receiveChunkMessage(String chunkId) {
        log.info("消费服务由: {} 提供",messageConsumer.getMqType());
        messageConsumer.consumeChunkParseMessage(chunkId);
    }
}
