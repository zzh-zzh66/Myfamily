-- 数据库迁移脚本 v1.0.1
-- 添加 spouse_id 和 is_virtual 字段到 t_member 表

-- 添加配偶ID字段
ALTER TABLE t_member ADD COLUMN spouse_id BIGINT COMMENT '配偶ID' AFTER spouse_name;

-- 添加索引
ALTER TABLE t_member ADD INDEX idx_member_spouse (spouse_id);

-- 添加外键约束
ALTER TABLE t_member ADD CONSTRAINT fk_member_spouse FOREIGN KEY (spouse_id) REFERENCES t_member(id);

-- 添加虚拟成员标记字段
ALTER TABLE t_member ADD COLUMN is_virtual TINYINT DEFAULT 0 COMMENT '是否为虚拟成员（无名氏）';
