package me.chr.hex.extend.BO;


import lombok.Data;

/**
 * @Author: CHR
 * @Date: create in 2026/2/6
 **/
@Data
public class AbstractRelationEdge {
    /**
     * 源实体名称
     */
    private String source;

    /**
     * 目标实体名称
     */
    private String target;

    /**
     * 关系类型（如：依赖、包含、作者）
     */
    private String relation;
}
