package me.chr.hex.extend.dao;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Data
public class KnowledgeChunkNode {
    private String chunkId;      // 知识片段唯一ID
    private String fileId;       // 关联文件ID
    private String content;      // 内容
    private String type;         // TEXT / IMAGE / PDF
    private Integer index;       // 顺序
    private Integer tokenCount;  // 字符数/Token数
    private LocalDateTime createTime;
    private String source;       // UPLOAD / CRAWLER
    private String status;       // NORMAL / DEPRECATED
}
