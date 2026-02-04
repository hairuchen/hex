package me.chr.hex.extend.properties.neo4j;


/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/

import lombok.Getter;

/**
 * 知识片段类型枚举
 */
@Getter
public enum ChunkTypeEnum {
    TEXT("TEXT" ),
    IMAGE("IMAGE"),
    VOICE("VOICE"),
    VIDEO("VIDEO"); // 预留扩展

    private final String type;

    ChunkTypeEnum(String type) {
        this.type=type;
    }

}
