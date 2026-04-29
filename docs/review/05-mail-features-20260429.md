# 邮件功能修复与完善总结

**日期**: 2026-04-29
**文档版本**: v1.0.0

---

## 一、问题修复

### 1. 前后端字段名不匹配
**问题描述**: 邮件列表和详情页无法正确显示数据，控制台报错字段不存在。

**根本原因**: 前端使用 `senderName`, `senderAvatar`, `senderId`，后端返回 `fromUserName`, `fromUserAvatar`, `fromUserId`。

**解决方案**:
- [api.ts](file:///e:/MyFamily/Myfamily/frontend/src/types/api.ts) - 修正 Mail 类型字段名
- [MailListView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailListView.vue) - `senderName` → `fromUserName`
- [MailDetailView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailDetailView.vue) - 修正所有不匹配的字段

---

### 2. isRead 类型判断错误
**问题描述**: 已读邮件仍然显示为未读状态。

**根本原因**: `isRead` 是 number 类型（0/1），前端使用 `!mail.isRead` 进行 boolean 判断。

**解决方案**:
- [MailListView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailListView.vue) - `!mail.isRead` → `mail.isRead === 0`

---

### 3. 保存草稿功能缺失
**问题描述**: 前端调用保存草稿接口，但后端未实现。

**根本原因**:
1. 后端缺少 `/mails/draft` 接口
2. Mail 实体缺少 `is_draft` 字段

**解决方案**:
- **数据库** - 添加 `is_draft` 字段
  ```sql
  ALTER TABLE t_mail ADD COLUMN is_draft TINYINT DEFAULT 0 COMMENT '是否草稿：0-否，1-是';
  ```
- [Mail.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-common/src/main/java/com/family/myfamily/entity/Mail.java) - 添加 `isDraft` 字段
- [MailDTO.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-common/src/main/java/com/family/myfamily/dto/MailDTO.java) - 添加 `isDraft` 字段
- [MailController.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-web/src/main/java/com/family/myfamily/controller/MailController.java) - 添加 `POST /mails/draft` 接口
- [MailService.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-common/src/main/java/com/family/myfamily/service/MailService.java) - 添加 `saveDraft()` 方法

---

### 4. 草稿箱显示收件箱内容
**问题描述**: 草稿箱和收件箱显示相同内容。

**根本原因**: `getMailList()` 方法的 drafts 文件夹查询条件不完整。

**解决方案**:
- [MailService.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-common/src/main/java/com/family/myfamily/service/MailService.java) - 添加 `isDraft = 1` 查询条件
  ```java
  } else if ("drafts".equals(folder)) {
      wrapper.eq(Mail::getFromUserId, userId)
              .eq(Mail::getIsDraft, 1)
              .eq(Mail::getIsDeleted, 0);
  }
  ```

---

### 5. 草稿箱邮件按钮显示错误
**问题描述**: 草稿箱中的邮件显示"回复"和"转发"按钮。

**根本原因**: 没有根据 `isDraft` 字段区分显示不同按钮。

**解决方案**:
- [MailDetailView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailDetailView.vue) - 根据 `isDraft` 显示不同按钮
  - 草稿显示：**发送** + **编辑**
  - 正式邮件显示：**回复** + **转发**

---

### 6. 发送草稿后未从草稿箱删除
**问题描述**: 草稿发送后仍然显示在草稿箱。

**根本原因**: `handleSend` 只发送了新邮件，未删除原草稿。

**解决方案**:
- [MailDetailView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailDetailView.vue) - 发送后调用 `deleteMail(draftId)`
- [MailComposeView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailComposeView.vue) - 编辑后发送也删除原草稿

---

### 7. 编辑草稿状态丢失
**问题描述**: 从草稿箱编辑草稿，收件人信息丢失。

**根本原因**: `handleEdit` 只传递了 `draftId`, `subject`, `content`，缺少 `to` 和 `attachments`。

**解决方案**:
- [MailDetailView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailDetailView.vue) - 传递完整参数
- [MailComposeView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailComposeView.vue) - 正确读取所有 query 参数

---

### 8. 删除草稿权限校验错误
**问题描述**: 删除草稿提示"无权删除此邮件"。

**根本原因**: 删除草稿时检查 `toMemberId`，但草稿的 `toMemberId` 可能为空。

**解决方案**:
- [MailService.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-common/src/main/java/com/family/myfamily/service/MailService.java) - 优先检查 `isDraft` 标志
  ```java
  if (mail.getIsDraft() != null && mail.getIsDraft() == 1) {
      if (!mail.getFromUserId().equals(userId)) {
          throw new BusinessException("无权删除此邮件");
      }
      mail.setIsDeleted(1);
  }
  ```

---

### 9. 前端动态导入失败
**问题描述**: 打开邮件详情页报错 "Failed to fetch dynamically imported module"。

**根本原因**: MailDetailView.vue 有重复的 import 语句。

**解决方案**:
- [MailDetailView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailDetailView.vue) - 删除重复的 `import { ElMessage, ElMessageBox }`

---

### 10. 草稿发送附件丢失
**问题描述**: 通过草稿箱发送邮件，附件图片丢失。

**根本原因**: MailDetailView 的 `handleSend` 没有传递 `attachments` 字段。

**解决方案**:
- [MailDetailView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailDetailView.vue) - 添加 `attachments: mail.value.attachments || ''`

---

## 二、新增功能

### 1. 邮件附件功能
**功能描述**: 邮件可以添加图片附件，仅限照片形式。

**实现内容**:

- **数据库**:
  ```sql
  ALTER TABLE t_mail ADD COLUMN attachments TEXT COMMENT '附件图片URL列表，逗号分隔';
  ```

- **后端**:
  - [Mail.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-common/src/main/java/com/family/myfamily/entity/Mail.java) - 添加 `attachments` 字段
  - [MailDTO.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-common/src/main/java/com/family/myfamily/dto/MailDTO.java) - 添加 `attachments` 字段
  - [FileUploadController.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-web/src/main/java/com/family/myfamily/controller/FileUploadController.java) - 复用图片上传接口

- **前端**:
  - [api.ts](file:///e:/MyFamily/Myfamily/frontend/src/api/mail.ts) - 添加 `uploadImage` 接口
  - [MailComposeView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailComposeView.vue) - 添加图片上传组件
  - [MailDetailView.vue](file:///e:/MyFamily/Myfamily/frontend/src/views/mail/MailDetailView.vue) - 显示附件图片

**限制规则**:
| 项目 | 限制 |
|------|------|
| 图片数量 | 最多 5 张 |
| 单张大小 | 最大 5MB |
| 支持格式 | JPG、PNG、GIF、WebP |

---

### 2. 头像功能
**功能描述**: 用户和成员添加头像字段，用于邮件显示发件人/收件人头像。

**实现内容**:

- **数据库**:
  ```sql
  ALTER TABLE t_user ADD COLUMN avatar VARCHAR(500) DEFAULT NULL;
  ALTER TABLE t_member ADD COLUMN avatar VARCHAR(500) DEFAULT NULL;
  ```

- **后端**:
  - [User.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-common/src/main/java/com/family/myfamily/entity/User.java) - 添加 `avatar` 字段
  - [Member.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-common/src/main/java/com/family/myfamily/entity/Member.java) - 添加 `avatar` 字段
  - [MailService.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-common/src/main/java/com/family/myfamily/service/MailService.java) - `convertToDTO()` 填充头像

---

### 3. 未读邮件数返回格式优化
**问题描述**: 前端期望 `{ inbox: number, total: number }`，后端返回 `number`。

**解决方案**:
- [MailController.java](file:///e:/MyFamily/Myfamily/backend/myfamily-parent/myfamily-web/src/main/java/com/family/myfamily/controller/MailController.java) - 返回 Map 格式
  ```java
  Map<String, Long> result = new HashMap<>();
  result.put("inbox", count);
  result.put("total", count);
  return Result.success(result);
  ```

---

## 三、数据库变更

### 迁移脚本
[migration-v1.0.3.sql](file:///e:/MyFamily/Myfamily/db/migration-v1.0.3.sql)

```sql
-- 添加用户头像
ALTER TABLE t_user ADD COLUMN avatar VARCHAR(500) DEFAULT NULL;

-- 添加成员头像
ALTER TABLE t_member ADD COLUMN avatar VARCHAR(500) DEFAULT NULL;

-- 添加草稿标志
ALTER TABLE t_mail ADD COLUMN is_draft TINYINT DEFAULT 0;

-- 添加附件字段
ALTER TABLE t_mail ADD COLUMN attachments TEXT;
```

---

## 四、API接口汇总

### 邮件相关
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/v1/mails` | GET | 获取邮件列表 |
| `/api/v1/mails/{id}` | GET | 获取邮件详情 |
| `/api/v1/mails/send` | POST | 发送邮件 |
| `/api/v1/mails/draft` | POST | 保存草稿 |
| `/api/v1/mails/{id}` | DELETE | 删除邮件 |
| `/api/v1/mails/unread-count` | GET | 获取未读数 |
| `/api/v1/mails/search-receivers` | GET | 搜索收件人 |
| `/api/v1/posts/upload-image` | POST | 上传图片（复用） |

---

## 五、文件变更清单

### 后端 (Java)
```
修改:
- myfamily-common/src/main/java/com/family/myfamily/entity/Mail.java
- myfamily-common/src/main/java/com/family/myfamily/entity/User.java
- myfamily-common/src/main/java/com/family/myfamily/entity/Member.java
- myfamily-common/src/main/java/com/family/myfamily/dto/MailDTO.java
- myfamily-common/src/main/java/com/family/myfamily/service/MailService.java
- myfamily-web/src/main/java/com/family/myfamily/controller/MailController.java
```

### 前端 (Vue/TS)
```
修改:
- src/types/api.ts
- src/api/mail.ts
- src/views/mail/MailListView.vue
- src/views/mail/MailDetailView.vue
- src/views/mail/MailComposeView.vue
```

### 数据库
```
迁移脚本:
- db/migration-v1.0.3.sql
```

---

## 六、待优化项

1. **草稿自动保存** - 定时自动保存草稿，防止意外丢失
2. **附件下载** - 支持下载邮件附件
3. **草稿编辑时不上传新附件** - 如果没有新增附件，不重复上传
4. **图片压缩** - 上传前压缩图片，减少存储和加载时间
