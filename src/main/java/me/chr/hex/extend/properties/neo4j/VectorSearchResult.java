package me.chr.hex.extend.properties.neo4j;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: CHR
 * @Date: create in 2026/2/5
 **/
@Data
public class VectorSearchResult{
    // 知识块节点
    private KnowledgeChunkNode chunk;
    private Double score;

    public VectorSearchResult() {
    }
    public VectorSearchResult(KnowledgeChunkNode chunk, Double score) {
        this.chunk = chunk;
        this.score = score;
    }
    public VectorSearchResult(KnowledgeChunkNode chunk){
        this.chunk=chunk;
        this.score=1D;
    }

}
