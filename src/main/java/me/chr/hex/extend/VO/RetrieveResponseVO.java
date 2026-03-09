package me.chr.hex.extend.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.chr.hex.extend.BO.ChunkNode;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrieveResponseVO {

    @Schema(description = "返回节点")
    private ChunkNode chunkNode;
    @Schema(description = "评分")
    private Double score;

    public RetrieveResponseVO(ChunkNode chunkNode){
        this.chunkNode=chunkNode;
        this.score=1D;
    }
}
