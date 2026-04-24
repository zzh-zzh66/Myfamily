# MyFamily - 数据库设计文档

| 版本 | 日期 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 1.0.0 | 2026-04-24 | 初始数据库设计 | DBA |

---

## 1. 数据库设计概述

### 1.1 设计原则

| 原则 | 说明 |
|------|------|
| 规范化设计 | 遵循第三范式(3NF)，消除数据冗余 |
| 业务驱动 | 以业务需求为导向设计表结构 |
| 扩展性 | 预留字段或使用JSON类型支持未来扩展 |
| 性能优先 | 大数据量表需考虑分区/索引优化 |

### 1.2 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 数据库 | 小写下划线 | myfamily |
| 表名 | t_{业务}_{含义} | t_member_info |
| 视图 | v_{业务}_{含义} | v_member_tree |
| 索引 | idx_{表}_{字段} | idx_member_family |
| 外键 | fk_{子表}_{父表} | fk_member_family |

---

## 2. ER关系图

### 2.1 实体关系

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  t_family   │       │  t_member   │       │t_member_    │
│  (家族)     │       │  (成员)     │       │ profile     │
├─────────────┤       ├─────────────┤       │(成员详情)   │
│ id (PK)     │◄──┐  │ id (PK)     │       ├─────────────┤
│ name        │   │  │ family_id   │───────┤ id (PK)     │
│ description │   │  │ name        │  N:1  │ member_id   │
│ created_at  │   │  │ gender      │◄──────│ (FK)        │
└─────────────┘   │  │ generation  │       │ avatar      │
                  │  │ father_id   │       │ birth_date  │
                  │  │ mother_id   │       │ death_date  │
                  │  │ status      │       │籍贯/intro   │
                  │  └─────────────┘       │ ...         │
                  │         │              └─────────────┘
                  │         │ 1:1
                  │         ▼
                  │  ┌─────────────┐
                  │  │   t_user    │
                  │  │  (用户)     │
                  │  ├─────────────┤
                  │  │ id (PK)     │
                  └─│ member_id   │◄──┘
                    │ username   │
                    │ password   │
                    │ role       │
                    │ status     │
                    └─────────────┘

┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  t_mail     │       │  t_post     │       │  t_comment  │
│  (邮件)     │       │  (动态)     │       │  (评论)     │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │       │ id (PK)     │
│ from_id     │───┐  │ family_id   │  1:N  │ post_id     │
│ (FK)        │   │  │ author_id   │◄──┐   │ (FK)        │
│ to_id       │   │  │ title       │   │  │ author_id   │
│ (FK)        │   │  │ content     │   │  │ (FK)        │
│ subject     │   │  │ status      │   │  │ content     │
│ content     │   │  │ created_at  │   │  │ created_at  │
│ is_read     │   │  └─────────────┘   │  └─────────────┘
│ created_at  │   │                    │
└─────────────┘   │                    │
                  │                    │
                  │         1:N        │
                  │         ▼          │
                  │  ┌─────────────┐     │
                  │  │ t_memorial │     │
                  │  │ (纪念堂)    │     │
                  │  ├─────────────┤     │
                  │  │ id (PK)     │     │
                  └─│ member_id   │◄────┘
                    │ title       │
                    │ bio         │
                    │ photo_url   │
                    │ created_at  │
                    └─────────────┘
```

---

## 3. 表结构详细设计

### 3.1 家族表 (t_family)

```sql
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
```

### 3.2 成员表 (t_member)

```sql
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
```

### 3.3 成员详情表 (t_member_profile)

```sql
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
```

### 3.4 用户表 (t_user)

```sql
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
```

### 3.5 族谱路径表 (t_genealogy_path)

用于优化族谱树的递归查询，提前存储祖先-后代关系。

```sql
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
```

### 3.6 邮件表 (t_mail)

```sql
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
```

### 3.7 动态表 (t_post)

```sql
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
```

### 3.8 评论表 (t_comment)

```sql
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
```

### 3.9 个人动态表 (t_personal_post)

用于存储个人主页的私有动态，与家族动态(t_post)分离。

```sql
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
```

### 3.10 纪念堂表 (t_memorial)

```sql
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
```

---

## 4. 索引设计汇总

### 4.1 索引清单

| 表名 | 索引名 | 字段 | 类型 | 说明 |
|------|--------|------|------|------|
| t_family | idx_family_status | status | NORMAL | 按状态查询 |
| t_family | idx_family_admin | admin_id | NORMAL | 管理员查询 |
| t_member | idx_member_family | family_id | NORMAL | 家族成员查询 |
| t_member | idx_member_father | father_id | NORMAL | 父子关系 |
| t_member | idx_member_mother | mother_id | NORMAL | 母子关系 |
| t_member | idx_member_generation | generation | NORMAL | 辈分查询 |
| t_member | idx_member_name | name | NORMAL | 姓名搜索 |
| t_user | idx_user_role | role | NORMAL | 角色查询 |
| t_mail | idx_mail_from | from_user_id | NORMAL | 发件查询 |
| t_mail | idx_mail_to | to_member_id | NORMAL | 收件查询 |
| t_mail | idx_mail_read | is_read | NORMAL | 未读查询 |
| t_post | idx_post_status | status | NORMAL | 审核状态 |
| t_post | idx_post_created | created_at | NORMAL | 时间排序 |

### 4.2 复合索引

| 索引名 | 表 | 字段顺序 | 用途 |
|--------|-----|---------|------|
| uk_profile_member | t_member_profile | member_id | 唯一索引 |
| uk_user_username | t_user | username | 唯一索引 |
| uk_user_email | t_user | email | 唯一索引 |
| uk_ancestor_descendant | t_genealogy_path | ancestor_id, descendant_id | 唯一索引 |

---

## 5. 初始化SQL脚本

### 5.1 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS myfamily
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 5.2 创建表

执行上述所有 CREATE TABLE 语句。

### 5.3 初始化示例数据

```sql
-- 插入示例家族
INSERT INTO t_family (id, name, description, status) VALUES
(1, '张三家族', '记录张三家族的族谱和历史', 1);

-- 插入示例成员（第一代祖先，无父亲）
INSERT INTO t_member (id, family_id, name, gender, generation, status) VALUES
(1, 1, '张老爷子', 'MALE', 1, 1),
(2, 1, '张大', 'MALE', 2, 1),
(3, 1, '张二', 'MALE', 2, 1);

-- 设置父子关系
UPDATE t_member SET father_id = 1 WHERE id IN (2, 3);

-- 插入族谱路径
INSERT INTO t_genealogy_path (ancestor_id, descendant_id, depth) VALUES
(1, 1, 0), (1, 2, 1), (1, 3, 1),
(2, 2, 0), (3, 3, 0);
```

---

## 6. 数据库维护

### 6.1 定期维护任务

| 任务 | 频率 | 说明 |
|------|------|------|
| 备份 | 每日 | 全量备份 |
| 索引优化 | 每周 | ANALYZE TABLE |
| 慢查询分析 | 每周 | 优化慢查询 |
| 空间清理 | 每月 | 清理临时数据 |

### 6.2 数据安全规范

- 生产环境禁止直接执行 UPDATE/DELETE（必须带 WHERE 条件）
- 执行危险操作前必须备份
- 敏感数据加密存储
- 定期更换数据库密码

---

## 7. 设计检查清单

- [x] 表名符合命名规范
- [x] 字段类型选择合理
- [x] 必填审计字段已添加（created_by, created_at, updated_by, updated_at, deleted）
- [x] 索引已创建（主键、外键、常用查询字段）
- [x] 外键约束已设置
- [x] 注释已添加（表注释、字段注释）
- [x] Entity类在 genealogy-common 模块创建
- [x] Mapper接口在 genealogy-common 模块创建
