package me.chr.hex.extend.mapper;


import me.chr.hex.extend.properties.neo4j.ChunkNode;
import me.chr.hex.extend.properties.neo4j.VectorSearchResult;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
public interface KnowledgeChunkRepository extends Neo4jRepository<ChunkNode, String> {

    @Query("""
        MATCH (c:ChunkNode)
        WHERE c.isDelete = false AND c.content CONTAINS $query
        RETURN c
        ORDER BY size(c.content) DESC
        LIMIT $topN
        """)
    List<ChunkNode> findTopByContentContaining(
            @Param("query") String query,
            @Param("topN") int topN
    );

    @Query("""
            MATCH (a:ChunkNode {id: $fromId})
            MATCH (b:ChunkNode {id: $toId})
            CREATE (a)-[:NEXT_CHUNK {order: 1}]->(b)
        """)
    void createNextChunkRelation(@Param("fromId") String fromId, @Param("toId") String toId);


    @Query("""
    CALL db.index.vector.queryNodes('chunk_embedding_index', $topN, $queryVector)
    YIELD node AS chunk, score
    WHERE chunk.isDelete = false
    RETURN chunk AS chunk, score
    ORDER BY score DESC
    """)
    List<VectorSearchResult> findTopByVectorSimilarityWithScore(
            @Param("queryVector") List<Double> queryVector,
            @Param("topN") int topN
    );

    /**
     * 创建 chunk -> ENTITY -> entity 关系
     */
    @Query("""
        MATCH (c:ChunkNode {id: $chunkId})
        MATCH (e:EntityNode {content: $entityContent})
        MERGE (c)-[:HAS_ENTITY]->(e)
        """)
    void createChunkToEntityRelation(@Param("chunkId") String chunkId, @Param("entityContent") String entityContent);

}
