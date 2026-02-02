package me.chr.hex.extend.service.kb.impl;


import me.chr.hex.extend.dao.KnowledgeChunkNode;
import me.chr.hex.extend.service.kb.KnowledgeService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: CHR
 * @Date: create in 2026/2/4
 **/
@Service
public class Neo4jKnowledgeServiceImpl implements KnowledgeService {

    @Autowired
    private Driver neo4jDriver;

    @Override
    public void createChunk(KnowledgeChunkNode node) {
        String cypher = """
                CREATE (c:KnowledgeChunk {
                    chunkId: $chunkId,
                    fileId: $fileId,
                    content: $content,
                    type: $type,
                    index: $index,
                    tokenCount: $tokenCount,
                    createTime: $createTime,
                    source: $source,
                    status: $status
                })
                """;

        try (var session = neo4jDriver.session()) {
            session.run(cypher, Values.parameters(
                    "chunkId", node.getChunkId(),
                    "fileId", node.getFileId(),
                    "content", node.getContent(),
                    "type", node.getType(),
                    "index", node.getIndex(),
                    "tokenCount", node.getTokenCount(),
                    "createTime", node.getCreateTime().toString(),
                    "source", node.getSource(),
                    "status", node.getStatus()
            ));
        }
    }

    @Override
    public void createNextChunkRelation(String fromChunkId, String toChunkId) {
        String cypher = """
                MATCH (a:KnowledgeChunk {chunkId: $fromId}), (b:KnowledgeChunk {chunkId: $toId})
                CREATE (a)-[:NEXT_CHUNK {order: $order}]->(b)
                """;

        try (var session = neo4jDriver.session()) {
            session.run(cypher, Values.parameters(
                    "fromId", fromChunkId,
                    "toId", toChunkId,
                    "order", 1
            ));
        }
    }
}
