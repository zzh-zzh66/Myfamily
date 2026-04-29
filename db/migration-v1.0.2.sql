-- ================================================
-- MyFamily Database Migration Script
-- Version: v1.0.2
-- Date: 2026-04-29
-- Description: Add post like table, fix title column, and add images column
-- ================================================

USE myfamily;

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE t_post MODIFY title VARCHAR(200) DEFAULT NULL COMMENT '标题';
ALTER TABLE t_post ADD COLUMN images TEXT COMMENT '图片URL列表，逗号分隔' AFTER content;

DROP TABLE IF EXISTS t_post_like;
CREATE TABLE t_post_like (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id             BIGINT NOT NULL,
    user_id             BIGINT NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted             TINYINT DEFAULT 0,
    UNIQUE KEY uk_post_user (post_id, user_id),
    INDEX idx_like_post (post_id),
    INDEX idx_like_user (user_id),
    CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES t_post(id),
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Post Like Table';

SET FOREIGN_KEY_CHECKS = 1;