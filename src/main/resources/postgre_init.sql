-- 1. 文件元数据表（补充进度字段 + 适配PostgreSQL注释语法）
CREATE TABLE IF NOT EXISTS hex_t_file (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name VARCHAR(255) NOT NULL,
    minio_bucket VARCHAR(100) NOT NULL,
    minio_object_path VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50),
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    upload_user VARCHAR(100),
    status SMALLINT DEFAULT 1,
    process_progress SMALLINT DEFAULT 0,  -- 新增：处理进度（0-100）
    fail_reason TEXT,                     -- 新增：失败原因
--     CONSTRAINT uk_minio_object_path UNIQUE (minio_object_path)  -- 唯一约束（PostgreSQL标准写法）
    );

-- 表注释（PostgreSQL单独声明）
COMMENT ON TABLE hex_t_file IS '文件元数据表（存储文件基础信息+处理状态）';

-- 字段注释（PostgreSQL必须单独声明）
COMMENT ON COLUMN hex_t_file.id IS '文件唯一标识（主键）';
COMMENT ON COLUMN hex_t_file.file_name IS '原始文件名（如test.txt）';
COMMENT ON COLUMN hex_t_file.minio_bucket IS 'MinIO存储桶名称';
COMMENT ON COLUMN hex_t_file.minio_object_path IS 'MinIO中文件的完整对象路径（如file/xxx-xxx-xxx.txt）';
COMMENT ON COLUMN hex_t_file.file_size IS '文件大小（字节）';
COMMENT ON COLUMN hex_t_file.file_type IS '文件类型（如txt、pdf、jpg，MIME类型）';
COMMENT ON COLUMN hex_t_file.upload_time IS '文件上传时间';
COMMENT ON COLUMN hex_t_file.upload_user IS '上传人（用户名/用户ID）';
COMMENT ON COLUMN hex_t_file.status IS '文件处理状态：1-待处理 2-处理中 3-处理成功 4-处理失败 5-已删除';
COMMENT ON COLUMN hex_t_file.process_progress IS '文件处理进度（0-100，仅处理中状态有效）';
COMMENT ON COLUMN hex_t_file.fail_reason IS '处理失败原因（仅处理失败状态有效）';
COMMENT ON CONSTRAINT uk_minio_object_path ON hex_t_file IS 'MinIO对象路径唯一约束（避免重复存储）';



CREATE INDEX IF NOT EXISTS idx_file_status_progress ON hex_t_file(status, process_progress);
COMMENT ON INDEX idx_file_status_progress IS '按处理状态+进度筛选文件的索引';