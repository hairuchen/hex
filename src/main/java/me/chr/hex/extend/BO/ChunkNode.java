package me.chr.hex.extend.BO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.chr.hex.extend.Enum.KnowledgeNodeTypeEnum;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Node("ChunkNode")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChunkNode {
    /**
     * 知识片段主键 ID
     */
    @Id
    private String id;

    /**
     * 来源文件ID（关联file表的主键）
     */
    private String fileId;

    /**
     * 知识片段原文
     */
    private String content;

    /**
     * 知识片段类型（关联KnowledgeNodeTypeEnum）
     */
    private String type;

    /**
     * 向量字段
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
     * 启用状态
     */
    private String status;

    /**
     * 逻辑删除标识（false：未删除，true：已删除）
     */
    private Boolean isDelete;

    public ChunkNode(String fileId, String content, KnowledgeNodeTypeEnum typeEnum, List<Double> vector, String creator){
        this.id= UUID.randomUUID().toString();
        this.fileId=fileId;
        this.content=content;
        this.type=typeEnum.toString();
        this.vector=vector;
        this.createTime=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.creator=creator;
        this.status="1";
        this.isDelete=Boolean.FALSE;
    }

}
