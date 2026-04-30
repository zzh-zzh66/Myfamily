# 智能体模块 - 任务分解

**版本**: v1.0.0
**日期**: 2026-04-30

---

## 一、后端开发任务

### 1.1 环境搭建

| 任务 | 说明 | 优先级 |
|------|------|--------|
| 1.1.1 | 在 myfamily-parent 下创建 myfamily-agent 模块 | P0 |
| 1.1.2 | 引入 Spring AI 依赖（参考 Spring AI 1.1.x） | P0 |
| 1.1.3 | 引入智谱 GLM starter 依赖 | P0 |
| 1.1.4 | 配置智谱 API Key | P0 |
| 1.1.5 | 编写 SpringAIConfig 配置类 | P0 |

### 1.2 数据库相关

| 任务 | 说明 | 优先级 |
|------|------|--------|
| 1.2.1 | 编写数据库迁移脚本（t_memorial_agent） | P0 |
| 1.2.2 | 创建 MemorialAgent 实体类 | P0 |
| 1.2.3 | 创建 MemorialAgentMapper | P1 |
| 1.2.4 | 创建 AgentMapperXML | P1 |

### 1.3 Service层

| 任务 | 说明 | 优先级 |
|------|------|--------|
| 1.3.1 | 创建 AgentService 接口 | P0 |
| 1.3.2 | 实现 AgentServiceImpl（CRUD） | P0 |
| 1.3.3 | 创建 DocumentParserService（文档解析） | P0 |
| 1.3.4 | 创建 PersonaExtractionService（人设提取） | P0 |
| 1.3.5 | 创建 ChatService（对话服务） | P0 |

### 1.4 Controller层

| 任务 | 说明 | 优先级 |
|------|------|--------|
| 1.4.1 | 创建 AgentController | P0 |
| 1.4.2 | 实现 getAgentStatus（获取状态） | P0 |
| 1.4.3 | 实现 createAgent（创建智能体） | P0 |
| 1.4.4 | 实现 uploadDocument（上传文档） | P0 |
| 1.4.5 | 实现 evaluateDocument（评估） | P0 |
| 1.4.6 | 实现 chat（对话） | P0 |
| 1.4.7 | 实现 deleteAgent（删除） | P1 |

---

## 二、前端开发任务

### 2.1 API层

| 任务 | 说明 | 优先级 |
|------|------|--------|
| 2.1.1 | 创建 api/agent.ts | P0 |
| 2.1.2 | 定义 Agent 类型接口 | P0 |

### 2.2 组件开发

| 任务 | 说明 | 优先级 |
|------|------|--------|
| 2.2.1 | 创建 AgentCreateDialog.vue（创建向导） | P0 |
| 2.2.2 | 创建 EvaluationResult.vue（评估结果） | P0 |
| 2.2.3 | 创建修改建议组件 | P1 |

### 2.3 页面开发

| 任务 | 说明 | 优先级 |
|------|------|--------|
| 2.3.1 | 修改 MemorialDetailView.vue（增加智能体入口） | P0 |
| 2.3.2 | 创建 AgentChatView.vue（对话页面） | P0 |
| 2.3.3 | 配置路由 | P0 |

### 2.4 资源文件

| 任务 | 说明 | 优先级 |
|------|------|--------|
| 2.4.1 | 创建文档模板 memorial-agent-template.md | P0 |
| 2.4.2 | 添加模板下载功能 | P0 |

---

## 三、测试任务

| 任务 | 说明 | 优先级 |
|------|------|--------|
| 3.1 | 后端单元测试（Service层） | P1 |
| 3.2 | 后端接口测试 | P1 |
| 3.3 | 前端功能测试 | P1 |
| 3.4 | 端到端测试 | P1 |

---

## 四、优先级说明

| 优先级 | 说明 |
|--------|------|
| P0 | 必须完成，影响主流程 |
| P1 | 重要功能，不影响主流程 |
| P2 | 可选优化，后续迭代 |

---

## 五、开发顺序建议

```
第一阶段：环境 + 基础（1-2天）
├── 1.1 环境搭建
├── 1.2.1 数据库迁移脚本
├── 1.2.2 实体类
└── 1.4.1 AgentController（基础CRUD）

第二阶段：核心功能（3-5天）
├── 1.3.3 DocumentParserService
├── 1.3.4 PersonaExtractionService
├── 1.3.5 ChatService
├── 1.4.4 evaluateDocument
└── 1.4.6 chat

第三阶段：前端（2-3天）
├── 2.1 API层
├── 2.2.1 AgentCreateDialog
├── 2.3.1 修改详情页
├── 2.3.2 对话页面
└── 2.4 模板

第四阶段：测试 + 优化（1-2天）
├── 3.x 测试
└── 问题修复
```

---

## 六、任务清单

- [ ] 1.1.1 创建 myfamily-agent 模块
- [ ] 1.1.2 引入 Spring AI 依赖
- [ ] 1.1.3 引入智谱 GLM starter
- [ ] 1.1.4 配置智谱 API Key
- [ ] 1.1.5 编写 SpringAIConfig
- [ ] 1.2.1 编写数据库迁移脚本
- [ ] 1.2.2 创建 MemorialAgent 实体类
- [ ] 1.2.3 创建 MemorialAgentMapper
- [ ] 1.3.1 创建 AgentService 接口
- [ ] 1.3.2 实现 AgentServiceImpl
- [ ] 1.3.3 创建 DocumentParserService
- [ ] 1.3.4 创建 PersonaExtractionService
- [ ] 1.3.5 创建 ChatService
- [ ] 1.4.1 创建 AgentController
- [ ] 1.4.2-7 实现各API方法
- [ ] 2.1.1 创建 api/agent.ts
- [ ] 2.1.2 定义类型接口
- [ ] 2.2.1 创建 AgentCreateDialog
- [ ] 2.2.2 创建 EvaluationResult
- [ ] 2.3.1 修改 MemorialDetailView
- [ ] 2.3.2 创建 AgentChatView
- [ ] 2.3.3 配置路由
- [ ] 2.4.1 创建文档模板
- [ ] 2.4.2 模板下载功能
- [ ] 3.x 测试任务
