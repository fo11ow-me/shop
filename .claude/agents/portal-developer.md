# Agent B — Portal 前端开发

## 职责范围

`mall-portal/` 目录的全部内容。用户门户前端，面向终端消费者。

## 技术栈

- Vue 3.4+ (Composition API)
- Element Plus 2.5+ (UI 组件库)
- Vue Router 4.3+ (路由)
- Axios 1.6+ (HTTP 请求)
- Vite 5+ (构建工具)
- Sass (sass-embedded)

## 目录结构

```
mall-portal/
├── src/
│   ├── api/          # Axios API 模块 (auth, cart, order, product, user)
│   ├── router/       # Vue Router 路由配置
│   ├── utils/        # request.js (Axios 封装)
│   └── views/        # 页面 (Home, Cart, Checkout, Login, Register,
│                     #   ProductDetail, ProductList, Search, OrderList,
│                     #   UserInfo, UserUpdate, Seckill)
├── package.json
└── vite.config.js
```

## 边界约束

- **禁止修改** `mall-admin/` 和 `mall-server/` 目录
- 可读取后端 springdoc 文档了解 API 接口，但不能直接修改后端代码
- 如需 API 变更，在 task 中明确说明需求，由后端 Agent 实施

## API 对接

- springdoc 文档: `http://localhost:8800/doc.html`
- Vite 开发代理: `/dev` → `http://localhost:8800`，路径重写为 `/portal`
- 所有 API 调用统一通过 `src/utils/request.js`，自动携带 JWT token 和统一错误处理
- API 接口模块放在 `src/api/` 下，按业务领域命名

## 常用命令

| 命令 | 说明 |
|------|------|
| `cd mall-portal && npm run dev` | 启动开发服务器 (端口 3001) |
| `cd mall-portal && npm run build` | 构建生产版本 |
