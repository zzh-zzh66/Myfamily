-- ================================================
-- MyFamily Database Migration Script
-- Version: v1.0.3
-- Date: 2026-04-29
-- Description: Add avatar columns, is_draft, and attachments to mail feature
-- ================================================

USE myfamily;

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE t_user ADD COLUMN avatar VARCHAR(500) DEFAULT NULL COMMENT '用户头像' AFTER role;

ALTER TABLE t_member ADD COLUMN avatar VARCHAR(500) DEFAULT NULL COMMENT '成员头像' AFTER generation;

ALTER TABLE t_mail ADD COLUMN is_draft TINYINT DEFAULT 0 COMMENT '是否草稿：0-否，1-是' AFTER is_deleted;

ALTER TABLE t_mail ADD COLUMN attachments TEXT COMMENT '附件图片URL列表，逗号分隔' AFTER content;

SET FOREIGN_KEY_CHECKS = 1;
