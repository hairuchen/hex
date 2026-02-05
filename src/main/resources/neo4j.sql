知识库中知识片段的数据模型阐述:


-- -- 2. 知识片段表（适配PostgreSQL注释语法）
-- CREATE TABLE IF NOT EXISTS hex_t_document (
--     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--     file_id UUID NOT NULL,
--     chunk_content TEXT NOT NULL,
--     chunk_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
--     chunk_index INT NOT NULL,
--     chunk_token_count INT,
--     prev_chunk_id UUID,
--     next_chunk_id UUID,
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
--     );
--
-- -- 表注释
-- COMMENT ON TABLE hex_t_document IS '文件解析后的知识片段表（按切片存储）';
--
-- -- 字段注释
-- COMMENT ON COLUMN hex_t_document.id IS '知识片段唯一标识（主键）';
-- COMMENT ON COLUMN hex_t_document.file_id IS '关联的文件ID（关联hex_t_file.id）';
-- COMMENT ON COLUMN hex_t_document.chunk_content IS '片段内容：文本切片存原文，图片/PDF存解析后的文本/URL/Base64';
-- COMMENT ON COLUMN hex_t_document.chunk_type IS '切片类型：TEXT(文本)、IMAGE(图片)、PDF(PDF文本)、AUDIO(音频转写文本)等';
-- COMMENT ON COLUMN hex_t_document.chunk_index IS '切片序号（按文件内顺序排序）';
-- COMMENT ON COLUMN hex_t_document.chunk_token_count IS '切片的Token数量（适配大模型处理）';
-- COMMENT ON COLUMN hex_t_document.prev_chunk_id IS '上一个切片ID（链表结构，方便上下文关联）';
-- COMMENT ON COLUMN hex_t_document.next_chunk_id IS '下一个切片ID（链表结构）';
-- COMMENT ON COLUMN hex_t_document.create_time IS '切片创建时间';

-- -- 索引优化（保留原有索引 + 补充注释）
-- CREATE INDEX IF NOT EXISTS idx_document_file_id ON hex_t_document(file_id);
-- COMMENT ON INDEX idx_document_file_id IS '按文件ID查询切片的索引';
--
-- CREATE INDEX IF NOT EXISTS idx_document_chunk_index ON hex_t_document(file_id, chunk_index);
-- COMMENT ON INDEX idx_document_chunk_index IS '按文件ID+切片序号排序查询的索引';
--
-- CREATE INDEX IF NOT EXISTS idx_document_chunk_type ON hex_t_document(chunk_type);
-- COMMENT ON INDEX idx_document_chunk_type IS '按切片类型筛选的索引';
--

-- 1. 删除之前错误绑定到 TestChunk 的旧索引（避免冲突）
DROP INDEX chunk_embedding_index IF EXISTS;

-- 2. 创建正确的向量索引：绑定 KnowledgeChunk 节点的 embedding 属性，1024 维，余弦相似度
CREATE VECTOR INDEX chunk_embedding_index
FOR (c:KnowledgeChunk) ON (c.vector)
OPTIONS {
    indexConfig: {
        `vector.dimensions`: 1024,
        `vector.similarity_function`: 'COSINE'
    }
};