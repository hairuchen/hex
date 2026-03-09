package me.chr.hex.extend.properties.rabbitmq;


/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * 定义：交换机、队列、绑定关系
 */
@Configuration
public class RabbitMqConfig {
    // 交换机名称
    public static final String FILE_EXCHANGE = "hex.file";
    public static final String CHUNK_EXCHANGE = "hex.chunk";

    // 队列名称
    public static final String FILE_PARSE_QUEUE = "file.parse.queue";
    public static final String CHUNK_PARSE_QUEUE = "chunk.parse.queue";

    // 路由键
    public static final String FILE_PARSE_ROUTING_KEY = "file.parse";
    public static final String CHUNK_PARSE_ROUTING_KEY = "chunk.parse";

    // 声明交换机（Direct 模式）
    @Bean
    public DirectExchange fileExchange() {
        return new DirectExchange(FILE_EXCHANGE, true, false);
    }
    @Bean
    public DirectExchange chunkExchange() {
        return new DirectExchange(CHUNK_EXCHANGE, true, false);
    }

    // 声明队列
    @Bean
    public Queue fileParseQueue() {
        // durable: 是否持久化（重启不丢失）
        return new Queue(FILE_PARSE_QUEUE, true);
    }
    @Bean
    public Queue chunkParseQueue() {
        // durable: 是否持久化（重启不丢失）
        return new Queue(CHUNK_PARSE_QUEUE, true);
    }

    // 绑定：队列 -> 交换机，使用路由键
    //TODO：绑定到文件推送到mongodb
    @Bean
    public Binding bindingFileParseQueue() {
        return BindingBuilder
                .bind(fileParseQueue())
                .to(fileExchange())
                .with(FILE_PARSE_ROUTING_KEY);
    }
    @Bean
    public Binding bindingChunkParseQueue() {
        return BindingBuilder
                .bind(chunkParseQueue())
                .to(chunkExchange())
                .with(CHUNK_PARSE_ROUTING_KEY);
    }
}
