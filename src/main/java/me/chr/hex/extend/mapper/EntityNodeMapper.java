package me.chr.hex.extend.mapper;

import me.chr.hex.extend.BO.EntityNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @Author: CHR
 * @Date: create in 2026/3/11
 **/
@Repository
public interface EntityNodeMapper extends Neo4jRepository<EntityNode, String> {
    @Query("""
            MATCH (a:ChunkNode {id: $fromId})
            MATCH (b:EntityNode {id: $toId})
            CREATE (a)-[:CONTAIN {order: $order}]->(b)
        """)
    void createChunkContainEntityRelation(@Param("fromId") String fromId, @Param("toId") String toId, @Param("order") Integer order);

    // 按 content 判断是否存在
    boolean existsByContent(@Param("content") String content);

    Optional<EntityNode> findByContent(String content);

    // 创建实体之间的关系（source -> relation -> target）
//    void createEntityRelation(String sourceEntityContent, String targetEntityContent, String relationType);
}
