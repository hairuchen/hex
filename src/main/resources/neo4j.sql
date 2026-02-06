知识库中知识片段的数据模型阐述:

-- 1. 创建正确的向量索引：绑定 KnowledgeChunk 节点的 embedding 属性，1024 维，余弦相似度
CREATE VECTOR INDEX chunk_embedding_index
FOR (c:ChunkNode) ON (c.vector)
OPTIONS {
  indexConfig: {
    `vector.dimensions`: 1024,
    `vector.similarity_function`: 'COSINE'
  }
};
CREATE VECTOR INDEX entity_embedding_index
FOR (e:EntityNode) ON (e.vector)
OPTIONS {
  indexConfig: {
    `vector.dimensions`: 1024,  // 必须和ChunkNode向量维度一致
    `vector.similarity_function`: 'COSINE'
  }
};

SHOW INDEXES WHERE type = 'VECTOR';
SHOW INDEXES WHERE name = 'chunk_embedding_index';
-- DROP INDEX chunk_embedding_index IF EXISTS;