package me.chr.hex.extend.mapper;


import me.chr.hex.extend.properties.neo4j.KnowledgeChunkNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
public interface KnowledgeChunkRepository extends Neo4jRepository<KnowledgeChunkNode, String> {

    @Query("""
        MATCH (c:KnowledgeChunk)
        WHERE c.isDelete = false AND c.content CONTAINS $query
        RETURN c
        ORDER BY size(c.content) DESC
        LIMIT $topN
        """)
    List<KnowledgeChunkNode> findTopByContentContaining(
            @Param("query") String query,
            @Param("topN") int topN
    );

    @Query("""
        MATCH (a:KnowledgeChunk {id: $fromId}), (b:KnowledgeChunk {id: $toId})
        CREATE (a)-[:NEXT_CHUNK {order: 1}]->(b)
        """)
    void createNextChunkRelation(@Param("fromId") String fromId, @Param("toId") String toId);
}
