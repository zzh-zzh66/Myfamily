-- ================================================
-- MyFamily 数据库建表脚本
-- 版本: v1.0.0
-- 日期: 2026-04-24
-- 说明: 家族族谱管理系统数据库初始化脚本
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS myfamily
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE myfamily;

-- 禁用外键检查（执行建表脚本时）
SET FOREIGN_KEY_CHECKS = 0;

-- ================================================
-- 1. 家族表 (t_family)
-- ================================================
DROP TABLE IF EXISTS t_family;
CREATE TABLE t_family (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '家族ID',
    name                VARCHAR(100) NOT NULL COMMENT '家族名称',
    description         TEXT COMMENT '家族简介',
    logo_url            VARCHAR(500) COMMENT '家族Logo',
    admin_id            BIGINT COMMENT '创始人/管理员ID',
    status              TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '软删除标记',

    INDEX idx_family_status (status),
    INDEX idx_family_admin (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家族表';

-- ================================================
-- 2. 成员表 (t_member)
-- ================================================
DROP TABLE IF EXISTS t_member;
CREATE TABLE t_member (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员ID',
    family_id           BIGINT NOT NULL COMMENT '所属家族ID',
    name                VARCHAR(50) NOT NULL COMMENT '成员姓名',
    gender              ENUM('MALE', 'FEMALE') NOT NULL COMMENT '性别',
    birth_date          DATE COMMENT '出生日期',
    death_date          DATE COMMENT '去世日期',
    generation          INT NOT NULL COMMENT '辈分（第几代）',
    father_id           BIGINT COMMENT '父亲ID',
    mother_id           BIGINT COMMENT '母亲ID',
    spouse_name         VARCHAR(50) COMMENT '配偶姓名',
    status              TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '软删除标记',

    INDEX idx_member_family (family_id),
    INDEX idx_member_father (father_id),
    INDEX idx_member_mother (mother_id),
    INDEX idx_member_generation (generation),
    INDEX idx_member_name (name),

    CONSTRAINT fk_member_family FOREIGN KEY (family_id) REFERENCES t_family(id),
    CONSTRAINT fk_member_father FOREIGN KEY (father_id) REFERENCES t_member(id),
    CONSTRAINT fk_member_mother FOREIGN KEY (mother_id) REFERENCES t_member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成员表';

-- ================================================
-- 3. 成员详情表 (t_member_profile)
-- ================================================
DROP TABLE IF EXISTS t_member_profile;
CREATE TABLE t_member_profile (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '详情ID',
    member_id           BIGINT NOT NULL COMMENT '成员ID',
    avatar_url          VARCHAR(500) COMMENT '头像URL',
    native_place        VARCHAR(200) COMMENT '籍贯',
    birth_place         VARCHAR(200) COMMENT '出生地',
    education           VARCHAR(100) COMMENT '学历',
    occupation          VARCHAR(100) COMMENT '职业',
    achievement         TEXT COMMENT '成就/事迹',
    biography           TEXT COMMENT '个人传记',
    contact_email       VARCHAR(100) COMMENT '联系邮箱',
    contact_phone       VARCHAR(20) COMMENT '联系电话',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '软删除标记',

    UNIQUE KEY uk_profile_member (member_id),

    CONSTRAINT fk_profile_member FOREIGN KEY (member_id) REFERENCES t_member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成员详情表';

-- ================================================
-- 4. 用户表 (t_user)
-- ================================================
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    member_id           BIGINT COMMENT '关联成员ID（可选）',
    username            VARCHAR(50) NOT NULL COMMENT '用户名',
    password            VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    email               VARCHAR(100) COMMENT '邮箱',
    role                ENUM('ADMIN', 'MEMBER') NOT NULL DEFAULT 'MEMBER' COMMENT '角色：ADMIN-管理员，MEMBER-普通成员',
    status              TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    last_login_at       DATETIME COMMENT '最后登录时间',
    last_login_ip       VARCHAR(50) COMMENT '最后登录IP',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '软删除标记',

    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_email (email),
    UNIQUE KEY uk_user_member (member_id),
    INDEX idx_user_role (role),

    CONSTRAINT fk_user_member FOREIGN KEY (member_id) REFERENCES t_member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ================================================
-- 5. 族谱路径表 (t_genealogy_path)
-- ================================================
DROP TABLE IF EXISTS t_genealogy_path;
CREATE TABLE t_genealogy_path (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '路径ID',
    ancestor_id         BIGINT NOT NULL COMMENT '祖先ID',
    descendant_id       BIGINT NOT NULL COMMENT '后代ID',
    depth               INT NOT NULL COMMENT '代数（距离）',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_ancestor_descendant (ancestor_id, descendant_id),
    INDEX idx_descendant (descendant_id),
    INDEX idx_ancestor (ancestor_id),

    CONSTRAINT fk_path_ancestor FOREIGN KEY (ancestor_id) REFERENCES t_member(id),
    CONSTRAINT fk_path_descendant FOREIGN KEY (descendant_id) REFERENCES t_member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='族谱路径表';

-- ================================================
-- 6. 邮件表 (t_mail)
-- ================================================
DROP TABLE IF EXISTS t_mail;
CREATE TABLE t_mail (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '邮件ID',
    family_id           BIGINT NOT NULL COMMENT '所属家族ID',
    from_user_id        BIGINT NOT NULL COMMENT '发件人ID',
    to_member_id        BIGINT NOT NULL COMMENT '收件成员ID',
    subject             VARCHAR(200) NOT NULL COMMENT '邮件主题',
    content             TEXT NOT NULL COMMENT '邮件内容',
    is_read             TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    is_deleted          TINYINT DEFAULT 0 COMMENT '是否删除（发件人）',
    deleted_by_receiver TINYINT DEFAULT 0 COMMENT '收件人是否删除',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_mail_family (family_id),
    INDEX idx_mail_from (from_user_id),
    INDEX idx_mail_to (to_member_id),
    INDEX idx_mail_created (created_at),
    INDEX idx_mail_read (is_read),

    CONSTRAINT fk_mail_family FOREIGN KEY (family_id) REFERENCES t_family(id),
    CONSTRAINT fk_mail_from FOREIGN KEY (from_user_id) REFERENCES t_user(id),
    CONSTRAINT fk_mail_to FOREIGN KEY (to_member_id) REFERENCES t_member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件表';

-- ================================================
-- 7. 动态表 (t_post)
-- ================================================
DROP TABLE IF EXISTS t_post;
CREATE TABLE t_post (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '动态ID',
    family_id           BIGINT NOT NULL COMMENT '所属家族ID',
    author_id           BIGINT NOT NULL COMMENT '作者ID',
    title               VARCHAR(200) NOT NULL COMMENT '标题',
    content             TEXT NOT NULL COMMENT '内容',
    type                ENUM('EVENT', 'ACHIEVEMENT', 'MILESTONE', 'OTHER') DEFAULT 'OTHER' COMMENT '类型',
    status              ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '审核状态',
    reject_reason       VARCHAR(500) COMMENT '拒绝原因',
    view_count          INT DEFAULT 0 COMMENT '浏览次数',
    like_count          INT DEFAULT 0 COMMENT '点赞次数',
    reviewer_id         BIGINT COMMENT '审核人ID',
    reviewed_at         DATETIME COMMENT '审核时间',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '软删除标记',

    INDEX idx_post_family (family_id),
    INDEX idx_post_author (author_id),
    INDEX idx_post_status (status),
    INDEX idx_post_type (type),
    INDEX idx_post_created (created_at),

    CONSTRAINT fk_post_family FOREIGN KEY (family_id) REFERENCES t_family(id),
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES t_user(id),
    CONSTRAINT fk_post_reviewer FOREIGN KEY (reviewer_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态表';

-- ================================================
-- 8. 评论表 (t_comment)
-- ================================================
DROP TABLE IF EXISTS t_comment;
CREATE TABLE t_comment (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    post_id             BIGINT NOT NULL COMMENT '动态ID',
    author_id           BIGINT NOT NULL COMMENT '评论人ID',
    parent_id           BIGINT COMMENT '父评论ID（回复）',
    content             TEXT NOT NULL COMMENT '评论内容',
    like_count          INT DEFAULT 0 COMMENT '点赞次数',
    status              TINYINT DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '软删除标记',

    INDEX idx_comment_post (post_id),
    INDEX idx_comment_author (author_id),
    INDEX idx_comment_parent (parent_id),

    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES t_post(id),
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES t_user(id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES t_comment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ================================================
-- 9. 个人动态表 (t_personal_post)
-- ================================================
DROP TABLE IF EXISTS t_personal_post;
CREATE TABLE t_personal_post (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '动态ID',
    member_id           BIGINT NOT NULL COMMENT '成员ID',
    content             TEXT NOT NULL COMMENT '内容',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '软删除标记',

    INDEX idx_personal_member (member_id),
    INDEX idx_personal_created (created_at),

    CONSTRAINT fk_personal_member FOREIGN KEY (member_id) REFERENCES t_member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人动态表';

-- ================================================
-- 10. 纪念堂表 (t_memorial)
-- ================================================
DROP TABLE IF EXISTS t_memorial;
CREATE TABLE t_memorial (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '纪念ID',
    family_id           BIGINT NOT NULL COMMENT '所属家族ID',
    member_id           BIGINT NOT NULL COMMENT '成员ID',
    title               VARCHAR(200) NOT NULL COMMENT '标题（如：家族楷模）',
    bio                 TEXT COMMENT '人物简介',
    photo_url           VARCHAR(500) COMMENT '照片URL',
    achievement         TEXT COMMENT '成就描述',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '软删除标记',

    INDEX idx_memorial_family (family_id),
    INDEX idx_memorial_member (member_id),

    CONSTRAINT fk_memorial_family FOREIGN KEY (family_id) REFERENCES t_family(id),
    CONSTRAINT fk_memorial_member FOREIGN KEY (member_id) REFERENCES t_member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='纪念堂表';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- 初始化示例数据
-- ================================================

-- 插入示例家族
INSERT INTO t_family (id, name, description, status) VALUES
(1, '张三家族', '记录张三家族的族谱和历史', 1);

-- 插入示例成员（第一代祖先，无父亲）
INSERT INTO t_member (id, family_id, name, gender, generation, status) VALUES
(1, 1, '张老爷子', 'MALE', 1, 1),
(2, 1, '张大', 'MALE', 2, 1),
(3, 1, '张二', 'MALE', 2, 1),
(4, 1, '张三', 'MALE', 2, 1);

-- 设置父子关系
UPDATE t_member SET father_id = 1 WHERE id IN (2, 3, 4);

-- 插入第二代成员
INSERT INTO t_member (id, family_id, name, gender, generation, father_id, status) VALUES
(5, 1, '张小一', 'MALE', 3, 2, 1),
(6, 1, '张小二', 'MALE', 3, 2, 1),
(7, 1, '张秀英', 'FEMALE', 3, 3, 1);

-- 插入族谱路径（预计算祖先-后代关系）
INSERT INTO t_genealogy_path (ancestor_id, descendant_id, depth) VALUES
-- 老爷子为祖先
(1, 1, 0), (1, 2, 1), (1, 3, 1), (1, 4, 1),
(1, 5, 2), (1, 6, 2), (1, 7, 2),
-- 张大为祖先
(2, 2, 0), (2, 5, 1), (2, 6, 1),
-- 张二为祖先
(3, 3, 0), (3, 7, 1),
-- 张三为祖先
(4, 4, 0),
-- 子代自身
(5, 5, 0), (6, 6, 0), (7, 7, 0);

-- 验证查询示例（查询某人的所有祖先）
-- SELECT m.* FROM t_member m
-- INNER JOIN t_genealogy_path p ON m.id = p.ancestor_id
-- WHERE p.descendant_id = 5 ORDER BY p.depth;

-- 验证查询示例（查询某人的所有后代）
-- SELECT m.* FROM t_member m
-- INNER JOIN t_genealogy_path p ON m.id = p.descendant_id
-- WHERE p.ancestor_id = 1 AND p.depth > 0 ORDER BY m.generation;
