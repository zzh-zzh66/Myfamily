# 族谱树功能开发总结报告

**日期**: 2026-04-28
**版本**: v1.0.1

---

## 一、功能概述

本次开发实现了族谱树可视化面板的侧边详情框和完整的删除功能，包括：

1. 侧边详情面板 - 查看成员信息
2. 删除功能 - 管理员专属权限
3. 夫妻节点处理 - 离婚和踢出族谱
4. 权限控制 - 仅管理员可操作

---

## 二、核心功能实现

### 2.1 侧边详情面板 (MemberDetailPanel.vue)

**文件位置**: `frontend/src/components/genealogy/MemberDetailPanel.vue`

**功能**:
- 点击人物节点时，从屏幕右侧滑出详情面板
- 显示成员基本信息：姓名、性别、辈分、在世/已故状态、年龄、出生/死亡日期
- 显示亲属关系：父亲、母亲、配偶（可点击跳转）
- 显示子女列表（可点击跳转）
- 管理员可看到删除按钮

**关键属性**:
```typescript
const isAdmin = computed(() => userStore.userInfo?.role?.toUpperCase() === 'ADMIN')
const isAlive = computed(() => !member.value?.deathDate)
const age = computed(() => {
  if (!member.value?.birthDate) return '未知'
  const birth = new Date(member.value.birthDate)
  const end = member.value.deathDate ? new Date(member.value.deathDate) : new Date()
  return Math.max(0, end.getFullYear() - birth.getFullYear())
})
```

### 2.2 删除功能

#### 2.2.1 权限控制

**前端检查**:
- 只有管理员（role === 'ADMIN'）能看到删除按钮
- 非管理员访问添加页面会被重定向

**后端检查** (待完善):
- Controller 层需要添加角色验证注解

#### 2.2.2 删除类型

| 场景 | 处理方式 | 说明 |
|------|---------|------|
| 单身节点无子女 | 直接删除 | 标记 deleted=1 |
| 单身节点有子女 | 警告后删除 | 子女父母关系需手动清理 |
| 夫妻节点无子女 | 离婚 或 踢出 | 离婚移除一方，踢出删除双方 |
| 夫妻节点有子女 | 离婚 或 踢出 | 离婚保留子女关系，踢出标记为无名氏 |

#### 2.2.3 删除操作流程

```typescript
// 离婚操作 - 有子女
1. 调用 clearSpouseRelation 清除双方配偶关系
2. 调用 deleteMember 删除被移除的一方
3. 刷新族谱树

// 踢出族谱 - 有子女
1. 将双方 name 改为 "无名氏"
2. 清除双方配偶关系
3. 刷新族谱树

// 踢出族谱 - 无子女
1. 删除双方
2. 刷新族谱树
```

### 2.3 族谱树节点类型

#### 2.3.1 节点数据结构

```typescript
interface CoupleNode {
  id: string              // 格式: "maleId-spouseId" 或 "single-memberId"
  male?: Member           // 男性成员
  female?: Member         // 女性成员
  isCouple: boolean       // 是否夫妻节点
  generation: number      // 辈分
  children: string[]       // 子节点 ID 列表
}
```

#### 2.3.2 节点类型判断

| 条件 | 节点类型 | 显示 |
|------|---------|------|
| member.spouseId && spouse.generation === member.generation | 夫妻节点 | 男♥女 |
| member.spouseId && spouse.generation !== member.generation | 单身节点 | 显示 member.name |
| !member.spouseId | 单身节点 | 显示 member.name |

### 2.4 节点转换规则

#### 2.4.1 离婚转换

```
原始: A(男) ♥ B(女) + 子女
操作: 离婚移除 B
结果:
  - A 变为单身节点
  - B 被删除 (deleted=1)
  - 子女关系保持不变 (fatherId/motherId 不变)
```

#### 2.4.2 踢出转换

```
原始: A(男) ♥ B(女) + 子女
操作: 踢出族谱
结果:
  - A 变为 "无名氏" (isVirtual=true)
  - B 变为 "无名氏" (isVirtual=true)
  - 双方配偶关系清除
  - 子女关系保持不变
```

#### 2.4.3 删除单身节点

```
原始: C(单身) + 子女
操作: 删除 C
结果:
  - C 被删除 (deleted=1)
  - 子女 fatherId/motherId 需要手动清理
```

---

## 三、数据库设计

### 3.1 关键字段

| 字段 | 类型 | 说明 |
|------|------|------|
| spouse_id | BIGINT | 配偶 ID（外键） |
| spouse_name | VARCHAR | 配偶姓名 |
| is_virtual | TINYINT | 是否虚拟成员（无名氏） |
| deleted | TINYINT | 逻辑删除标记（@TableLogic） |
| father_id | BIGINT | 父亲 ID |
| mother_id | BIGINT | 母亲 ID |

### 3.2 迁移脚本

**文件**: `db/migration-v1.0.1.sql`

```sql
-- 添加配偶ID字段
ALTER TABLE t_member ADD COLUMN spouse_id BIGINT COMMENT '配偶ID' AFTER spouse_name;
ALTER TABLE t_member ADD INDEX idx_member_spouse (spouse_id);
ALTER TABLE t_member ADD CONSTRAINT fk_member_spouse FOREIGN KEY (spouse_id) REFERENCES t_member(id);

-- 添加虚拟成员标记字段
ALTER TABLE t_member ADD COLUMN is_virtual TINYINT DEFAULT 0 COMMENT '是否为虚拟成员（无名氏）';
```

---

## 四、后端关键代码

### 4.1 清除配偶关系

**文件**: `MemberService.java`

```java
@Transactional
public void clearSpouseRelation(Long memberId, Long userId) {
    Member member = memberMapper.selectById(memberId);
    if (member == null || member.getDeleted() == 1) {
        throw new BusinessException("成员不存在");
    }
    Long spouseId = member.getSpouseId();

    memberMapper.clearSpouseById(memberId, userId);

    if (spouseId != null) {
        memberMapper.clearSpouseById(spouseId, userId);
    }
}
```

### 4.2 删除成员

```java
@Transactional
public void deleteMember(Long id, Long userId) {
    Member member = memberMapper.selectById(id);
    if (member == null || member.getDeleted() == 1) {
        throw new BusinessException("成员不存在");
    }
    memberMapper.deleteById(id);  // 使用 @TableLogic 自动标记 deleted=1
}
```

### 4.3 Mapper SQL

```java
@Update("UPDATE t_member SET spouse_id = NULL, spouse_name = NULL, updated_by = #{userId} WHERE id = #{memberId}")
void clearSpouseById(@Param("memberId") Long memberId, @Param("userId") Long userId);

@Update("UPDATE t_member SET deleted = 1, updated_by = #{userId} WHERE id = #{memberId}")
void softDeleteById(@Param("memberId") Long memberId, @Param("userId") Long userId);
```

---

## 五、前端关键代码

### 5.1 权限检查

```typescript
// GenealogyTreeView.vue
const isAdmin = computed(() => userStore.userInfo?.role?.toUpperCase() === 'ADMIN')

// 添加成员按钮
<el-button v-if="isAdmin" type="primary" @click="handleAddMember">
  添加成员
</el-button>

// 删除按钮
<el-button v-if="isAdmin" type="danger" @click="handleShowDeleteOptions">
  删除成员
</el-button>
```

### 5.2 族谱树构建

```typescript
// genealogyTree.ts - transformToCoupleNodes
if (member.spouseId) {
  const spouse = memberMap.get(member.spouseId)
  if (spouse && spouse.generation === member.generation) {
    // 创建夫妻节点
    const coupleNode: CoupleNode = {
      id: `${member.id}-${spouse.id}`,
      male: normalizeGender(member.gender) === 'male' ? member : spouse,
      female: normalizeGender(spouse.gender) === 'female' ? spouse : member,
      isCouple: true,
      generation: member.generation,
      children: []
    }
  }
}
```

### 5.3 删除对话框逻辑

```typescript
// MemberDetailPanel.vue
const isCoupleNode = computed(() => !!member.value?.spouseId)

// 删除对话框
<template v-if="isCoupleNode && hasChildren">
  <el-button @click="handleDivorce">离婚（移除一方）</el-button>
  <el-button type="danger" @click="handleKickOut">踢出族谱</el-button>
</template>

<template v-else-if="isCoupleNode && !hasChildren">
  <el-button @click="handleDivorceNoChildren">移除该成员</el-button>
  <el-button type="danger" @click="handleKickOut">删除整个夫妻节点</el-button>
</template>

<template v-else>
  <el-button type="danger" @click="handleDeleteSingle">确认删除</el-button>
</template>
```

---

## 六、已知问题与注意事项

### 6.1 性别字段

添加成员时，配偶的性别必须正确设置。性别字段用于确定夫妻节点中谁是男方、谁是女方。

### 6.2 后端权限验证

当前后端 Controller 未添加角色验证注解，建议添加：
```java
@PreAuthorize("hasRole('ADMIN')")
```

### 6.3 子女关系清理

删除有子女的成员后，子女的 fatherId/motherId 需要手动清理或在业务逻辑中自动处理。

---

## 七、文件清单

### 前端
| 文件 | 说明 |
|------|------|
| `frontend/src/components/genealogy/MemberDetailPanel.vue` | 侧边详情面板 |
| `frontend/src/views/genealogy/GenealogyTreeView.vue` | 族谱树视图（集成详情面板） |
| `frontend/src/views/member/MemberAddView.vue` | 添加成员页面（权限控制） |
| `frontend/src/api/member.ts` | API 调用（添加 clearSpouseRelation） |

### 后端
| 文件 | 说明 |
|------|------|
| `MemberController.java` | 添加 DELETE /{id}/spouse 端点 |
| `MemberService.java` | 添加 clearSpouseRelation 方法 |
| `MemberMapper.java` | 添加 clearSpouseById、softDeleteById SQL |
| `Member.java` | 添加 isVirtual 字段 |
| `MemberDTO.java` | 添加 isVirtual 字段 |

### 数据库
| 文件 | 说明 |
|------|------|
| `db/migration-v1.0.1.sql` | 数据库迁移脚本 |

---

## 八、操作示例

### 8.1 离婚操作（有子女）

1. 选中夫妻节点 A ♥ B
2. 点击"删除成员"
3. 选择"离婚（移除一方，子女关系保持不变）"
4. 在弹出对话框中选择要移除的一方（A 或 B）
5. 确认后，选中方被删除，另一方变为单身节点
6. 子女关系保持不变

### 8.2 踢出族谱操作（有子女）

1. 选中夫妻节点 A ♥ B
2. 点击"删除成员"
3. 选择"踢出族谱（标记为无名氏）"
4. 确认后，双方都变为"无名氏"
5. 子女关系保持不变

### 8.3 删除单身节点

1. 选中单身节点 C
2. 点击"删除成员"
3. 确认删除
4. C 被标记为已删除

---

**报告生成时间**: 2026-04-28