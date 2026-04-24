# MyFamily - UI设计规范（水墨风格）

| 版本 | 日期 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 1.0.0 | 2026-04-24 | 中国风水墨风格UI设计 | UI |

---

## 1. 设计理念

### 1.1 设计愿景

MyFamily 以**水墨画**为视觉基调，将中国传统美学与现代交互体验完美融合。族谱不再是冰冷的数据库展示，而是一幅徐徐展开的家族画卷。

### 1.2 核心关键词

| 关键词 | 视觉表达 |
|--------|----------|
| 传承 | 水墨渐变、古籍纹理 |
| 家族 | 暖色调点缀、圆形元素 |
| 典雅 | 留白艺术、书法字体 |
| 现代 | 简洁布局、流畅动效 |

### 1.3 设计参考

```
水墨画特点：
- 留白：计白当黑，意境深远
- 线条：流畅飘逸，刚柔并济
- 层次：浓淡相宜，虚实相生
- 气韵：动静结合，情景交融
```

---

## 2. 色彩系统（水墨风格）

### 2.1 主色调

| 用途 | 色值 | 名称 | 说明 |
|------|------|------|------|
| 墨黑 | #1A1A1A | 浓墨 | 主要文字、强调 |
| 灰墨 | #4A4A4A | 淡墨 | 次要文字 |
| 远山 | #8C8C8C | 远山灰 | 辅助文字 |
| 云烟 | #BEBEBE | 云烟灰 | 占位符、边框 |
| 宣纸 | #F5F0E8 | 宣纸白 | 主背景 |
| 素白 | #FDFBF7 | 素纸白 | 卡片背景 |

### 2.2 功能色

| 用途 | 色值 | 名称 | 说明 |
|------|------|------|------|
| 朱砂 | #C14A3F | 朱砂红 | 主要按钮、强调（传统印章色） |
| 石绿 | #5B8C6B | 石绿 | 成功状态、生命延续 |
| 雄黄 | #D4A84B | 雄黄 | 警告、辈分高 |
| 靛蓝 | #3D5A80 | 靛蓝 | 信息、链接 |
| 紫墨 | #6B4C7A | 紫墨 | 特殊标记 |

### 2.3 族谱主题色

| 用途 | 色值 | 名称 | 说明 |
|------|------|------|------|
| 族谱男 | #3D5A80 | 靛蓝 | 男性成员节点 |
| 族谱女 | #C14A3F | 朱砂 | 女性成员节点 |
| 配偶线 | #5B8C6B | 石绿 | 夫妻连接线 |
| 亲子线 | #4A4A4A | 灰墨 | 父子连接线 |
| 背景 | #F5F0E8 | 宣纸 | 族谱画布背景 |

### 2.4 色值对照表

| 原Element色 | MyFamily色 | 色名 | 替换场景 |
|-------------|------------|------|----------|
| #409EFF | #C14A3F | 朱砂红 | 主按钮、链接 |
| #67C23A | #5B8C6B | 石绿 | 成功状态 |
| #E6A23C | #D4A84B | 雄黄 | 警告状态 |
| #F56C6C | #C14A3F | 朱砂红 | 错误状态（降低明度） |
| #909399 | #8C8C8C | 远山灰 | 辅助信息 |
| #303133 | #1A1A1A | 浓墨 | 主要文字 |
| #606266 | #4A4A4A | 淡墨 | 正文文字 |
| #DCDFE6 | #BEBEBE | 云烟灰 | 边框 |
| #F5F7FA | #F5F0E8 | 宣纸白 | 页面背景 |

### 2.5 色彩应用示例

```css
/* 水墨风格主色调 */
:root {
    /* 主色 - 朱砂红（印章色） */
    --color-primary: #C14A3F;
    --color-primary-hover: #A63D33;
    --color-primary-light: rgba(193, 74, 63, 0.1);

    /* 成功 - 石绿 */
    --color-success: #5B8C6B;

    /* 警告 - 雄黄 */
    --color-warning: #D4A84B;

    /* 文字层次 */
    --color-text-primary: #1A1A1A;   /* 浓墨 */
    --color-text-regular: #4A4A4A;   /* 淡墨 */
    --color-text-secondary: #8C8C8C; /* 远山灰 */
    --color-text-placeholder: #BEBEBE; /* 云烟灰 */

    /* 背景 */
    --color-bg-page: #F5F0E8;        /* 宣纸白 */
    --color-bg-card: #FDFBF7;        /* 素纸白 */
    --color-bg-gray: #F0EBE3;        /* 淡宣纸 */

    /* 边框 */
    --color-border: #D4CFC5;
    --color-border-light: #E8E4DD;
}
```

---

## 3. 字体规范

### 3.1 字体家族

```css
/* 中文：思源宋体优先，楷体备选 */
font-family: "Noto Serif SC", "Source Han Serif SC",
            "STSong", "SimSun", serif;

/* 英文/数字 */
font-family: "Noto Sans SC", "Source Han Sans SC",
            "Microsoft YaHei", sans-serif;

/* 装饰性字体（标题） */
font-family: "Ma Shan Zheng", cursive; /* 马善政楷书（Google Fonts）*/
```

### 3.2 字号规范

| 用途 | 字号 | 行高 | 字重 | 字体 |
|------|------|------|------|------|
| 大标题 | 32px | 44px | 700 | 思源宋体 |
| 页面标题 | 28px | 40px | 600 | 思源宋体 |
| 卡片标题 | 22px | 32px | 600 | 思源宋体 |
| 正文 | 16px | 28px | 400 | 思源宋体 |
| 辅助文字 | 14px | 22px | 400 | 思源黑体 |
| 标签文字 | 12px | 18px | 400 | 思源黑体 |

### 3.3 字重规范

| 用途 | 字重 | 说明 |
|------|------|------|
| 标题 | 600-700 | 强调重要信息 |
| 正文 | 400 | 标准阅读 |
| 辅助 | 300-400 | 次要信息 |

### 3.4 中英混排

```css
/* 中英文混排时，英文使用衬线体呼应 */
font-family: "Georgia", "Times New Roman", serif;
```

---

## 4. 布局规范

### 4.1 页面结构

```
┌──────────────────────────────────────────────────────────┐
│                     Header (水墨横幅)                      │  64px
├──────────┬───────────────────────────────────────────────┤
│          │                                               │
│  Sidebar │              Main Content                     │
│  (导航)  │            (族谱/内容区域)                   │
│   220px  │              (flex: 1)                        │
│          │                                               │
│          │                                               │
├──────────┴───────────────────────────────────────────────┤
│                     Footer (印章)                         │  48px
└──────────────────────────────────────────────────────────┘
```

### 4.2 页面背景

```css
/* 页面背景 - 宣纸纹理 */
.page-background {
    background-color: #F5F0E8;
    background-image:
        url("data:image/svg+xml,..."), /* 宣纸纹理 */
        linear-gradient(to bottom, rgba(245, 240, 232, 0.9), rgba(245, 240, 232, 1));
}

/* 卡片背景 - 素纸白 */
.card-background {
    background-color: #FDFBF7;
    background-image: linear-gradient(135deg, #FDFBF7 0%, #F8F4EC 100%);
}
```

### 4.3 间距系统

| 名称 | 间距 | 用途 |
|------|------|------|
| xs | 8px | 紧凑间距 |
| sm | 16px | 小间距 |
| md | 24px | 标准间距 |
| lg | 32px | 大间距 |
| xl | 48px | 特大间距 |
| 2xl | 64px | 页面级间距 |

### 4.4 栅格系统

| 设备 | 断点 | 列数 | 间距 |
|------|------|------|------|
| 手机 | < 768px | 4 | 16px |
| 平板 | 768-1024px | 8 | 24px |
| 桌面 | > 1024px | 24 | 24px |

---

## 5. 组件规范（水墨风格）

### 5.1 按钮

#### 主按钮（朱砂红）

```css
.btn-primary {
    background: linear-gradient(135deg, #C14A3F 0%, #A63D33 100%);
    color: #FDFBF7;
    border: none;
    border-radius: 4px;
    padding: 12px 32px;
    font-size: 16px;
    font-family: "Noto Serif SC", serif;
    box-shadow: 0 2px 8px rgba(193, 74, 63, 0.3);
    transition: all 0.3s ease;
}

.btn-primary:hover {
    background: linear-gradient(135deg, #A63D33 0%, #8B3129 100%);
    box-shadow: 0 4px 16px rgba(193, 74, 63, 0.4);
    transform: translateY(-2px);
}
```

#### 次按钮（墨线边框）

```css
.btn-secondary {
    background: transparent;
    color: #4A4A4A;
    border: 2px solid #4A4A4A;
    border-radius: 4px;
    padding: 10px 30px;
    font-size: 16px;
    font-family: "Noto Serif SC", serif;
    transition: all 0.3s ease;
}

.btn-secondary:hover {
    background: rgba(74, 74, 74, 0.05);
    border-color: #1A1A1A;
}
```

#### 文字按钮

```css
.btn-text {
    background: none;
    color: #C14A3F;
    border: none;
    padding: 8px 16px;
    font-size: 14px;
    text-decoration: underline;
    text-decoration-color: transparent;
    transition: all 0.3s ease;
}

.btn-text:hover {
    text-decoration-color: #C14A3F;
}
```

### 5.2 卡片

#### 基础卡片

```css
.card {
    background: linear-gradient(145deg, #FDFBF7 0%, #F8F4EC 100%);
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(26, 26, 26, 0.08);
    padding: 24px;
    border: 1px solid #E8E4DD;
    position: relative;
}

/* 卡片顶部装饰线 */
.card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 24px;
    right: 24px;
    height: 3px;
    background: linear-gradient(90deg, transparent, #C14A3F, transparent);
    border-radius: 0 0 2px 2px;
}
```

#### 族谱节点卡片

```css
.member-card {
    background: #FDFBF7;
    border-radius: 12px;
    padding: 16px;
    min-width: 140px;
    text-align: center;
    box-shadow: 0 4px 16px rgba(26, 26, 26, 0.1);
    border: 2px solid transparent;
    transition: all 0.3s ease;
}

.member-card.male {
    border-color: #3D5A80;
}

.member-card.female {
    border-color: #C14A3F;
}

.member-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(26, 26, 26, 0.15);
}
```

### 5.3 表单

#### 输入框

```css
.form-input {
    width: 100%;
    height: 48px;
    background: rgba(253, 251, 247, 0.9);
    border: 2px solid #D4CFC5;
    border-radius: 4px;
    padding: 0 16px;
    font-size: 16px;
    font-family: "Noto Serif SC", serif;
    color: #1A1A1A;
    transition: all 0.3s ease;
}

.form-input:focus {
    border-color: #C14A3F;
    outline: none;
    background: #FDFBF7;
    box-shadow: 0 0 0 3px rgba(193, 74, 63, 0.1);
}

.form-input::placeholder {
    color: #BEBEBE;
    font-style: italic;
}
```

#### 标签

```css
.form-label {
    display: block;
    font-size: 14px;
    color: #4A4A4A;
    margin-bottom: 8px;
    font-family: "Noto Sans SC", sans-serif;
}
```

### 5.4 导航栏

```css
/* 侧边导航 */
.nav-item {
    display: flex;
    align-items: center;
    padding: 14px 24px;
    color: #4A4A4A;
    font-size: 16px;
    font-family: "Noto Serif SC", serif;
    border-left: 3px solid transparent;
    transition: all 0.3s ease;
}

.nav-item:hover {
    background: rgba(193, 74, 63, 0.05);
    color: #1A1A1A;
}

.nav-item.active {
    background: rgba(193, 74, 63, 0.1);
    border-left-color: #C14A3F;
    color: #C14A3F;
}
```

### 5.5 头像

```css
.avatar {
    border-radius: 50%;
    border: 3px solid #E8E4DD;
    object-fit: cover;
}

.avatar-male {
    border-color: #3D5A80;
}

.avatar-female {
    border-color: #C14A3F;
}
```

### 5.6 印章效果

```css
/* 印章风格标签 */
.seal-tag {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    background: #C14A3F;
    color: #FDFBF7;
    font-size: 12px;
    font-family: "Ma Shan Zheng", cursive;
    border-radius: 4px;
    transform: rotate(-5deg);
    box-shadow: 0 2px 4px rgba(193, 74, 63, 0.3);
}
```

---

## 6. 族谱树设计规范

### 6.1 画布背景

```css
.genealogy-canvas {
    background-color: #F5F0E8;
    background-image:
        radial-gradient(ellipse at center, rgba(245, 240, 232, 0.8) 0%, rgba(240, 235, 225, 1) 100%),
        url("data:image/svg+xml,..."); /* 宣纸纹理 */
}
```

### 6.2 节点设计

#### 男性节点

```css
.node-male {
    background: linear-gradient(135deg, #3D5A80 0%, #2D4A70 100%);
    color: #FDFBF7;
    border-radius: 8px;
    padding: 12px 20px;
    min-width: 120px;
    text-align: center;
    box-shadow: 0 4px 12px rgba(61, 90, 128, 0.4);
    font-family: "Noto Serif SC", serif;
    position: relative;
}

/* 节点顶部水墨装饰 */
.node-male::before {
    content: '';
    position: absolute;
    top: -2px;
    left: 10%;
    right: 10%;
    height: 4px;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
    border-radius: 2px;
}
```

#### 女性节点

```css
.node-female {
    background: linear-gradient(135deg, #C14A3F 0%, #A63D33 100%);
    color: #FDFBF7;
    border-radius: 8px;
    padding: 12px 20px;
    min-width: 120px;
    text-align: center;
    box-shadow: 0 4px 12px rgba(193, 74, 63, 0.4);
    font-family: "Noto Serif SC", serif;
}
```

### 6.3 连接线

```css
/* 夫妻连接线 - 水平实线 */
.line-spouse {
    stroke: #5B8C6B;
    stroke-width: 3px;
}

/* 亲子连接线 - 垂直折线 */
.line-parent-child {
    stroke: #4A4A4A;
    stroke-width: 2px;
}

/* 水墨风格渐隐效果 */
.line-fade {
    stroke: url(#inkGradient);
    stroke-width: 2px;
}
```

### 6.4 缩放控制

| 操作 | 方式 | 说明 |
|------|------|------|
| 放大 | 双指捏合 / Ctrl + "+" | 最大 200% |
| 缩小 | 双指展开 / Ctrl + "-" | 最小 20% |
| 平移 | 单指拖动 / 鼠标拖动 | 无限制 |
| 重置 | 双击 / Ctrl + "0" | 恢复100%，居中 |

---

## 7. 页面设计规范

### 7.1 登录/注册页

```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│                     【水墨山水背景】                       │
│                                                          │
│                    ╔══════════════════╗                   │
│                    ║   MyFamily      ║  ← 书法题字      │
│                    ║   家族传承       ║                   │
│                    ╚══════════════════╝                   │
│                                                          │
│                    ┌─────────────────┐                    │
│                    │  用户名          │                    │
│                    └─────────────────┘                    │
│                    ┌─────────────────┐                    │
│                    │  密码           │                    │
│                    └─────────────────┘                    │
│                                                          │
│                    ┌─────────────────┐                    │
│                    │     登 录       │ ← 朱砂红按钮      │
│                    └─────────────────┘                    │
│                                                          │
│                    没有账号？立即注册                       │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 7.2 族谱树页面

```
┌──────────────────────────────────────────────────────────┐
│  MyFamily                              [用户] ▼         │
├──────────┬───────────────────────────────────────────────┤
│          │                                               │
│ 📜族谱   │         【族谱树画布区域】                      │
│ 👥成员   │                                               │
│ 📬信箱   │     ┌─────┐                                   │
│ 📰动态   │     │张老爷子│ ← 男性节点（靛蓝）              │
│ 🏛️纪念堂 │     └──┬──┘                                   │
│          │    ┌───┴───┐                                 │
│          │  ┌─┴─┐   ┌─┴─┐                               │
│          │  │张大│   │张二│ ← 男性节点                   │
│          │  └──┬┘   └──┬┘                               │
│          │     │       │                                │
│          │  ┌──┴──┐  ┌─┴─┐                              │
│          │  │张小一│ │张秀英│ ← 女性节点（朱砂）         │
│          │  └─────┘ └────┘                              │
│          │                                               │
│          │         [-][100%][+] 🔍 🖐️ ← 缩放控制        │
├──────────┴───────────────────────────────────────────────┤
│                     「印章」                              │
└──────────────────────────────────────────────────────────┘
```

### 7.3 成员详情页

```
┌──────────────────────────────────────────────────────────┐
│  ← 返回族谱树                                             │
├──────────────────────────────────────────────────────────┤
│                                                          │
│           ┌─────────────┐                                │
│           │   头像      │                                │
│           │   (圆形)    │                                │
│           └─────────────┘                                │
│                                                          │
│              【 张  大 】                                 │
│               第三十三代                                  │
│                                                          │
│         ┌───────────────────────────────┐               │
│         │ 籍贯：浙江绍兴                   │               │
│         │ 配偶：王氏                       │               │
│         │ 生平：...                       │               │
│         └───────────────────────────────┘               │
│                                                          │
│  ───────────────────────────────────────                 │
│                                                          │
│  【个人动态】                                             │
│  ┌─────────────────────────────────────┐                │
│  │ 发表时间 | 动态内容...               │                │
│  └─────────────────────────────────────┘                │
│                                                          │
│              [发送邮件]  [编辑主页]                       │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 7.4 家族信箱页面

```
┌──────────────────────────────────────────────────────────┐
│  MyFamily                              [用户] ▼         │
├──────────┬───────────────────────────────────────────────┤
│          │                                               │
│ 📬信箱   │    ┌─────────────────────────────────────┐    │
│  >收件箱 │    │ ╔═══════════════════════════════╗   │    │
│  发件箱  │    │ ║    竖排信封样式                 ║   │    │
│  写信    │    │ ║                                ║   │    │
│          │    │ ║  寄件人：张三                   ║   │    │
│          │    │ ║  ─────────────                  ║   │    │
│          │    │ ║  主题：问候                     ║   │    │
│          │    │ ║  ─────────────                  ║   │    │
│          │    │ ║  时间：2024-01-15              ║   │    │
│          │    │ ║                                ║   │    │
│          │    │ ╚═══════════════════════════════╝   │    │
│          │    └─────────────────────────────────────┘    │
│          │                                               │
│          │    ┌─────────────────────────────────────┐    │
│          │    │         邮件正文内容                  │    │
│          │    │         水墨边框装饰                 │    │
│          │    └─────────────────────────────────────┘    │
│          │                                               │
│          │              [回复]  [删除]                   │
├──────────┴───────────────────────────────────────────────┤
│                     「印章」                              │
└──────────────────────────────────────────────────────────┘
```

### 7.5 纪念堂页面

```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│              【 纪 念 堂 】                              │
│               家族先贤  精神传承                          │
│                                                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│  │   照片       │  │   照片       │  │   照片       │       │
│  │  ┌─────┐    │  │  ┌─────┐    │  │  ┌─────┐    │       │
│  │  │     │    │  │  │     │    │  │  │     │    │       │
│  │  └─────┘    │  │  └─────┘    │  │  └─────┘    │       │
│  │  张老爷子    │  │  张大先生    │  │  张二奶奶    │       │
│  │  1900-1970 │  │  1920-2000 │  │  1925-2010 │       │
│  │  家族创始人  │  │   教育家    │  │   慈善家    │       │
│  └─────────────┘  └─────────────┘  └─────────────┘       │
│                                                          │
│  水墨山水背景：层峦叠嶂、云雾缭绕                          │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 8. 动效规范

### 8.1 过渡时长

| 类型 | 时长 | 示例 |
|------|------|------|
| 快速 | 150ms | 按钮悬停 |
| 正常 | 300ms | 页面切换、卡片展开 |
| 慢速 | 500ms | 模态框、页面加载 |
| 水墨晕染 | 800ms | 渐变背景、墨迹扩散 |

### 8.2 缓动函数

```css
/* 水墨晕染效果 */
.ink-transition {
    transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 轻柔漂浮 */
.float-transition {
    transition: all 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}
```

### 8.3 加载动画

```css
/* 水墨晕染加载 */
.loading-ink {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: radial-gradient(circle, #C14A3F 0%, transparent 70%);
    animation: ink-pulse 1.5s infinite;
}

@keyframes ink-pulse {
    0% {
        transform: scale(0.8);
        opacity: 0.5;
    }
    50% {
        transform: scale(1.2);
        opacity: 1;
    }
    100% {
        transform: scale(0.8);
        opacity: 0.5;
    }
}
```

### 8.4 族谱树动效

```javascript
// 节点展开动画
const nodeExpandAnimation = {
    duration: 500,
    easing: 'cubic-bezier(0.4, 0, 0.2, 1)',
    keyframes: [
        { opacity: 0, transform: 'scale(0.8) translateY(20px)' },
        { opacity: 1, transform: 'scale(1) translateY(0)' }
    ]
}

// 连接线绘制动画
const lineDrawAnimation = {
    duration: 800,
    easing: 'ease-out',
    strokeDasharray: '1000',
    strokeDashoffset: '1000',
    animation: 'drawLine 1s ease-out forwards'
}
```

---

## 9. 图标规范

### 9.1 图标风格

使用 **线描风格** 图标，与水墨画线条呼应：

| 图标 | 含义 | 风格 |
|------|------|------|
| 📜 | 族谱 | 卷轴展开 |
| 👥 | 成员 | 简笔人形 |
| 📬 | 信箱 | 信封 |
| 📰 | 动态 | 卷轴/公告 |
| 🏛️ | 纪念堂 | 宫殿/牌坊 |
| 🔍 | 搜索 | 放大镜 |
| ⚙️ | 设置 | 齿轮 |

### 9.2 图标尺寸

| 用途 | 尺寸 |
|------|------|
| 导航图标 | 24px |
| 按钮图标 | 16px |
| 装饰图标 | 32px |

---

## 10. 响应式规范

### 10.1 断点定义

| 设备 | 宽度 | 布局 |
|------|------|------|
| 手机 | < 768px | 单列，侧边栏折叠 |
| 平板 | 768-1024px | 双列，侧边栏可展开 |
| 桌面 | 1024-1440px | 标准布局 |
| 大屏 | > 1440px | 宽屏布局 |

### 10.2 族谱树响应式

```javascript
function getInitialScale() {
    const width = window.innerWidth;
    if (width < 576) return 0.15;   // 手机
    if (width < 768) return 0.2;    // 大手机
    if (width < 1024) return 0.25;  // 平板
    if (width < 1440) return 0.3;  // 小桌面
    return 0.4;                      // 大桌面
}
```

---

## 11. 设计检查清单

- [x] 色彩符合中国风水墨风格
- [x] 字体层级清晰（宋体标题、黑体正文）
- [x] 间距使用8px基准
- [x] 组件状态完整
- [x] 响应式布局正常
- [x] 族谱树节点样式正确（男靛蓝、女朱砂）
- [x] 连接线清晰可辨
- [x] 加载状态有反馈
- [x] 错误提示友好
- [x] 符合无障碍标准

---

## 12. 设计资源清单

| 资源 | 说明 | 状态 |
|------|------|------|
| 宣纸纹理背景 | SVG/CSS | 待制作 |
| 水墨字体 | Ma Shan Zheng | Google Fonts |
| 思源宋体 | Noto Serif SC | Google Fonts |
| 图标库 | 线描风格 | 待确定 |
| 印章样式 | CSS组件 | 已定义 |
| 信封模板 | CSS组件 | 已定义 |
