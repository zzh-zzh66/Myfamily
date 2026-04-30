# MyFamily 家族族谱管理系统

一款面向家族的数字化族谱管理平台，支持族谱可视化、成员管理、家族信箱、家族动态、纪念堂等核心功能。

## 技术栈

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | 渐进式前端框架（Composition API） |
| TypeScript | 类型安全保障 |
| Vite 5 | 快速构建工具 |
| Pinia | Vue 3 状态管理 |
| Vue Router 4 | 页面路由 |
| Element Plus | UI 组件库 |
| Axios | HTTP 客户端 |

### 后端

| 技术 | 说明 |
|------|------|
| Spring Boot 3.2 | 应用框架 |
| Spring Security 6 | 安全认证 |
| MyBatis-Plus 3.5 | ORM 增强框架 |
| JWT (jjwt 0.12) | 无状态身份认证 |
| MySQL 8.0 | 关系型数据库 |
| Hutool | Java 工具库 |

### 架构

- **Maven 多模块**：父项目统一版本管理，子模块职责分离
  - `myfamily-common` - 公共实体、DTO、Mapper
  - `myfamily-auth` - 认证模块
  - `myfamily-member` - 成员管理模块
  - `myfamily-social` - 社交功能模块
  - `myfamily-web` - Web 入口模块

## 功能实现

### 核心功能

| 模块 | 功能 |
|------|------|
| 族谱管理 | Canvas 可视化族谱树、成员增删改查、父子关系维护、辈分管理 |
| 成员管理 | 成员信息管理、个人主页、私有动态控制 |
| 家族信箱 | 点对点邮件收发、中国风信封样式、无需互相关注 |
| 家族动态 | 论坛发帖、评论互动、管理员审核机制 |
| 纪念堂 | 逝者纪念展示、数字相册 |

### 数据库

- 基于邻接表 + 路径枚举的族谱数据结构
- 支持递归查询优化（WITH RECURSIVE + 预计算路径表）
- 完整的迁移脚本管理

## 项目亮点

### 1. 水墨风格可视化族谱

采用 Canvas 2D 自主研发族谱树渲染引擎，支持：

- **视口裁剪**：仅渲染可见区域，大幅降低大数据量渲染压力
- **层级简化**：根据缩放级别动态调整节点信息密度
- **节点懒加载**：按需加载深层节点，首屏加载更快
- **Canvas 缓存**：静态元素缓存复用，避免重复绘制

### 2. 高性能族谱查询

通过 **邻接表 + 路径枚举** 混合数据结构设计：

- 解决递归查询性能瓶颈
- 支持快速查询任意节点的祖先路径和后代子树
- 数据库索引优化，查询时间控制在 50ms 以内

### 3. 安全认证体系

- JWT 无状态认证，支持 Token 刷新
- BCrypt 密码加密存储
- 基于角色的权限控制（@PreAuthorize）
- 敏感操作审计日志

### 4. 模块化架构设计

- Maven 多模块分层，职责清晰
- 公共模块抽取复用，减少代码冗余
- 统一版本管理，依赖管理更规范
- 支持微服务扩展（模块可独立部署）

### 5. 完整的前端工程化

- TypeScript 类型保障，降低运行时错误
- Vite 极速开发体验（HMR）
- 组件自动导入（unplugin-auto-import）
- Pinia 持久化状态管理
- Vitest 单元测试覆盖

## 项目结构

```
e:\MyFamily\Myfamily
├── backend/                    # 后端 Spring Boot 项目
│   └── myfamily-parent/        # Maven 父项目
│       ├── myfamily-common/    # 公共模块
│       ├── myfamily-auth/      # 认证模块
│       ├── myfamily-member/    # 成员模块
│       ├── myfamily-social/    # 社交模块
│       └── myfamily-web/       # Web 入口
├── frontend/                   # 前端 Vue 3 项目
│   └── src/
│       ├── api/               # API 接口
│       ├── components/        # 组件
│       ├── views/             # 页面
│       ├── stores/            # 状态管理
│       └── utils/            # 工具函数
├── db/                        # 数据库脚本
└── docs/                      # 项目文档
```

## API 接口

| 模块 | 前缀 | 说明 |
|------|------|------|
| 认证 | `/api/v1/auth` | 登录、注册 |
| 成员 | `/api/v1/members` | 成员 CRUD |
| 族谱 | `/api/v1/genealogy` | 族谱树 |
| 邮件 | `/api/v1/mails` | 邮件收发 |
| 动态 | `/api/v1/posts` | 论坛动态 |
| 纪念堂 | `/api/v1/memorials` | 纪念堂 |
