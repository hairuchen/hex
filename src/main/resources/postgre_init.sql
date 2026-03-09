-- 1. 文件元数据表（补充进度字段 + 适配PostgreSQL注释语法）
CREATE TABLE IF NOT EXISTS file (
    id VARCHAR(50) PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name VARCHAR(255) NOT NULL,
    minio_bucket VARCHAR(100) NOT NULL,
    minio_object_name VARCHAR(100) NOT NULL,
    minio_img_path VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50),
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    upload_user VARCHAR(100),
    status SMALLINT DEFAULT 1,
    process_progress SMALLINT DEFAULT 0,
    fail_reason TEXT,
    is_excavate BOOLEAN DEFAULT FALSE,
    CONSTRAINT uk_minio_object_name UNIQUE (minio_object_name)  -- 唯一约束（PostgreSQL标准写法）
    );

-- 表注释（PostgreSQL单独声明）
COMMENT ON TABLE file IS '文件元数据表（存储文件基础信息+处理状态）';

-- 字段注释（PostgreSQL必须单独声明）
COMMENT ON COLUMN file.id IS '文件唯一标识（主键）';
COMMENT ON COLUMN file.file_name IS '原始文件名（如test.txt）';
COMMENT ON COLUMN file.minio_bucket IS 'MinIO存储桶名称';
COMMENT ON COLUMN file.minio_object_name IS 'MinIO文件对象名称';
COMMENT ON COLUMN file.minio_img_path IS 'MinIO中文件的访问路径';
COMMENT ON COLUMN file.file_size IS '文件大小（字节）';
COMMENT ON COLUMN file.file_type IS '文件类型（如txt、pdf、jpg，MIME类型）';
COMMENT ON COLUMN file.upload_time IS '文件上传时间';
COMMENT ON COLUMN file.upload_user IS '上传人（用户名/用户ID）';
COMMENT ON COLUMN file.status IS '文件处理状态：1-待处理 2-处理中 3-处理成功 4-处理失败 5-已删除';
COMMENT ON COLUMN file.process_progress IS '文件处理进度（0-100，仅处理中状态有效）';
COMMENT ON COLUMN file.fail_reason IS '处理失败原因（仅处理失败状态有效）';
COMMENT ON CONSTRAINT uk_minio_object_name ON file IS 'MinIO对象唯一约束（避免重复存储）';



CREATE INDEX IF NOT EXISTS idx_file_status_progress ON file(status, process_progress);
COMMENT ON INDEX idx_file_status_progress IS '按处理状态+进度筛选文件的索引';