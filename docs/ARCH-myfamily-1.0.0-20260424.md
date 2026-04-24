# MyFamily - 架构设计文档

| 版本 | 日期 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 1.0.0 | 2026-04-24 | 初始架构设计 | ARCH |

---

## 1. 技术可行性评估

### 1.1 核心技术评估

| 功能需求 | 技术方案 | 可行性 | 风险等级 | 备注 |
|----------|----------|--------|----------|------|
| 族谱树水墨风格渲染 | Canvas 2D + 自定义绘制 | ✅ 可行 | 中 | 需优化大数据量渲染性能 |
| 无需互关注的邮件系统 | 传统邮件表结构设计 | ✅ 可行 | 低 | 实现简单 |
| 中国风信封样式 | CSS + 图片资源 | ✅ 可行 | 低 | 纯前端实现 |
| 论坛+审核流程 | 动态表 + 状态机 | ✅ 可行 | 低 | 标准CRUD+审核状态 |
| 纪念堂展示 | 独立模块 | ✅ 可行 | 低 | 简单展示型页面 |
| 个人主页私有动态 | 独立动态表+隐私控制 | ✅ 可行 | 低 | 独立表存储 |

### 1.2 技术挑战与应对

| 挑战 | 影响 | 应对方案 |
|------|------|----------|
| **族谱树Canvas渲染性能** | 中 | 视口裁剪、层级简化、懒加载 |
| **递归查询族谱关系** | 中 | 数据库递归查询优化 + 缓存 |
| **水墨风格实现** | 中 | 使用CSS滤镜 + Canvas阴影 |
| **大量成员邮件查询** | 低 | 分页 + 索引优化 |

---

## 2. 系统架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                         │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │
│  │族谱树页面│ │成员管理 │ │家族信箱 │ │家族动态 │           │
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘           │
└───────┼──────────┼──────────┼──────────┼───────────────────┘
        │          │          │          │
        └──────────┴──────────┴──────────┘
                          │
                    ┌─────┴─────┐
                    │  API Gateway │
                    │   (Nginx)  │
                    └─────┬─────┘
                          │
┌─────────────────────────┴───────────────────────────────────┐
│                      后端 (Spring Boot 3)                    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                 genealogy-web (Controller)          │    │
│  └─────────────────────────────────────────────────────┘    │
│           │                │                │               │
│  ┌────────┴────┐  ┌────────┴────┐  ┌────────┴────┐          │
│  │genealogy-   │  │genealogy-   │  │genealogy-   │          │
│  │  member     │  │  social     │  │   auth      │          │
│  └──────┬──────┘  └──────┬──────┘  └─────────────┘          │
│         │                │                                 │
│  ┌──────┴────────────────────────────────────────────────┐  │
│  │              genealogy-common (Entity/Mapper/DTO)    │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                          │
                    ┌─────┴─────┐
                    │   MySQL   │
                    │   8.0+    │
                    └───────────┘
```

### 2.2 技术栈确认

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端框架 | Spring Boot | 3.2.x | 应用框架 |
| 安全框架 | Spring Security | 6.x | JWT认证 |
| ORM | MyBatis-Plus | 3.5.x | CRUD增强 |
| Java版本 | JDK | 17 LTS | 长期支持 |
| 前端框架 | Vue.js | 3.4.x | 渐进式框架 |
| 构建工具 | Vite | 5.x | 快速构建 |
| 状态管理 | Pinia | 2.x | Vue状态管理 |
| UI组件 | Element Plus | 最新 | 组件库 |
| 数据库 | MySQL | 8.0+ | 主数据库 |
| 缓存 | Redis | 7.x | 热点数据缓存(可选) |

---

## 3. 模块详细设计

### 3.1 Maven多模块结构

```
myfamily-parent/                    # 父项目
├── pom.xml                         # 版本管理、依赖管理
│
├── myfamily-common/                # 公共模块
│   └── src/main/java/com.myfamily/
│       ├── entity/                # 实体类 (t_member, t_family, etc.)
│       ├── mapper/                # Mapper接口
│       ├── dto/                   # 数据传输对象
│       │   ├── request/          # 请求DTO
│       │   └── response/         # 响应DTO
│       ├── config/               # 配置类
│       ├── exception/            # 异常定义
│       └── common/               # 通用工具
│
├── myfamily-auth/                  # 认证模块
│   └── src/main/java/com.myfamily/
│       ├── service/              # 认证Service
│       └── controller/            # 认证Controller
│
├── myfamily-member/                # 成员管理模块
│   └── src/main/java/com.myfamily/
│       ├── service/             # 成员Service
│       └── controller/           # 成员Controller
│
├── myfamily-social/                # 社交功能模块
│   └── src/main/java/com.myfamily/
│       ├── service/             # 动态、评论Service
│       └── controller/           # 动态、评论Controller
│
└── myfamily-web/                   # Web入口模块
    └── src/main/java/com.myfamily/
        ├── controller/          # 族谱、邮件Controller
        └── MyFamilyApplication.java
```

### 3.2 模块依赖关系

```
                    ┌─────────────────┐
                    │   myfamily-web  │
                    │   (Web入口)     │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│ myfamily-auth │    │myfamily-member│    │myfamily-social│
│   (认证)      │    │   (成员)      │    │   (社交)      │
└───────┬───────┘    └───────┬───────┘    └───────────────┘
        │                    │
        └────────────────────┤
                             │
                    ┌────────┴────────┐
                    │ myfamily-common │
                    │   (公共模块)    │
                    └────────────────┘
```

### 3.3 前端项目结构

```
frontend/                           # 前端项目
├── src/
│   ├── api/                       # API接口定义
│   │   ├── member.ts             # 成员相关API
│   │   ├── genealogy.ts          # 族谱相关API
│   │   ├── mail.ts              # 邮件相关API
│   │   ├── forum.ts             # 动态相关API
│   │   ├── memorial.ts          # 纪念堂API
│   │   └── auth.ts              # 认证API
│   │
│   ├── components/               # 组件
│   │   ├── common/              # 通用组件
│   │   ├── genealogy/           # 族谱组件
│   │   │   ├── GenealogyTree.vue     # 族谱树主组件
│   │   │   ├── GenealogyCanvas.vue   # Canvas渲染组件
│   │   │   └── GenealogyNode.vue     # 节点组件
│   │   ├── mail/               # 邮件组件
│   │   │   ├── Envelope.vue         # 电子信封
│   │   │   └── MailItem.vue         # 邮件列表项
│   │   └── forum/              # 论坛组件
│   │
│   ├── views/                    # 页面
│   │   ├── genealogy/
│   │   │   ├── GenealogyTreeView.vue   # 族谱树页面
│   │   │   └── MemberDetailView.vue    # 成员详情页
│   │   ├── member/
│   │   │   ├── MemberListView.vue      # 成员列表
│   │   │   └── ProfileEditView.vue     # 个人主页编辑
│   │   ├── mail/
│   │   │   ├── InboxView.vue           # 收件箱
│   │   │   ├── OutboxView.vue          # 发件箱
│   │   │   └── ComposeView.vue        # 写信页面
│   │   ├── forum/
│   │   │   ├── ForumView.vue           # 动态列表
│   │   │   ├── PostDetailView.vue      # 动态详情
│   │   │   └── PostEditorView.vue      # 发布动态
│   │   ├── memorial/
│   │   │   └── MemorialView.vue        # 纪念堂
│   │   └── user/
│   │       ├── LoginView.vue            # 登录页
│   │       └── RegisterView.vue        # 注册页
│   │
│   ├── stores/                    # Pinia状态管理
│   │   ├── user.ts               # 用户状态
│   │   ├── genealogy.ts          # 族谱状态
│   │   └── app.ts                # 应用状态
│   │
│   ├── router/                   # 路由配置
│   │   └── index.ts
│   │
│   ├── utils/                    # 工具函数
│   │   ├── request.ts           # Axios封装
│   │   └── canvas.ts            # Canvas水墨绘图工具
│   │
│   └── types/                    # TypeScript类型
│       └── api.d.ts
│
└── public/
    └── images/                   # 静态图片资源
        └── ink/                  # 水墨风格图片
```

---

## 4. 数据库设计

### 4.1 核心表结构

| 表名 | 说明 | 优先级 |
|------|------|--------|
| t_family | 家族表 | P0 |
| t_member | 成员表（核心） | P0 |
| t_member_profile | 成员详情表 | P0 |
| t_user | 用户表（认证） | P0 |
| t_genealogy_node | 族谱关系表 | P0 |
| t_mail | 邮件表 | P1 |
| t_post | 动态表 | P1 |
| t_comment | 评论表 | P1 |
| t_memorial | 纪念堂表 | P2 |

### 4.2 ER关系图

```
t_family (家族)
    │
    │ 1:N
    ▼
t_member (成员) ◄──────────────┐
    │                            │
    │ 1:1 (一对一)               │ N:1 (父子)
    │                            │
    ▼                            │
t_member_profile (详情)          │
    │                            │
    │ N:1                        │
    ▼                            │
t_user (用户) ────────────────────┘
    │                                 ▲
    │ 1:N                             │ N:1 (父子)
    ▼                                 │
t_mail (邮件) ──────► t_member         │
    │                            ┌─────┴─────┐
    │                            │ t_genealogy_node
    │ 1:N                        │ (族谱关系表)
    ▼                            └───────────┘
t_post (动态)
    │
    │ 1:N
    ▼
t_comment (评论)
    │
    │ N:1
    ▼
t_member ◄──────── t_memorial (纪念堂)
```

### 4.3 族谱数据结构

族谱使用**邻接表模型** + **路径枚举**混合方式：

```sql
-- 成员表
CREATE TABLE t_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    birth_date DATE,
    death_date DATE,
    generation INT NOT NULL,              -- 辈分
    father_id BIGINT,                      -- 父亲ID（邻接表）
    mother_id BIGINT,                      -- 母亲ID（邻接表）
    status TINYINT DEFAULT 1,
    created_by BIGINT,
    created_at DATETIME,
    updated_by BIGINT,
    updated_at DATETIME,
    deleted TINYINT DEFAULT 0,

    INDEX idx_member_family (family_id),
    INDEX idx_member_father (father_id),
    INDEX idx_member_generation (generation)
);

-- 族谱路径表（用于快速查询子树）
CREATE TABLE t_genealogy_path (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ancestor_id BIGINT NOT NULL,           -- 祖先ID
    descendant_id BIGINT NOT NULL,        -- 后代ID
    depth INT NOT NULL,                   -- 距离

    UNIQUE KEY uk_ancestor_descendant (ancestor_id, descendant_id),
    INDEX idx_descendant (descendant_id)
);
```

---

## 5. API接口设计

### 5.1 接口分组

| 模块 | 前缀 | 说明 |
|------|------|------|
| 认证 | /api/v1/auth | 登录、注册 |
| 成员 | /api/v1/members | 成员CRUD |
| 族谱 | /api/v1/genealogy | 族谱树 |
| 邮件 | /api/v1/mails | 邮件收发 |
| 动态 | /api/v1/posts | 论坛动态 |
| 评论 | /api/v1/comments | 评论 |
| 纪念堂 | /api/v1/memorials | 纪念堂 |

### 5.2 核心接口清单

#### 族谱接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/genealogy/tree | 获取族谱树数据 |
| GET | /api/v1/genealogy/members/:id | 获取成员及其后代 |
| POST | /api/v1/genealogy/members | 添加成员（管理员） |
| PUT | /api/v1/genealogy/members/:id | 更新成员（管理员） |
| DELETE | /api/v1/genealogy/members/:id | 删除成员（管理员） |

#### 邮件接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/mails/inbox | 收件箱 |
| GET | /api/v1/mails/outbox | 发件箱 |
| POST | /api/v1/mails | 发送邮件 |
| GET | /api/v1/mails/:id | 查看邮件 |

#### 动态接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/posts | 获取动态列表（已审核） |
| POST | /api/v1/posts | 发布动态 |
| GET | /api/v1/posts/:id | 动态详情 |
| GET | /api/v1/posts/pending | 待审核动态（管理员） |
| PUT | /api/v1/posts/:id/approve | 审核通过（管理员） |
| PUT | /api/v1/posts/:id/reject | 审核拒绝（管理员） |

---

## 6. 性能优化策略

### 6.1 族谱树渲染优化

```javascript
// Canvas渲染优化方案
const GenealogyRenderer = {
    // 1. 视口裁剪 - 只渲染可见区域节点
    viewportCulling: {
        enabled: true,
        buffer: 100  // 缓冲区像素
    },

    // 2. 层级简化 - 根据缩放级别决定渲染细节
    levelBasedDetail: {
        zoom > 0.8: 'full',      // 完整信息
        zoom > 0.5: 'name_only',  // 仅姓名
        zoom <= 0.5: 'dot'        // 仅圆点
    },

    // 3. 节点懒加载 - 按需加载深层节点
    lazyLoad: {
        enabled: true,
        maxInitialDepth: 3,       // 初始加载深度
        loadOnExpand: true       // 展开时加载
    },

    // 4. Canvas缓存 - 缓存静态元素
    cacheStrategy: {
        cacheBackground: true,
        cacheNodes: true
    }
}
```

### 6.2 数据库优化

| 优化点 | 方案 | 预期效果 |
|--------|------|----------|
| 族谱递归查询 | 使用 WITH RECURSIVE + path表 | 查询时间 < 50ms |
| 成员列表分页 | 索引优化 + COUNT缓存 | 查询时间 < 100ms |
| 动态列表查询 | 状态索引 + 分页 | 查询时间 < 100ms |
| 邮件查询 | 收件人索引 | 查询时间 < 50ms |

---

## 7. 安全设计

### 7.1 认证授权

| 功能 | 实现方式 |
|------|----------|
| 认证方式 | JWT Token |
| Token有效期 | 7天（可刷新） |
| 密码存储 | BCrypt |
| 管理员权限 | Role字段 + @PreAuthorize |

### 7.2 接口安全

| 安全措施 | 实现 |
|----------|------|
| SQL注入 | MyBatis参数化查询 |
| XSS攻击 | 输入输出转义 |
| CSRF | Spring Security禁用CSRF（API场景） |
| 敏感操作日志 | AOP记录关键操作 |

---

## 8. 风险评估

| 风险 | 概率 | 影响 | 应对策略 |
|------|------|------|----------|
| Canvas渲染卡顿 | 中 | 中 | 优化算法、降低细节、懒加载 |
| 数据库递归查询慢 | 低 | 高 | 路径表预计算、缓存 |
| 水墨风格实现困难 | 低 | 低 | 参考现有实现、迭代优化 |
| 大规模数据性能 | 中 | 中 | 分页、缓存、索引优化 |

---

## 9. 开发计划

### 阶段一：基础架构（1-2周）

| 任务 | 负责人 | 产出物 |
|------|--------|--------|
| 项目结构搭建 | ARCH | Maven多模块项目 |
| 数据库设计 | DBA | ER图 + DDL |
| 公共模块开发 | BE | Entity/Mapper/DTO |
| 认证模块开发 | BE | JWT认证接口 |

### 阶段二：核心功能（3-5周）

| 任务 | 负责人 | 产出物 |
|------|--------|--------|
| 族谱树API | BE | 族谱CRUD接口 |
| 族谱树前端 | FE+UI | Canvas族谱组件 |
| 成员管理API | BE | 成员CRUD接口 |
| 成员管理前端 | FE | 成员列表/详情页 |

### 阶段三：社交功能（6-7周）

| 任务 | 负责人 | 产出物 |
|------|--------|--------|
| 邮件模块开发 | BE+FE | 邮件收发功能 |
| 动态模块开发 | BE+FE | 论坛+审核功能 |
| 纪念堂开发 | BE+FE | 纪念堂展示 |

---

## 10. 架构评审清单

- [x] 技术选型合理
- [x] 模块划分清晰
- [x] 依赖关系无循环
- [x] 数据库设计符合规范
- [x] API设计RESTful
- [x] 安全措施到位
- [x] 性能指标明确
- [x] 风险识别并有对策

---

## 11. 后续工作

| 工作项 | 负责人 | 说明 |
|--------|--------|------|
| 数据库详细设计 | DBA | 产出DDL脚本 |
| UI原型设计 | UI | 水墨风格设计稿 |
| 接口文档 | BE | Swagger/OpenAPI |
| 测试数据 | CG | 示例数据生成 |
