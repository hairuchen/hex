-- 创建数据库（如不存在）
-- 注意：此命令需要在连接到 postgres 数据库后执行
CREATE DATABASE hex;

-- 切换到 hex 数据库
-- psql 命令行工具 navicat 需要单独指定数据库 不能使用
\c hex;


CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.update_time = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;



-- ==========================================
-- 租户表（系统顶层隔离边界）
-- ==========================================
CREATE TABLE IF NOT EXISTS tenant (
    id              VARCHAR(36) PRIMARY KEY,
    tenant_name     VARCHAR(255) NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    password        VARCHAR(100) NOT NULL,
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    remark          VARCHAR(500) DEFAULT ''
);

COMMENT ON TABLE tenant IS '租户表（系统顶层隔离边界，无删除操作，通过状态控制）';
COMMENT ON COLUMN tenant.id IS '租户ID（UUID主键）';
COMMENT ON COLUMN tenant.tenant_name IS '租户名称（企业/组织名称）';
COMMENT ON COLUMN tenant.username IS '租户管理员账号（登录名）';
COMMENT ON COLUMN tenant.password IS '租户管理员密码（加密存储）';
COMMENT ON COLUMN tenant.status IS '状态（1-启用，0-关闭）';
COMMENT ON COLUMN tenant.create_time IS '创建时间';
COMMENT ON COLUMN tenant.update_time IS '更新时间';
COMMENT ON COLUMN tenant.remark IS '备注';


-- ==========================================
-- 用户实体表（归属租户、物理分区、creator_id溯源）
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_user (
    id              VARCHAR(36)  NOT NULL,
    tenant_id       VARCHAR(36)  NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    password        VARCHAR(100) DEFAULT NULL,
    full_name       VARCHAR(255) DEFAULT '',
    email           VARCHAR(100) DEFAULT '',
    phone           VARCHAR(20)  DEFAULT '',
    avatar          VARCHAR(255) DEFAULT '',
    last_login_time TIMESTAMP    DEFAULT NULL,
    last_login_ip   VARCHAR(50)  DEFAULT '',
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    creator_id      VARCHAR(36)  DEFAULT NULL,
    remark          VARCHAR(500) DEFAULT '',
    PRIMARY KEY (tenant_id, id)
) PARTITION BY LIST (tenant_id);

-- 默认分区（存储未匹配租户的数据，或作为初始分区）
CREATE TABLE IF NOT EXISTS sys_user_default PARTITION OF sys_user DEFAULT;

COMMENT ON TABLE sys_user IS '用户实体表（归属租户、物理分区、物理删除、creator_id溯源）';
COMMENT ON COLUMN sys_user.id IS '用户ID（UUID）';
COMMENT ON COLUMN sys_user.tenant_id IS '归属租户ID（分区键）';
COMMENT ON COLUMN sys_user.username IS '用户名（登录账号）';
COMMENT ON COLUMN sys_user.password IS '密码（加密存储，如BCrypt）';
COMMENT ON COLUMN sys_user.full_name IS '用户姓名（真实姓名）';
COMMENT ON COLUMN sys_user.email IS '电子邮箱';
COMMENT ON COLUMN sys_user.phone IS '手机号码';
COMMENT ON COLUMN sys_user.avatar IS '头像URL';
COMMENT ON COLUMN sys_user.last_login_time IS '最后登录时间';
COMMENT ON COLUMN sys_user.last_login_ip IS '最后登录IP地址';
COMMENT ON COLUMN sys_user.status IS '状态（0-禁用，1-正常，2-锁定）';
COMMENT ON COLUMN sys_user.create_time IS '创建时间';
COMMENT ON COLUMN sys_user.update_time IS '更新时间';
COMMENT ON COLUMN sys_user.creator_id IS '创建者ID（记录该用户由谁创建，用于溯源）';
COMMENT ON COLUMN sys_user.remark IS '备注';

-- 用户表索引（在分区表上创建，会自动应用到所有分区）
-- 分区键在前，利于分区裁剪
CREATE UNIQUE INDEX IF NOT EXISTS idx_u_tenant_username_uniq ON sys_user (tenant_id, username);
COMMENT ON INDEX idx_u_tenant_username_uniq IS '租户内用户名唯一索引';
CREATE INDEX IF NOT EXISTS idx_u_creator_id ON sys_user (creator_id);
COMMENT ON INDEX idx_u_creator_id IS '创建者索引（用于溯源查询）';


-- ==========================================
-- 工作空间表（树表父子级、逻辑删除）
-- ==========================================
CREATE TABLE IF NOT EXISTS workspace (
    id              VARCHAR(36)  NOT NULL,
    tenant_id       VARCHAR(36)  NOT NULL,
    workspace_name  VARCHAR(100) NOT NULL,
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT     NOT NULL DEFAULT 0,
    creator_id      VARCHAR(36)  DEFAULT NULL,
    parent_id       VARCHAR(36)  DEFAULT NULL,
    remark          VARCHAR(500) DEFAULT '',
    PRIMARY KEY (tenant_id, id)
    ) PARTITION BY LIST (tenant_id);

-- 默认分区
CREATE TABLE IF NOT EXISTS workspace_default PARTITION OF workspace DEFAULT;

COMMENT ON TABLE workspace IS '工作空间表（归属租户、物理分区、树表父子级、逻辑删除）';
COMMENT ON COLUMN workspace.id IS '工作空间ID（UUID）';
COMMENT ON COLUMN workspace.tenant_id IS '归属租户ID（分区键）';
COMMENT ON COLUMN workspace.workspace_name IS '工作空间名称';
COMMENT ON COLUMN workspace.status IS '状态（0-禁用，1-正常，2-锁定）';
COMMENT ON COLUMN workspace.create_time IS '创建时间';
COMMENT ON COLUMN workspace.update_time IS '更新时间';
COMMENT ON COLUMN workspace.is_deleted IS '逻辑删除标识(0-正常，1-删除)';
COMMENT ON COLUMN workspace.creator_id IS '创建者ID（溯源）';
COMMENT ON COLUMN workspace.parent_id IS '父节点-父级工作空间ID（树表父子级）';
COMMENT ON COLUMN workspace.remark IS '备注';

-- 工作空间表索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_w_tenant_name_uniq ON workspace (tenant_id, workspace_name) WHERE is_deleted = 0;
COMMENT ON INDEX idx_w_tenant_name_uniq IS '租户内工作空间名称唯一索引（未删除）';
CREATE INDEX IF NOT EXISTS idx_w_parent_id ON workspace (parent_id);
COMMENT ON INDEX idx_w_parent_id IS '工作空间树形查询索引';
CREATE INDEX IF NOT EXISTS idx_w_creator_id ON workspace (creator_id);
COMMENT ON INDEX idx_w_creator_id IS '创建者索引（用于溯源）';

-- ==========================================
-- 用户与工作空间的分配关系表
-- ==========================================
CREATE TABLE IF NOT EXISTS user2workspace (
    id            VARCHAR(36) NOT NULL,
    tenant_id     VARCHAR(36) NOT NULL,
    user_id       VARCHAR(36) NOT NULL,
    workspace_id  VARCHAR(36) NOT NULL,
    creator_id    VARCHAR(36) DEFAULT NULL,
    create_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, id)
) PARTITION BY LIST (tenant_id);

-- 默认分区
CREATE TABLE IF NOT EXISTS user2workspace_default PARTITION OF user2workspace DEFAULT;

COMMENT ON TABLE user2workspace IS '用户与工作空间的分配关系表（归属租户、物理分区）';
COMMENT ON COLUMN user2workspace.id IS '关系ID（UUID）';
COMMENT ON COLUMN user2workspace.tenant_id IS '归属租户ID（分区键）';
COMMENT ON COLUMN user2workspace.user_id IS '用户ID（关联sys_user.id）';
COMMENT ON COLUMN user2workspace.workspace_id IS '工作空间ID（关联workspace.id）';
COMMENT ON COLUMN user2workspace.creator_id IS '创建者ID（记录谁建立了这个关系，用于溯源）';
COMMENT ON COLUMN user2workspace.create_time IS '创建时间';

-- 关系表索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_u2w_user_ws_uniq ON user2workspace (tenant_id, user_id, workspace_id);
COMMENT ON INDEX idx_u2w_user_ws_uniq IS '租户内用户-工作空间唯一索引';
CREATE INDEX IF NOT EXISTS idx_u2w_workspace_id ON user2workspace (workspace_id);
COMMENT ON INDEX idx_u2w_workspace_id IS '工作空间查询索引';


-- ==========================================
-- 角色实体表（树表父子级、逻辑删除）
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_role (
    id              VARCHAR(36)  NOT NULL,
    tenant_id       VARCHAR(36)  NOT NULL,
    role_name       VARCHAR(50)  NOT NULL,
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT     NOT NULL DEFAULT 0,
    creator_id      VARCHAR(36)  DEFAULT NULL,
    parent_id       VARCHAR(36)  DEFAULT NULL,
    remark          VARCHAR(500) DEFAULT '',
    PRIMARY KEY (tenant_id, id)
) PARTITION BY LIST (tenant_id);

-- 默认分区
CREATE TABLE IF NOT EXISTS sys_role_default PARTITION OF sys_role DEFAULT;

COMMENT ON TABLE sys_role IS '角色实体表（归属租户、物理分区、树表父子级、逻辑删除）';
COMMENT ON COLUMN sys_role.id IS '角色ID（UUID）';
COMMENT ON COLUMN sys_role.tenant_id IS '归属租户ID（分区键）';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.status IS '状态（0-禁用，1-正常，2-锁定）';
COMMENT ON COLUMN sys_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_role.update_time IS '更新时间';
COMMENT ON COLUMN sys_role.is_deleted IS '逻辑删除标识(0-正常，1-删除)';
COMMENT ON COLUMN sys_role.creator_id IS '创建者ID（溯源）';
COMMENT ON COLUMN sys_role.parent_id IS '父节点-父级角色ID（树表父子级）';
COMMENT ON COLUMN sys_role.remark IS '备注';

-- 角色表索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_r_tenant_name_uniq ON sys_role (tenant_id, role_name) WHERE is_deleted = 0;
COMMENT ON INDEX idx_r_tenant_name_uniq IS '租户内角色名称唯一索引（未删除）';
CREATE INDEX IF NOT EXISTS idx_r_parent_id ON sys_role (parent_id);
COMMENT ON INDEX idx_r_parent_id IS '角色树形查询索引';
CREATE INDEX IF NOT EXISTS idx_r_creator_id ON sys_role (creator_id);
COMMENT ON INDEX idx_r_creator_id IS '创建者索引（用于溯源）';

-- ==========================================
-- 用户与角色的分配关系表
-- ==========================================
CREATE TABLE IF NOT EXISTS user2role (
    id            VARCHAR(36) NOT NULL,
    tenant_id     VARCHAR(36) NOT NULL,
    user_id       VARCHAR(36) NOT NULL,
    role_id       VARCHAR(36) NOT NULL,
    creator_id    VARCHAR(36) DEFAULT NULL,
    create_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, id)
) PARTITION BY LIST (tenant_id);

-- 默认分区
CREATE TABLE IF NOT EXISTS user2role_default PARTITION OF user2role DEFAULT;

COMMENT ON TABLE user2role IS '用户与角色的分配关系表（归属租户、物理分区）';
COMMENT ON COLUMN user2role.id IS '关系ID（UUID）';
COMMENT ON COLUMN user2role.tenant_id IS '归属租户ID（分区键）';
COMMENT ON COLUMN user2role.user_id IS '用户ID（关联sys_user.id）';
COMMENT ON COLUMN user2role.role_id IS '角色ID（关联sys_role.id）';
COMMENT ON COLUMN user2role.creator_id IS '创建者ID（记录谁建立了这个关系，用于溯源）';
COMMENT ON COLUMN user2role.create_time IS '创建时间';

-- 关系表索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_u2r_user_role_uniq ON user2role (tenant_id, user_id, role_id);
COMMENT ON INDEX idx_u2r_user_role_uniq IS '租户内用户-角色唯一索引';
CREATE INDEX IF NOT EXISTS idx_u2r_role_id ON user2role (role_id);
COMMENT ON INDEX idx_u2r_role_id IS '角色查询索引';

-- ==========================================
-- 工作空间与角色的分配关系表
-- ==========================================
CREATE TABLE IF NOT EXISTS workspace2role (
    id            VARCHAR(36) NOT NULL,
    tenant_id     VARCHAR(36) NOT NULL,
    workspace_id  VARCHAR(36) NOT NULL,
    role_id       VARCHAR(36) NOT NULL,
    creator_id    VARCHAR(36) DEFAULT NULL,
    create_time   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, id)
) PARTITION BY LIST (tenant_id);

-- 默认分区
CREATE TABLE IF NOT EXISTS workspace2role_default PARTITION OF workspace2role DEFAULT;

COMMENT ON TABLE workspace2role IS '工作空间与角色的分配关系表（归属租户、物理分区）';
COMMENT ON COLUMN workspace2role.id IS '关系ID（UUID）';
COMMENT ON COLUMN workspace2role.tenant_id IS '归属租户ID（分区键）';
COMMENT ON COLUMN workspace2role.workspace_id IS '工作空间ID（关联workspace.id）';
COMMENT ON COLUMN workspace2role.role_id IS '角色ID（关联sys_role.id）';
COMMENT ON COLUMN workspace2role.creator_id IS '创建者ID（记录谁建立了这个关系，用于溯源）';
COMMENT ON COLUMN workspace2role.create_time IS '创建时间';

-- 关系表索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_w2r_ws_role_uniq ON workspace2role (tenant_id, workspace_id, role_id);
COMMENT ON INDEX idx_w2r_ws_role_uniq IS '租户内工作空间-角色唯一索引';

-- ==========================================
-- 权限实体表（树表父子级、逻辑删除）
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_permission (
    id              VARCHAR(36)  NOT NULL,
    tenant_id       VARCHAR(36)  NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    controller_name VARCHAR(100) NULL,
    function_name   VARCHAR(100) NULL,
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT     NOT NULL DEFAULT 0,
    creator_id      VARCHAR(36)  DEFAULT NULL,
    parent_id       VARCHAR(36)  DEFAULT NULL,
    remark          VARCHAR(500) DEFAULT '',
    PRIMARY KEY (tenant_id, id)
) PARTITION BY LIST (tenant_id);

-- 默认分区
CREATE TABLE IF NOT EXISTS sys_permission_default PARTITION OF sys_permission DEFAULT;

COMMENT ON TABLE sys_permission IS '权限实体表（归属租户、物理分区、树表父子级、逻辑删除）';
COMMENT ON COLUMN sys_permission.id IS '权限ID（UUID）';
COMMENT ON COLUMN sys_permission.tenant_id IS '归属租户ID（分区键）';
COMMENT ON COLUMN sys_permission.permission_name IS '权限名称';
COMMENT ON COLUMN sys_permission.controller_name IS '控制器名称';
COMMENT ON COLUMN sys_permission.function_name IS '方法名称';
COMMENT ON COLUMN sys_permission.status IS '状态（0-禁用，1-正常，2-锁定）';
COMMENT ON COLUMN sys_permission.create_time IS '创建时间';
COMMENT ON COLUMN sys_permission.update_time IS '更新时间';
COMMENT ON COLUMN sys_permission.is_deleted IS '逻辑删除标识(0-正常，1-删除)';
COMMENT ON COLUMN sys_permission.creator_id IS '创建者ID（溯源）';
COMMENT ON COLUMN sys_permission.parent_id IS '父节点-父级权限ID（树表父子级）';
COMMENT ON COLUMN sys_permission.remark IS '备注';

-- 权限表索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_p_tenant_name_uniq ON sys_permission (tenant_id, permission_name) WHERE is_deleted = 0;
COMMENT ON INDEX idx_p_tenant_name_uniq IS '租户内权限名称唯一索引（未删除）';
CREATE INDEX IF NOT EXISTS idx_p_controller_func ON sys_permission (tenant_id, controller_name, function_name);
COMMENT ON INDEX idx_p_controller_func IS '控制器+方法查询索引（用于鉴权）';
CREATE INDEX IF NOT EXISTS idx_p_parent_id ON sys_permission (parent_id);
COMMENT ON INDEX idx_p_parent_id IS '权限树形查询索引';
CREATE INDEX IF NOT EXISTS idx_p_creator_id ON sys_permission (creator_id);
COMMENT ON INDEX idx_p_creator_id IS '创建者索引（用于溯源）';

-- ==========================================
-- 角色与权限的授权关系表
-- ==========================================
CREATE TABLE IF NOT EXISTS role2permission (
    id              VARCHAR(36) NOT NULL,
    tenant_id       VARCHAR(36) NOT NULL,
    role_id         VARCHAR(36) NOT NULL,
    permission_id   VARCHAR(36) NOT NULL,
    creator_id      VARCHAR(36) DEFAULT NULL,
    create_time     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, id)
) PARTITION BY LIST (tenant_id);

-- 默认分区
CREATE TABLE IF NOT EXISTS role2permission_default PARTITION OF role2permission DEFAULT;

COMMENT ON TABLE role2permission IS '角色与权限的授权关系表（归属租户、物理分区）';
COMMENT ON COLUMN role2permission.id IS '关系ID（UUID）';
COMMENT ON COLUMN role2permission.tenant_id IS '归属租户ID（分区键）';
COMMENT ON COLUMN role2permission.role_id IS '角色ID（关联sys_role.id）';
COMMENT ON COLUMN role2permission.permission_id IS '权限ID（关联sys_permission.id）';
COMMENT ON COLUMN role2permission.creator_id IS '创建者ID（记录谁建立了这个关系，用于溯源）';
COMMENT ON COLUMN role2permission.create_time IS '创建时间';

-- 关系表索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_r2p_role_perm_uniq ON role2permission (tenant_id, role_id, permission_id);
COMMENT ON INDEX idx_r2p_role_perm_uniq IS '租户内角色-权限唯一索引';
CREATE INDEX IF NOT EXISTS idx_r2p_permission_id ON role2permission (permission_id);
COMMENT ON INDEX idx_r2p_permission_id IS '权限查询索引';

CREATE INDEX IF NOT EXISTS idx_w2r_role_id ON workspace2role (role_id);
COMMENT ON INDEX idx_w2r_role_id IS '角色查询索引';

-- ==========================================
-- 用户与权限的授权关系表
-- ==========================================
CREATE TABLE IF NOT EXISTS user2permission (
    id              VARCHAR(36) NOT NULL,
    tenant_id       VARCHAR(36) NOT NULL,
    user_id         VARCHAR(36) NOT NULL,
    permission_id   VARCHAR(36) NOT NULL,
    creator_id      VARCHAR(36) DEFAULT NULL,
    create_time     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, id)
) PARTITION BY LIST (tenant_id);

-- 默认分区
CREATE TABLE IF NOT EXISTS user2permission_default PARTITION OF user2permission DEFAULT;

COMMENT ON TABLE user2permission IS '用户与权限的授权关系表（归属租户、物理分区）';
COMMENT ON COLUMN user2permission.id IS '关系ID（UUID）';
COMMENT ON COLUMN user2permission.tenant_id IS '归属租户ID（分区键）';
COMMENT ON COLUMN user2permission.user_id IS '用户ID（关联sys_user.id）';
COMMENT ON COLUMN user2permission.permission_id IS '权限ID（关联sys_permission.id）';
COMMENT ON COLUMN user2permission.creator_id IS '创建者ID（记录谁建立了这个关系，用于溯源）';
COMMENT ON COLUMN user2permission.create_time IS '创建时间';

-- 关系表索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_u2p_user_perm_uniq ON user2permission (tenant_id, user_id, permission_id);
COMMENT ON INDEX idx_u2p_user_perm_uniq IS '租户内用户-权限唯一索引';
CREATE INDEX IF NOT EXISTS idx_u2p_permission_id ON user2permission (permission_id);
COMMENT ON INDEX idx_u2p_permission_id IS '权限查询索引';

-- ==========================================
-- 工作空间与权限的授权关系表
-- ==========================================
CREATE TABLE IF NOT EXISTS workspace2permission (
    id              VARCHAR(36) NOT NULL,
    tenant_id       VARCHAR(36) NOT NULL,
    workspace_id    VARCHAR(36) NOT NULL,
    permission_id   VARCHAR(36) NOT NULL,
    creator_id      VARCHAR(36) DEFAULT NULL,
    create_time     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, id)
) PARTITION BY LIST (tenant_id);

-- 默认分区
CREATE TABLE IF NOT EXISTS workspace2permission_default PARTITION OF workspace2permission DEFAULT;

COMMENT ON TABLE workspace2permission IS '工作空间与权限的授权关系表（归属租户、物理分区）';
COMMENT ON COLUMN workspace2permission.id IS '关系ID（UUID）';
COMMENT ON COLUMN workspace2permission.tenant_id IS '归属租户ID（分区键）';
COMMENT ON COLUMN workspace2permission.workspace_id IS '工作空间ID（关联workspace.id）';
COMMENT ON COLUMN workspace2permission.permission_id IS '权限ID（关联sys_permission.id）';
COMMENT ON COLUMN workspace2permission.creator_id IS '创建者ID（记录谁建立了这个关系，用于溯源）';
COMMENT ON COLUMN workspace2permission.create_time IS '创建时间';

-- 关系表索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_w2p_ws_perm_uniq ON workspace2permission (tenant_id, workspace_id, permission_id);
COMMENT ON INDEX idx_w2p_ws_perm_uniq IS '租户内工作空间-权限唯一索引';
CREATE INDEX IF NOT EXISTS idx_w2p_permission_id ON workspace2permission (permission_id);
COMMENT ON INDEX idx_w2p_permission_id IS '权限查询索引';



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


-- 补充PostgreSQL的update_time自动更新触发器（替代MySQL的ON UPDATE）
CREATE TRIGGER trg_tenant_update
    BEFORE UPDATE ON tenant
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- 新表update_time触发器
CREATE TRIGGER trg_sys_user_update
    BEFORE UPDATE ON sys_user
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_workspace_update
    BEFORE UPDATE ON workspace
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_sys_role_update
    BEFORE UPDATE ON sys_role
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_sys_permission_update
    BEFORE UPDATE ON sys_permission
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();
