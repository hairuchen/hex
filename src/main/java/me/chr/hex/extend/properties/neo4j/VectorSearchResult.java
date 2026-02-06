package me.chr.hex.extend.properties.neo4j;


import lombok.Data;

/**
 * @Author: CHR
 * @Date: create in 2026/2/5
 **/
@Data
public class VectorSearchResult{
    // 知识块节点
    private ChunkNode chunk;
    private Double score;

    public VectorSearchResult() {
    }
    public VectorSearchResult(ChunkNode chunk, Double score) {
        this.chunk = chunk;
        this.score = score;
    }
    public VectorSearchResult(ChunkNode chunk){
        this.chunk=chunk;
        this.score=1D;
    }

}
