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
        
        
-- ==========================================
-- 基础实体表
-- ==========================================
-- 用户实体表
CREATE TABLE IF NOT EXISTS "user" (
    id              VARCHAR(36)  NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    password        VARCHAR(100) DEFAULT NULL,
    full_name       VARCHAR(50)  DEFAULT '',
    email           VARCHAR(100) DEFAULT '',
    phone           VARCHAR(20)  DEFAULT '',
    avatar          VARCHAR(255) DEFAULT '',
    last_login_time TIMESTAMP    DEFAULT NULL,
    last_login_ip   VARCHAR(50)  DEFAULT '',
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT     NOT NULL DEFAULT 0,
    version         VARCHAR(50)  NOT NULL DEFAULT '0',
    parent_id       VARCHAR(36)  DEFAULT NULL,
    remark          VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id)
    );

-- 表注释
COMMENT ON TABLE "user" IS '用户实体表';
-- 字段注释
COMMENT ON COLUMN "user".id IS '用户ID（UUID）';
COMMENT ON COLUMN "user".username IS '用户名（登录账号）';
COMMENT ON COLUMN "user".password IS '密码（加密存储，如BCrypt）';
COMMENT ON COLUMN "user".full_name IS '用户姓名（真实姓名）';
COMMENT ON COLUMN "user".email IS '电子邮箱';
COMMENT ON COLUMN "user".phone IS '手机号码';
COMMENT ON COLUMN "user".avatar IS '头像URL';
COMMENT ON COLUMN "user".last_login_time IS '最后登录时间';
COMMENT ON COLUMN "user".last_login_ip IS '最后登录IP地址';
COMMENT ON COLUMN "user".status IS '状态（0-禁用，1-正常，2-锁定）';
COMMENT ON COLUMN "user".create_time IS '创建时间';
COMMENT ON COLUMN "user".update_time IS '更新时间';
COMMENT ON COLUMN "user".is_deleted IS '逻辑删除标识(0-正常，1-删除)';
COMMENT ON COLUMN "user".version IS '版本号';
COMMENT ON COLUMN "user".parent_id IS '父节点-创建人ID';
COMMENT ON COLUMN "user".remark IS '备注';

-- 部门实体表
CREATE TABLE IF NOT EXISTS department (
    id              VARCHAR(36)  NOT NULL,
    dept_name       VARCHAR(50)  NULL,
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT     NOT NULL DEFAULT 0,
    version         VARCHAR(50)  NOT NULL DEFAULT '0',
    parent_id       VARCHAR(36)  DEFAULT NULL,
    remark          VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id)
    );

COMMENT ON TABLE department IS '部门实体表';
COMMENT ON COLUMN department.id IS '部门ID（UUID）';
COMMENT ON COLUMN department.dept_name IS '部门名称';
COMMENT ON COLUMN department.status IS '状态（0-禁用，1-正常，2-锁定）';
COMMENT ON COLUMN department.create_time IS '创建时间';
COMMENT ON COLUMN department.update_time IS '更新时间';
COMMENT ON COLUMN department.is_deleted IS '逻辑删除标识(0-正常，1-删除)';
COMMENT ON COLUMN department.version IS '版本号';
COMMENT ON COLUMN department.parent_id IS '父节点-上级部门ID';
COMMENT ON COLUMN department.remark IS '备注';

-- 角色实体表
CREATE TABLE IF NOT EXISTS "role" (
    id              VARCHAR(36)  NOT NULL,
    role_name       VARCHAR(50)  NULL,
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT     NOT NULL DEFAULT 0,
    version         VARCHAR(50)  NOT NULL DEFAULT '0',
    parent_id       VARCHAR(36)  DEFAULT NULL,
    remark          VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id)
    );

COMMENT ON TABLE "role" IS '角色实体表';
COMMENT ON COLUMN "role".id IS '角色ID（UUID）';
COMMENT ON COLUMN "role".role_name IS '角色名称';
COMMENT ON COLUMN "role".status IS '状态（0-禁用，1-正常，2-锁定）';
COMMENT ON COLUMN "role".create_time IS '创建时间';
COMMENT ON COLUMN "role".update_time IS '更新时间';
COMMENT ON COLUMN "role".is_deleted IS '逻辑删除标识(0-正常，1-删除)';
COMMENT ON COLUMN "role".version IS '版本号';
COMMENT ON COLUMN "role".parent_id IS '父节点-父级角色ID';
COMMENT ON COLUMN "role".remark IS '备注';

-- 菜单/权限实体表
CREATE TABLE IF NOT EXISTS "menu" (
    id              VARCHAR(36)  NOT NULL,
    menu_name       VARCHAR(50)  NULL,
    controller_name VARCHAR(100) NULL,
    function_name   VARCHAR(100) NULL,
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT     NOT NULL DEFAULT 0,
    version         VARCHAR(50)  NOT NULL DEFAULT '0',
    parent_id       VARCHAR(36)  DEFAULT NULL,
    remark          VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id)
    );

COMMENT ON TABLE "menu" IS '菜单/权限实体表';
COMMENT ON COLUMN "menu".id IS '菜单ID（UUID）';
COMMENT ON COLUMN "menu".menu_name IS '菜单名称';
COMMENT ON COLUMN "menu".controller_name IS '控制器名称';
COMMENT ON COLUMN "menu".function_name IS '方法名称';
COMMENT ON COLUMN "menu".status IS '状态（0-禁用，1-正常，2-锁定）';
COMMENT ON COLUMN "menu".create_time IS '创建时间';
COMMENT ON COLUMN "menu".update_time IS '更新时间';
COMMENT ON COLUMN "menu".is_deleted IS '逻辑删除标识(0-正常，1-删除)';
COMMENT ON COLUMN "menu".version IS '版本号';
COMMENT ON COLUMN "menu".parent_id IS '父节点-父级菜单ID';
COMMENT ON COLUMN "menu".remark IS '备注';

-- ==========================================
-- 关系表
-- ==========================================
-- 用户与部门的归属关系表
CREATE TABLE IF NOT EXISTS user2dept (
    id            VARCHAR(36) NOT NULL,
    user_id       VARCHAR(36) NOT NULL,
    dept_id       VARCHAR(36) NOT NULL,
    create_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

COMMENT ON TABLE user2dept IS '用户与部门的归属关系表';
COMMENT ON COLUMN user2dept.id IS '关系ID（UUID）';
COMMENT ON COLUMN user2dept.user_id IS '用户ID（关联user.id）';
COMMENT ON COLUMN user2dept.dept_id IS '部门ID（关联department.id）';
COMMENT ON COLUMN user2dept.create_time IS '创建时间';

-- 用户与权限的授权关系表
CREATE TABLE IF NOT EXISTS user2menu (
    id            VARCHAR(36) NOT NULL,
    user_id       VARCHAR(36) NOT NULL,
    menu_id       VARCHAR(36) NOT NULL,
    create_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

COMMENT ON TABLE user2menu IS '用户与权限的授权关系表';
COMMENT ON COLUMN user2menu.id IS '关系ID（UUID）';
COMMENT ON COLUMN user2menu.user_id IS '用户ID（关联user.id）';
COMMENT ON COLUMN user2menu.menu_id IS '权限/菜单ID（关联menu.id）';
COMMENT ON COLUMN user2menu.create_time IS '创建时间';

-- 用户与角色的分配关系表
CREATE TABLE IF NOT EXISTS user2role (
    id            VARCHAR(36) NOT NULL,
    user_id       VARCHAR(36) NOT NULL,
    role_id       VARCHAR(36) NOT NULL,
    create_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

COMMENT ON TABLE user2role IS '用户与角色的分配关系表';
COMMENT ON COLUMN user2role.id IS '关系ID（UUID）';
COMMENT ON COLUMN user2role.user_id IS '用户ID（关联user.id）';
COMMENT ON COLUMN user2role.role_id IS '角色ID（关联role.id）';
COMMENT ON COLUMN user2role.create_time IS '创建时间';

-- 部门与角色的分配关系表
CREATE TABLE IF NOT EXISTS dept2role (
    id            VARCHAR(36) NOT NULL,
    dept_id       VARCHAR(36) NOT NULL,
    role_id       VARCHAR(36) NOT NULL,
    create_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

COMMENT ON TABLE dept2role IS '部门与角色的分配关系表';
COMMENT ON COLUMN dept2role.id IS '关系ID（UUID）';
COMMENT ON COLUMN dept2role.dept_id IS '部门ID（关联department.id）';
COMMENT ON COLUMN dept2role.role_id IS '角色ID（关联role.id）';
COMMENT ON COLUMN dept2role.create_time IS '创建时间';

-- 角色与权限的授权关系表
CREATE TABLE IF NOT EXISTS role2menu (
    id            VARCHAR(36) NOT NULL,
    role_id       VARCHAR(36) NOT NULL,
    menu_id       VARCHAR(36) NOT NULL,
    create_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

COMMENT ON TABLE role2menu IS '角色与权限的授权关系表';
COMMENT ON COLUMN role2menu.id IS '关系ID（UUID）';
COMMENT ON COLUMN role2menu.role_id IS '角色ID（关联role.id）';
COMMENT ON COLUMN role2menu.menu_id IS '权限/菜单ID（关联menu.id）';
COMMENT ON COLUMN role2menu.create_time IS '创建时间';

-- 部门与权限的授权关系表
CREATE TABLE IF NOT EXISTS dept2menu (
    id            VARCHAR(36) NOT NULL,
    dept_id       VARCHAR(36) NOT NULL,
    menu_id       VARCHAR(36) NOT NULL,
    create_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

COMMENT ON TABLE dept2menu IS '部门与权限的授权关系表';
COMMENT ON COLUMN dept2menu.id IS '关系ID（UUID）';
COMMENT ON COLUMN dept2menu.dept_id IS '部门ID（关联department.id）';
COMMENT ON COLUMN dept2menu.menu_id IS '权限/菜单ID（关联menu.id）';
COMMENT ON COLUMN dept2menu.create_time IS '创建时间';

-- ==========================================
-- 索引（适配PostgreSQL语法）
-- ==========================================
/* 用户表索引 */
CREATE UNIQUE INDEX IF NOT EXISTS idx_u_username_uniq ON "user" (username);
CREATE INDEX IF NOT EXISTS idx_u_email ON "user" (email);
CREATE INDEX IF NOT EXISTS idx_u_phone ON "user" (phone);
CREATE INDEX IF NOT EXISTS idx_u_parent_id ON "user" (parent_id);
CREATE INDEX IF NOT EXISTS idx_u_create_tm ON "user" (create_time);

/* 部门表索引 */
CREATE INDEX IF NOT EXISTS idx_d_parent_id ON department (parent_id);
CREATE INDEX IF NOT EXISTS idx_d_create_tm ON department (create_time);

/* 角色表索引 */
CREATE INDEX IF NOT EXISTS idx_r_parent_id ON "role" (parent_id);
CREATE INDEX IF NOT EXISTS idx_r_create_tm ON "role" (create_time);

/* 菜单表索引 */
CREATE INDEX IF NOT EXISTS idx_m_parent_id ON "menu" (parent_id);
CREATE INDEX IF NOT EXISTS idx_m_create_tm ON "menu" (create_time);

/* 关系表索引 */
-- 用户-部门
CREATE INDEX IF NOT EXISTS idx_ur2d_user_id ON user2dept (user_id);
CREATE INDEX IF NOT EXISTS idx_ur2d_dept_id ON user2dept (dept_id);
-- 用户-权限
CREATE INDEX IF NOT EXISTS idx_ur2m_user_id ON user2menu (user_id);
CREATE INDEX IF NOT EXISTS idx_ur2m_menu_id ON user2menu (menu_id);
-- 用户-角色
CREATE INDEX IF NOT EXISTS idx_ur2r_user_id ON user2role (user_id);
CREATE INDEX IF NOT EXISTS idx_ur2r_role_id ON user2role (role_id);
-- 部门-角色
CREATE INDEX IF NOT EXISTS idx_dr2r_dept_id ON dept2role (dept_id);
CREATE INDEX IF NOT EXISTS idx_dr2r_role_id ON dept2role (role_id);
-- 角色-权限
CREATE INDEX IF NOT EXISTS idx_rl2m_role_id ON role2menu (role_id);
CREATE INDEX IF NOT EXISTS idx_rl2m_menu_id ON role2menu (menu_id);
-- 部门-权限
CREATE INDEX IF NOT EXISTS idx_dr2m_dept_id ON dept2menu (dept_id);
CREATE INDEX IF NOT EXISTS idx_dr2m_menu_id ON dept2menu (menu_id);

-- 索引注释
COMMENT ON INDEX idx_u_username_uniq IS '登录唯一索引';
COMMENT ON INDEX idx_u_email IS '后台搜索邮箱索引';
COMMENT ON INDEX idx_u_phone IS '后台搜索手机号索引';
COMMENT ON INDEX idx_u_parent_id IS '用户表树形查询索引';
COMMENT ON INDEX idx_u_create_tm IS '用户表最新注册索引';
COMMENT ON INDEX idx_d_parent_id IS '部门表树形查询索引';
COMMENT ON INDEX idx_d_create_tm IS '部门表最新创建索引';
COMMENT ON INDEX idx_r_parent_id IS '角色表树形查询索引';
COMMENT ON INDEX idx_r_create_tm IS '角色表最新创建索引';
COMMENT ON INDEX idx_m_parent_id IS '菜单表树形查询索引';
COMMENT ON INDEX idx_m_create_tm IS '菜单表最新创建索引';

-- 补充PostgreSQL的update_time自动更新触发器（替代MySQL的ON UPDATE）
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.update_time = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为所有带update_time的表添加触发器
CREATE TRIGGER trg_user_update
    BEFORE UPDATE ON "user"
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_department_update
    BEFORE UPDATE ON department
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_role_update
    BEFORE UPDATE ON "role"
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_menu_update
    BEFORE UPDATE ON "menu"
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();