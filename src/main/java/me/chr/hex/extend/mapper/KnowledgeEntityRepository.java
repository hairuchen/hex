package me.chr.hex.extend.mapper;


import me.chr.hex.extend.properties.neo4j.EntityNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

/**
 * @Author: CHR
 * @Date: create in 2026/2/6
 **/
public interface KnowledgeEntityRepository extends Neo4jRepository<EntityNode, String> {

    // 按 content 判断是否存在
    @Query("""
        MATCH (e:EntityNode {content: $content, isDelete: false})
        RETURN count(e) > 0
    """)
    boolean existsByContent(@Param("content") String content);

    // 创建实体间关系

}
