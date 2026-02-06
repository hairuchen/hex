package me.chr.hex.extend.properties.neo4j;


import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * @Author: CHR
 * @Date: create in 2026/2/6
 **/
@Node("EntityNode")
@Data
public class EntityNode {
    /**
     * 知识片段主键ID
     */
    @Id
    private String id;

    /**
     * 知识片段原文
     */
    private String content;

    /**
     * 知识片段类型（关联ChunkTypeEnum）
     */
    private String type;

    /**
     * 向量字段（PostgreSQL可结合pgvector插件，这里用text存储向量字符串，也可自定义类型）
     */
    private List<Double> vector;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 状态（预留扩展）
     */
    private String status;

    /**
     * 逻辑删除标识（false：未删除，true：已删除）
     */
    private Boolean isDelete;

    public EntityNode(String content, KnowledgeNodeTypeEnum typeEnum, List<Double> vector, String creator){
        this.id= UUID.randomUUID().toString();
        this.content=content;
        this.type=typeEnum.toString();
        this.vector=vector;
        this.createTime= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.creator=creator;
        this.status="1";
        this.isDelete=Boolean.FALSE;
    }

    public EntityNode() {
    }
}
