# MyFamily Frontend

家族族谱管理系统前端项目

## 技术栈

- Vue.js 3.x (Composition API)
- TypeScript 5.x
- Vite 5.x
- Pinia 2.x (状态管理)
- Vue Router 4.x
- Element Plus (UI组件库)
- Axios (HTTP客户端)

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 类型检查
npm run type-check
```

## 项目结构

```
src/
├── api/              # API接口定义
├── components/       # 公共组件
│   ├── common/       # 通用组件
│   └── genealogy/    # 族谱相关组件
├── views/            # 页面视图
├── stores/           # Pinia状态管理
├── router/           # 路由配置
├── utils/            # 工具函数
├── types/            # TypeScript类型定义
├── assets/           # 静态资源
│   └── styles/       # 全局样式
├── App.vue
└── main.ts
```

## 设计规范

遵循水墨风格UI设计规范，详见 `docs/UI-myfamily-1.0.0-20260424.md`。
