package me.chr.hex.extend.BO;


import lombok.Data;

/**
 * @Author: CHR
 * @Date: create in 2026/2/6
 **/
@Data
public class AbstractEntityNode {
    /**
     * 实体名称（如：Spring Boot）
     */
    private String name;

    /**
     * 实体类型（如：框架、技术、人物）
     */
    private String type;
}
