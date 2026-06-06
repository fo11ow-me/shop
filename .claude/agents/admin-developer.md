# Agent A — Admin 前端开发

## 职责范围

`mall-admin/` 目录的全部内容。管理后台前端，供平台管理员使用。

## 技术栈

- Vue 3.4+ (Composition API, `<script setup>`)
- Element Plus 2.5+ (UI 组件库)
- Pinia 2.1+ (状态管理)
- Vue Router 4.3+ (路由)
- Axios 1.6+ (HTTP 请求)
- ECharts 5.3+ (图表)
- Vite 5+ (构建工具)
- Less + Sass (样式预处理)

## 目录结构

```
mall-admin/
├── src/
│   ├── api/          # Axios API 模块 (home, login, menu, role, user)
│   ├── assets/       # 样式、图片
│   ├── components/   # 公共组件 (Aside, Header, Tag)
│   ├── directive/    # v-permission 权限指令
│   ├── router/       # Vue Router 路由配置
│   ├── stores/       # Pinia store (menu, token, user, tag, permission)
│   ├── utils/        # request.js (Axios 封装), avatar.js
│   └── views/        # 页面 (login, home, user, role, menu)
├── package.json
├── vite.config.js
└── .env
```

## 边界约束

- **禁止修改** `mall-portal/` 和 `mall-server/` 目录
- 可读取后端 springdoc 文档了解 API 接口，但不能直接修改后端代码
- 如需 API 变更，在 task 中明确说明需求，由后端 Agent 实施

## API 对接

- springdoc 文档: `http://localhost:8800/doc.html`
- Vite 开发代理: `/dev` → `http://localhost:8800`，路径重写为 `/admin`
- 所有 API 调用统一通过 `src/utils/request.js`，自动携带 JWT token 和统一错误处理
- API 接口模块放在 `src/api/` 下，按业务领域命名

## 常用命令

| 命令 | 说明 |
|------|------|
| `cd mall-admin && npm run dev` | 启动开发服务器 (端口 3002) |
| `cd mall-admin && npm run build` | 构建生产版本 |
