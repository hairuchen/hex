package me.chr.hex.extend.properties.kb;


/**
 * @Author: CHR
 * @Date: create in 2026/2/5
 **/

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 多路召回权重配置
 */
@Component
@ConfigurationProperties(prefix = "knowledge.retrieve")
@Data
public class RetrieveWeightProperties {
    // 文本匹配权重
    private Double textWeight = 0.3;
    // 向量匹配权重
    private Double vectorWeight = 0.7;
    // 最终返回topN
    private Integer finalTopN = 10;
}
