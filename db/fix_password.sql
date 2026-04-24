-- MyFamily 数据库密码修复脚本
-- 执行方法：复制以下SQL到MySQL客户端直接执行

-- 查看当前密码状态（可选）
-- SELECT id, username, SUBSTRING(password, 1, 20) as pwd_prefix FROM t_user;

-- 更新所有用户的密码为 "password123"
-- BCrypt哈希对应的明文密码是: password123
UPDATE t_user SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE username = 'admin';
UPDATE t_user SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE username = 'li_zhiqiang';
UPDATE t_user SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE username = 'li_xiaoyan';
UPDATE t_user SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE username = 'li_jianjun';
UPDATE t_user SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE username = 'li_jianfang';
