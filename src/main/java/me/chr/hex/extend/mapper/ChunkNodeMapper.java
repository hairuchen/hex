package me.chr.hex.extend.mapper;


import me.chr.hex.extend.BO.ChunkNode;
import me.chr.hex.extend.VO.RetrieveResponseVO;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/3/10
 **/
@Repository
public interface ChunkNodeMapper extends Neo4jRepository<ChunkNode, String> {

    /*
        持久化 ChunkNode 间 Relation    A ——> B
     */
    @Query("""
            MATCH (a:ChunkNode {id: $fromId})
            MATCH (b:ChunkNode {id: $toId})
            CREATE (a)-[:NEXT_CHUNK {order: 1}]->(b)
        """)
    void createNextChunkRelation(@Param("fromId") String fromId, @Param("toId") String toId);


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
    CALL db.index.vector.queryNodes('chunk_embedding_index', $topN, $queryVector)
    YIELD node AS chunk, score
    WHERE chunk.isDelete = false
    RETURN chunk AS chunkNode, score
    ORDER BY score DESC
    """)
    List<RetrieveResponseVO> findTopByVectorSimilarityWithScore(
            @Param("queryVector") List<Double> queryVector,
            @Param("topN") int topN
    );
}
