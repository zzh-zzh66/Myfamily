-- ================================================
-- MyFamily Database Migration Script
-- Version: v1.0.4
-- Date: 2026-04-30
-- Description: Add memorial_agent table for memorial AI agent feature
-- ================================================

USE myfamily;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------
-- Table: t_memorial_agent
--智能体配置表
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS t_memorial_agent (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '智能体ID',
    memorial_id         BIGINT NOT NULL COMMENT '纪念人物ID',
    status              VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING待上传/ACTIVE已激活/FAILED未通过',
    document_content    TEXT COMMENT '用户上传的原始文档',
    persona_prompt     TEXT COMMENT '生成的人设Prompt',
    evaluation_score    INT COMMENT '评估得分',
    evaluation_detail   JSON COMMENT '评估详情JSON',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_memorial (memorial_id),
    INDEX idx_agent_memorial (memorial_id),

    CONSTRAINT fk_agent_memorial FOREIGN KEY (memorial_id)
        REFERENCES t_memorial(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体配置表';

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------
-- 使用说明
-- ----------------------------------------------------
-- 该表用于存储纪念人物的智能体配置
-- 关系：t_memorial (1) <-> (0..1) t_memorial_agent
-- ----------------------------------------------------
