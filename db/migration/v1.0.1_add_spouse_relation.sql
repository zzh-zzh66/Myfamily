-- ================================================
-- MyFamily 数据库迁移脚本
-- 版本: v1.0.1
-- 日期: 2026-04-24
-- 说明: 添加配偶外键支持，优化族谱展示
-- ================================================

USE myfamily;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- ================================================
-- 1. 添加 spouse_id 字段到 t_member 表
-- ================================================

ALTER TABLE t_member
ADD COLUMN spouse_id BIGINT COMMENT '配偶ID（外键关联自身）' AFTER mother_id;

-- 添加外键约束（自引用）
ALTER TABLE t_member
ADD CONSTRAINT fk_member_spouse FOREIGN KEY (spouse_id) REFERENCES t_member(id);

-- 添加索引
CREATE INDEX idx_member_spouse ON t_member(spouse_id);

-- ================================================
-- 2. 更新现有配偶关系数据
-- 根据 spouse_name 字段匹配已有成员，建立 spouse_id 关系
-- ================================================

-- 李守仁 <-> 李王氏（李王氏不在成员表中，spouse_id 设为 NULL）
-- UPDATE t_member SET spouse_id = NULL WHERE name = '李守仁' AND spouse_name = '李王氏';

-- 李文博 <-> 王桂兰（双方都在成员表中）
UPDATE t_member SET spouse_id = 12 WHERE id = 2;  -- 李文博的配偶是王桂兰(id=12)
UPDATE t_member SET spouse_id = 2 WHERE id = 12;  -- 王桂兰的配偶是李文博(id=2)

-- 李文秀 <-> 张德顺（张德顺不在成员表中，spouse_id 保持 NULL）

-- 李建华 <-> 张秀英（双方都在成员表中）
UPDATE t_member SET spouse_id = 13 WHERE id = 4;  -- 李建华的配偶是张秀英(id=13)
UPDATE t_member SET spouse_id = 4 WHERE id = 13;  -- 张秀英的配偶是李建华(id=4)

-- 李建军 <-> 陈凤英（双方都在成员表中）
UPDATE t_member SET spouse_id = 14 WHERE id = 5;  -- 李建军的配偶是陈凤英(id=14)
UPDATE t_member SET spouse_id = 5 WHERE id = 14;  -- 陈凤英的配偶是李建军(id=5)

-- 李建芳 <-> 赵国安（赵国安不在成员表中，spouse_id 保持 NULL）

-- 李志强 <-> 刘雅琴（双方都在成员表中）
UPDATE t_member SET spouse_id = 15 WHERE id = 7;  -- 李志强的配偶是刘雅琴(id=15)
UPDATE t_member SET spouse_id = 7 WHERE id = 15;  -- 刘雅琴的配偶是李志强(id=7)

-- 李晓燕 <-> 周伟明（周伟明不在成员表中，spouse_id 保持 NULL）

-- ================================================
-- 3. 验证更新结果
-- ================================================

SELECT id, name, gender, spouse_id,
       (SELECT name FROM t_member m2 WHERE m2.id = m1.spouse_id) AS spouse_name
FROM t_member m1
WHERE spouse_id IS NOT NULL;

-- ================================================
-- 4. 可选：删除 spouse_name 字段（建议保留以保持兼容性）
-- 如果确认不需要 spouse_name，可以取消下面注释执行删除
-- ================================================

-- ALTER TABLE t_member DROP COLUMN spouse_name;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- 迁移完成
-- ================================================
