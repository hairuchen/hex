package me.chr.hex.extend.controller;

import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.core.R.Response.ResultCode;
import me.chr.hex.extend.properties.rabbitmq.RabbitMqConfig;
import me.chr.hex.extend.service.file.MessageConsumer;
import me.chr.hex.extend.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: CHR
 * @Date: create in 2026/1/29
 **/
@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private MessageConsumer messageConsumer;


    @PostMapping("/upload")
    @ResponseBody
    public CommonResult<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return CommonResult.failure(ResultCode.FAILED,"文件为空");
        }

        fileService.uploadFile(file);

        return CommonResult.success("文件已提交，正在后台处理...");

    }


    /**
     * 监听队列，实时消费
     * 这是一个监听方法，不是对外开放的接口
     */
    @RabbitListener(queues = RabbitMqConfig.FILE_PARSE_QUEUE)
    public void receiveMessage(String fileId) {
        // 只做一件事：调用业务层接口
        messageConsumer.consumeParseMessage(fileId);
    }
}
