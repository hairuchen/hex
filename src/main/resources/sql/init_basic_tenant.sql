-- 默认租户初始化 SQL
-- 基础租户账号：hex / hex
-- 密码已使用 BCrypt 加密：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVE54.

INSERT INTO tenant (
    id,
    tenant_name,
    username,
    password,
    status,
    create_time,
    update_time,
    remark
) VALUES (
    'fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0',
    '默认租户',
    'hex',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVE54.',
    1,
    '2026-04-10 22:41:54.58084',
    '2026-04-10 22:42:48.051641',
    '系统基础租户，用于快速测试和开发'
);

-- 预创建默认租户分区（所有表）
-- 用户表分区
CREATE TABLE sys_user_tenant_fc7d6ab4_def2_4c64_a5e6_ddeac22f26c0 PARTITION OF sys_user FOR VALUES IN ('fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0');

-- 工作空间表分区
CREATE TABLE workspace_tenant_fc7d6ab4_def2_4c64_a5e6_ddeac22f26c0 PARTITION OF workspace FOR VALUES IN ('fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0');

-- 用户与工作空间关系表分区
CREATE TABLE user2workspace_tenant_fc7d6ab4_def2_4c64_a5e6_ddeac22f26c0 PARTITION OF user2workspace FOR VALUES IN ('fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0');

-- 角色表分区
CREATE TABLE sys_role_tenant_fc7d6ab4_def2_4c64_a5e6_ddeac22f26c0 PARTITION OF sys_role FOR VALUES IN ('fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0');

-- 用户与角色关系表分区
CREATE TABLE user2role_tenant_fc7d6ab4_def2_4c64_a5e6_ddeac22f26c0 PARTITION OF user2role FOR VALUES IN ('fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0');

-- 工作空间与角色关系表分区
CREATE TABLE workspace2role_tenant_fc7d6ab4_def2_4c64_a5e6_ddeac22f26c0 PARTITION OF workspace2role FOR VALUES IN ('fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0');

-- 权限表分区
CREATE TABLE sys_permission_tenant_fc7d6ab4_def2_4c64_a5e6_ddeac22f26c0 PARTITION OF sys_permission FOR VALUES IN ('fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0');

-- 角色与权限关系表分区
CREATE TABLE role2permission_tenant_fc7d6ab4_def2_4c64_a5e6_ddeac22f26c0 PARTITION OF role2permission FOR VALUES IN ('fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0');

-- 用户与权限关系表分区
CREATE TABLE user2permission_tenant_fc7d6ab4_def2_4c64_a5e6_ddeac22f26c0 PARTITION OF user2permission FOR VALUES IN ('fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0');

-- 工作空间与权限关系表分区
CREATE TABLE workspace2permission_tenant_fc7d6ab4_def2_4c64_a5e6_ddeac22f26c0 PARTITION OF workspace2permission FOR VALUES IN ('fc7d6ab4-def2-4c64-a5e6-ddeac22f26c0');
