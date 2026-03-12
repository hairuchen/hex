package me.chr.hex.extend.DTO;


/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.chr.hex.core.log.Loggable;

/**
 * 知识召回请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "RetrieveDTO", description = "召回请求 DTO对象")
public class RetrieveDTO implements Loggable {

    /**
     * 用户查询文本（必填）
     */
    @NotBlank(message = "查询文本不能为空")
    private String query;

    /**
     * 返回最相似的 topN 条（默认 5，范围 1~20）
     */
    @Min(value = 1, message = "topN 最小为 1")
    @Max(value = 20, message = "topN 最大为 20")
    private Integer topN = 5;
}
