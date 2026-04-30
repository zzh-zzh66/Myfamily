# 纪念堂智能体模块 - 需求与方案文档

**版本**: v1.0.0
**日期**: 2026-04-30
**状态**: 待开发

---

## 一、项目概述

### 1.1 背景

MyFamily 纪念堂模块用于展示家族已故先贤的生平和成就。用户提出创新需求：

> **将纪念人物蒸馏成可对话的智能体，实现回忆、怀旧功能。**

### 1.2 目标

打造一个基于真实回忆文档的个性化智能体，让用户能够：
- **回忆往事**：与智能体对话，重温与先人的美好回忆
- **寄托思念**：在悲伤时获得温暖的精神陪伴

### 1.3 核心原则

```
【100%基于文档，系统不创造内容】

• 用户写什么 → 智能体就是什么
• 系统只做：提取、组装、结构化
• 系统不：推断、补充、假设、编造
• 不知道的 → 如实说"不太记得"
```

---

## 二、技术选型

| 模块 | 选择 | 说明 |
|------|------|------|
| AI框架 | Spring AI 1.1.x | 融入现有Spring Boot项目 |
| LLM | 智谱GLM-4-flash | ¥0.1/1M，国内可用 |
| 向量数据库 | 暂不引入（MVP阶段） | 可后续扩展 |
| 对话记忆 | 会话级 | 不持久化 |
| 文档格式 | Markdown + YAML | 用户友好 |

---

## 三、功能流程

```
┌─────────────────────────────────────────────────────────────────┐
│                      智能体生命周期                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 创建智能体                                                   │
│     ├── 用户进入纪念人物详情页                                    │
│     ├── 点击"打造智能体"                                         │
│     ├── 下载模板 → 填写 → 上传                                   │
│     ├── 系统评估（≥90分通过）                                    │
│     └── 激活智能体                                              │
│                                                                  │
│  2. 对话                                                        │
│     ├── 用户点击"开始对话"                                       │
│     ├── 进入对话页面                                             │
│     └── 多轮对话（会话级记忆）                                   │
│                                                                  │
│  3. 删除智能体                                                   │
│     └── 用户可删除已创建的智能体                                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 四、数据设计

### 4.1 数据库表

```sql
-- ================================================
-- 智能体配置表
-- ================================================
CREATE TABLE t_memorial_agent (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    memorial_id         BIGINT NOT NULL COMMENT '纪念人物ID',
    status              VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING待上传/ACTIVE已激活/FAILED未通过',
    document_content    TEXT COMMENT '用户上传的原始文档',
    persona_prompt     TEXT COMMENT '生成的人设Prompt',
    evaluation_score    INT COMMENT '评估得分',
    evaluation_detail   JSON COMMENT '评估详情',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_memorial (memorial_id),
    INDEX idx_agent_memorial (memorial_id),
    CONSTRAINT fk_agent_memorial FOREIGN KEY (memorial_id) REFERENCES t_memorial(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体配置表';
```

### 4.2 实体类

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| memorialId | Long | 关联的纪念人物ID |
| status | String | PENDING/ACTIVE/FAILED |
| documentContent | String | 用户上传的原始文档 |
| personaPrompt | String | 生成的人设Prompt |
| evaluationScore | Integer | 评估得分 |
| evaluationDetail | String(JSON) | 评估详情 |
| createdBy | Long | 创建人 |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

---

## 五、API 接口

| 方法 | 路径 | 说明 | 请求体 |
|------|------|------|--------|
| GET | `/api/v1/agents/memorial/{memorialId}` | 获取智能体状态 | - |
| POST | `/api/v1/agents/memorial/{memorialId}/create` | 创建智能体 | - |
| POST | `/api/v1/agents/{id}/upload` | 上传文档 | multipart/form-data |
| POST | `/api/v1/agents/{id}/evaluate` | 评估文档 | - |
| POST | `/api/v1/agents/{id}/chat` | 对话 | `{message, sessionId}` |
| DELETE | `/api/v1/agents/{id}` | 删除智能体 | - |

### 5.1 响应格式

```json
// 获取状态
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "memorialId": 1,
    "status": "PENDING", // PENDING | ACTIVE | FAILED
    "evaluationScore": null,
    "evaluationDetail": null
  }
}

// 对话
{
  "code": 200,
  "message": "success",
  "data": {
    "content": "孩子，爷爷想你了..."
  }
}
```

---

## 六、评估标准

### 6.1 评分维度

| 维度 | 满分 | 达标条件 | 扣分规则 |
|------|------|---------|---------|
| 基本信息 | 20分 | 提供姓名、关系、生卒年 | 缺一项扣5分 |
| 整体印象 | 15分 | 描述≥50字 | 不足50字扣5分 |
| 外貌习惯 | 15分 | 提供外貌OR习惯OR口头禅（任一项） | 缺一项扣5分 |
| 性格情感 | 20分 | 提供性格OR情感表达（任一项） | 缺一项扣5分 |
| 回忆事件 | 30分 | 每个完整回忆得15分，最多30分 | 不完整扣10分/个 |

### 6.2 通过条件

```
总分 ≥ 90分 → 通过
总分 < 90分 → 不通过，显示缺失项
```

### 6.3 评估维度说明

**回忆事件完整标准**：
- 时间（什么时候）
- 事件（发生了什么）
- 反应（他/她说了什么、做了什么）
- 感受（用户的感受，可选）

---

## 七、人设生成规则

### 7.1 提取原则

```
【100%基于文档，系统不创造任何内容】

提取规则：
• 用户写了什么 → 提取什么
• 用户没写 → 留空，不编造
• 保持用户原话（口头禅、习惯用语等）

组装规则：
• 按固定模板结构组装
• 回忆事件按时间顺序
• 不知道的如实说"不太记得"
```

### 7.2 生成字段

| 字段 | 来源 | 处理方式 |
|------|------|---------|
| name | YAML | 直接提取 |
| title | YAML | 直接提取 |
| relationship | YAML | 直接提取 |
| birthYear | YAML | 直接提取 |
| deathYear | YAML | 直接提取 |
| overall | 第一部分 | 原文提取 |
| appearance | 第二部分 | 原文提取 |
| habits | 第二部分 | 原文提取为列表 |
| catchphrases | 第二部分 | 原文提取为列表 |
| personality | 第三部分 | 原文提取 |
| angerBehavior | 第三部分 | 原文提取 |
| joyBehavior | 第三部分 | 原文提取 |
| careExpression | 第三部分 | 原文提取 |
| memories | 第四部分 | 结构化提取 |
| supplement | 第五部分 | 原文提取 |

### 7.3 对话原则生成

```
【对话原则】
1. 回忆往事时：尽可能引用或参考记忆中提供的内容，保持真实感
2. 安慰亲人时：以温暖、接纳的态度陪伴，表达关爱
3. 保持性格：如果描述你沉默寡言，就少说话
4. 口头禅：适当使用用户提供的口头禅
5. 不知道的：如实说"这个不太记得"或"没听你提过"
6. 不要编造：不要创造文档中没有的新细节
```

---

## 八、前端页面

### 8.1 纪念人物详情页（改造）

在现有的 MemorialDetailView.vue 中增加智能体状态入口：

```
┌─────────────────────────────────────────────────────────────┐
│ 智能体状态                                                │
│ ─────────────────                                          │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │  ❌ 未创建智能体                                    │   │
│  │                                                    │   │
│  │  将此人蒸馏成可对话的智能体，                       │   │
│  │  可以回忆往事、寄托思念                             │   │
│  │                                                    │   │
│  │           [🌟 打造智能体]                          │   │
│  └────────────────────────────────────────────────────┘   │
│                                                             │
│  或                                                        │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │  ⚠️ 智能体待激活                                  │   │
│  │  上次上传的文档未通过评估，请重新上传              │   │
│  │           [📤 重新上传文档]                        │   │
│  └────────────────────────────────────────────────────┘   │
│                                                             │
│  或                                                        │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │  ✅ 智能体已就绪                                   │   │
│  │                                                    │   │
│  │           [💬 开始对话]                             │   │
│  └────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 打造智能体向导

**步骤1/3：引导说明**
- 展示流程说明
- 提供模板下载按钮

**步骤2/3：上传文档**
- 拖拽上传
- 支持 .md, .txt 格式
- 上传后自动触发评估

**步骤3/3：评估结果**
- 通过：显示成功 + "开始对话"按钮
- 不通过：显示缺失项 + "查看修改建议"按钮

### 8.3 对话页面

```
┌─────────────────────────────────────────────────────────────┐
│ 与李守仁对话                              [← 返回详情]      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                      │   │
│  │  【智能体】                           10:30          │   │
│  │  孩子，爷爷想你了。有什么想跟爷爷说的吗？            │   │
│  │                                                      │   │
│  │                                     【用户】 10:31   │   │
│  │         爷爷，我好想你...                            │   │
│  │                                                      │   │
│  │  【智能体】                           10:31          │   │
│  │  傻孩子，爷爷也知道你想我...                         │   │
│  │                                                      │   │
│  │  ┌─────────────────────────────────────────────┐   │   │
│  │  │ 输入你想说的话...                              │   │   │
│  │  └─────────────────────────────────────────────┘   │   │
│  │                                            [发送]   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 九、模块结构

### 9.1 后端结构

```
myfamily-parent/
└── myfamily-agent/                    # 新增模块
    ├── pom.xml
    └── src/main/java/com/family/myfamily/
        ├── controller/
        │   └── AgentController.java
        ├── service/
        │   ├── AgentService.java
        │   └── impl/
        │       ├── AgentServiceImpl.java
        │       ├── DocumentParserService.java
        │       ├── PersonaExtractionService.java
        │       └── ChatService.java
        ├── entity/
        │   └── MemorialAgent.java
        ├── config/
        │   └── SpringAIConfig.java
        └── parser/
            ├── DocumentParser.java
            ├── YamlHeaderParser.java
            └── EvaluationParser.java
```

### 9.2 前端结构

```
frontend/src/
├── api/
│   └── agent.ts                     # 新增
├── views/memorial/
│   ├── MemorialDetailView.vue       # 修改（增加智能体入口）
│   └── AgentChatView.vue           # 新增对话页
├── components/agent/                # 新增组件目录
│   ├── AgentCreateDialog.vue        # 创建向导
│   └── EvaluationResult.vue         # 评估结果
├── router/index.ts                  # 修改（增加路由）
└── types/api.ts                    # 修改（增加类型）
```

### 9.3 资源文件

```
frontend/public/
└── templates/
    └── memorial-agent-template.md   # 用户文档模板
```

---

## 十、模板设计

详见 `TEMPLATE.md`

---

## 十一、Prompt设计

详见 `PROMPTS.md`

---

## 十二、任务分解

详见 `TASKS.md`
