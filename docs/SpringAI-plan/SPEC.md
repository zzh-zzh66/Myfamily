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

### 4.1 数据库迁移脚本

```
db/migration/
├── v1.0.4_add_memorial_agent.sql    # 迁移脚本
└── v1.0.4_add_memorial_agent_entity.java  # 实体类（参考）
```

> **重要**：数据库字段命名使用 **snake_case**（下划线），Java实体类使用 **camelCase**（驼峰命名），MyBatis Plus自动处理映射。

### 4.2 字段映射关系

| 数据库字段（snake_case） | Java字段（camelCase） | 类型 | 说明 |
|------------------------|---------------------|------|------|
| id | id | Long | 主键 |
| memorial_id | memorialId | Long | 纪念人物ID |
| status | status | String | 状态 |
| document_content | documentContent | String | 原始文档 |
| persona_prompt | personaPrompt | String | 人设Prompt |
| evaluation_score | evaluationScore | Integer | 评估得分 |
| evaluation_detail | evaluationDetail | String(JSON) | 评估详情 |
| created_by | createdBy | Long | 创建人 |
| created_at | createdAt | LocalDateTime | 创建时间 |
| updated_at | updatedAt | LocalDateTime | 更新时间 |

### 4.3 数据库表

```sql
-- ================================================
-- 智能体配置表
-- ================================================
CREATE TABLE t_memorial_agent (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '智能体ID',
    memorial_id         BIGINT NOT NULL COMMENT '纪念人物ID',
    status              VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING待上传/ACTIVE已激活/FAILED未通过',
    document_content    TEXT COMMENT '用户上传的原始文档',
    persona_prompt     TEXT COMMENT '生成的人设Prompt',
    evaluation_score    INT COMMENT '评估得分',
    evaluation_detail   JSON COMMENT '评估详情JSON',
    created_by          BIGINT COMMENT '创建人',
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_memorial (memorial_id),
    INDEX idx_agent_memorial (memorial_id),
    CONSTRAINT fk_agent_memorial FOREIGN KEY (memorial_id) REFERENCES t_memorial(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体配置表';
```

### 4.4 Java实体类

```java
package com.family.myfamily.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_memorial_agent")
public class MemorialAgent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memorialId;           // memorial_id

    private String status;             // status

    private String documentContent;    // document_content

    private String personaPrompt;      // persona_prompt

    private Integer evaluationScore;    // evaluation_score

    private String evaluationDetail;   // evaluation_detail (JSON)

    private Long createdBy;           // created_by

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;   // created_at

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;   // updated_at

    // 状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_FAILED = "FAILED";
}
```

### 4.5 Mapper接口

```java
package com.family.myfamily.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.myfamily.entity.MemorialAgent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemorialAgentMapper extends BaseMapper<MemorialAgent> {

    MemorialAgent selectByMemorialId(Long memorialId);
}
```

### 4.6 前端TypeScript类型

> **注意**：前端类型使用 camelCase，与Java保持一致

```typescript
export interface MemorialAgent {
  id: number
  memorialId: number
  status: 'PENDING' | 'ACTIVE' | 'FAILED'
  documentContent?: string
  personaPrompt?: string
  evaluationScore?: number
  evaluationDetail?: EvaluationDetail
  createdAt?: string
  updatedAt?: string
}

export interface EvaluationDetail {
  details: {
    basicInfo: { score: number; complete: boolean; desc: string }
    overall: { score: number; complete: boolean; desc: string }
    appearance: { score: number; complete: boolean; desc: string }
    personality: { score: number; complete: boolean; desc: string }
    memories: { score: number; complete: boolean; desc: string }
  }
  missing: string[]
  suggestions: string[]
}

export interface ChatRequest {
  message: string
  sessionId: string
}

export interface ChatResponse {
  content: string
}
```

### 4.7 命名规范总结

| 层级 | 命名规范 | 示例 |
|------|---------|------|
| 数据库 | snake_case | memorial_id, evaluation_score |
| Java | camelCase | memorialId, evaluationScore |
| MyBatis Plus | 自动映射 | 无需配置 |
| TypeScript | camelCase | memorialId, evaluationScore |
| JSON | camelCase | memorialId, evaluationScore |

---

## 五、API 接口

> **路径设计说明**：智能体是纪念人物(memorial)的子资源，采用嵌套RESTful设计。
> 基础路径：`/api/v1/memorials/{memorialId}/agent`

| 方法 | 路径 | 说明 | 请求体/参数 |
|------|------|------|------------|
| GET | `/api/v1/memorials/{memorialId}/agent` | 获取智能体状态 | - |
| POST | `/api/v1/memorials/{memorialId}/agent` | 创建智能体配置 | - |
| POST | `/api/v1/memorials/{memorialId}/agent/document` | 上传文档 | multipart/form-data |
| POST | `/api/v1/memorials/{memorialId}/agent/evaluate` | 评估文档 | - |
| POST | `/api/v1/memorials/{memorialId}/agent/chat` | 对话 | `{message, sessionId}` |
| DELETE | `/api/v1/memorials/{memorialId}/agent` | 删除智能体 | - |

### 5.1 请求/响应示例

```json
// GET /api/v1/memorials/{memorialId}/agent
// 获取智能体状态
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "memorialId": 1,
    "status": "PENDING",   // PENDING | ACTIVE | FAILED
    "evaluationScore": null,
    "evaluationDetail": null,
    "createdAt": "2026-04-30T10:00:00"
  }
}

// POST /api/v1/memorials/{memorialId}/agent/chat
// 对话请求
{
  "message": "爷爷，我好想你",
  "sessionId": "sess_abc123"
}

// 对话响应
{
  "code": 200,
  "message": "success",
  "data": {
    "content": "傻孩子，爷爷也知道你想我..."
  }
}
```

### 5.2 前端API调用示例

```typescript
// api/agent.ts
import request from '@/utils/request'
import type { ApiResponse, MemorialAgent, ChatRequest, ChatResponse, EvaluationDetail } from '@/types/api'

// 获取智能体状态
export function getAgentStatus(memorialId: number) {
  return request.get<ApiResponse<MemorialAgent>>(`/memorials/${memorialId}/agent`)
}

// 创建智能体配置
export function createAgent(memorialId: number) {
  return request.post<ApiResponse<MemorialAgent>>(`/memorials/${memorialId}/agent`)
}

// 上传文档
export function uploadAgentDocument(memorialId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<MemorialAgent>>(`/memorials/${memorialId}/agent/document`, formData)
}

// 评估文档
export function evaluateDocument(memorialId: number) {
  return request.post<ApiResponse<{ score: number; passed: boolean; evaluationDetail: EvaluationDetail }>>(`/memorials/${memorialId}/agent/evaluate`)
}

// 对话
export function chat(memorialId: number, data: ChatRequest) {
  return request.post<ApiResponse<ChatResponse>>(`/memorials/${memorialId}/agent/chat`, data)
}

// 删除智能体
export function deleteAgent(memorialId: number) {
  return request.delete<ApiResponse<null>>(`/memorials/${memorialId}/agent`)
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
└── myfamily-agent/                    # 新增模块（与现有模块平级）
    ├── pom.xml
    └── src/main/java/com/family/myfamily/
        ├── controller/
        │   └── MemorialAgentController.java   # 注意：复用MemorialController或新建
        ├── service/
        │   ├── MemorialAgentService.java
        │   └── impl/
        │       ├── MemorialAgentServiceImpl.java
        │       ├── DocumentParserService.java
        │       ├── PersonaExtractionService.java
        │       └── ChatService.java
        ├── entity/
        │   └── MemorialAgent.java
        ├── mapper/
        │   └── MemorialAgentMapper.java
        ├── config/
        │   └── SpringAIConfig.java
        └── parser/
            ├── DocumentParser.java
            ├── YamlHeaderParser.java
            └── EvaluationResultParser.java
```

> **说明**：为了简化，可以将Agent相关接口直接添加到现有的 MemorialController 中，或者创建独立的 MemorialAgentController。

### 9.2 前端结构

```
frontend/src/
├── api/
│   ├── memorial.ts         # 已有
│   └── agent.ts           # 新增（智能体API）
├── views/memorial/
│   ├── MemorialHallView.vue         # 已有
│   ├── MemorialDetailView.vue       # 修改（增加智能体入口）
│   └── AgentChatView.vue           # 新增对话页
├── components/
│   └── agent/                      # 新增组件目录
│       ├── AgentCreateDialog.vue   # 创建向导
│       └── EvaluationResult.vue    # 评估结果
├── router/
│   └── index.ts                    # 修改（增加路由）
└── types/
    └── api.ts                     # 修改（增加类型）
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
