# mall — 全栈电商系统

<p align="center">
  <em>基于 Spring Boot 3 + Vue 3 的 B2C 电商平台</em>
</p>
<p align="center">
  <em>A full-stack B2C e-commerce platform built with Spring Boot 3 + Vue 3</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Vue-3.4-4FC08D" alt="Vue" />
  <img src="https://img.shields.io/badge/Java-17-orange" alt="Java 17" />
  <img src="https://img.shields.io/badge/MySQL-8.0-blue" alt="MySQL" />
  <img src="https://img.shields.io/badge/Redis-7-red" alt="Redis" />
  <img src="https://img.shields.io/badge/RabbitMQ-3-FF6600" alt="RabbitMQ" />
</p>

<p align="center">
  🌐 在线访问 / Online: <a href="http://mall.qiujie.net.cn/">http://mall.qiujie.net.cn/</a> (门户/Portal) ｜ <a href="http://mall.qiujie.net.cn/admin/">http://mall.qiujie.net.cn/admin/</a> (后台/Admin)
</p>

---

## 功能概览 / Features

- 用户注册登录 / User authentication (JWT + 图形验证码)
- 商品浏览搜索 / Product catalog with search
- 购物车管理 / Shopping cart
- 订单结算 / Order & checkout
- 秒杀活动 / Flash sale (Redis Lua + RabbitMQ 死信队列)
- 管理后台 / Admin dashboard (ECharts + 权限管理)
- OSS 图片存储 / Image storage with Alibaba Cloud OSS

---

## 技术栈 / Tech Stack

| 层级 / Layer | 技术 / Technology |
|-------------|-------------------|
| 后端框架 / Backend | Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 |
| 安全 / Security | Spring Security + JWT |
| 数据库 / Database | MySQL 8.0 + Redis 7 |
| 消息队列 / MQ | RabbitMQ 3 |
| 搜索引擎 / Search | Elasticsearch 7 |
| 管理后台 / Admin | Vue 3.4 + Element Plus 2.5 + Pinia + ECharts + UnoCSS |
| 用户门户 / Portal | Vue 3.4 + Element Plus 2.5 + Axios |

---

## 快速启动 / Quick Start

### 前置依赖 / Prerequisites

- **Java 17+** + Maven 3
- **Node.js 18+** + npm
- **Docker**（MySQL + Redis + RabbitMQ + Elasticsearch）

### 1. 启动中间件 / Start Middleware

```bash
docker compose -f docker-compose.yml up -d
```

### 2. 启动后端 / Start Server（端口 8800）

```bash
cd mall-server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 启动管理后台 / Start Admin（端口 3002）

```bash
cd mall-admin
npm install
npm run dev
```

### 4. 启动用户门户 / Start Portal（端口 3001）

```bash
cd mall-portal
npm install
npm run dev
```

| 服务 / Service | 地址 / URL |
|---------------|-----------|
| 后端 API | http://localhost:8800 |
| Swagger 文档 | http://localhost:8800/swagger-ui/index.html |
| 管理后台 / Admin | http://localhost:3002/admin/ |
| 用户门户 / Portal | http://localhost:3001/ |

---

## 模块架构 / Architecture

```
┌─────────────────────────────────────────────────┐
│                   Nginx (反向代理)                  │
├─────────────────────┬───────────────────────────┤
│   mall-portal       │      mall-admin            │
│   Vue 3 · :3001     │      Vue 3 · :3002         │
├─────────────────────┴───────────────────────────┤
│                 mall-server                       │
│            Spring Boot 3 · :8800                  │
├──────┬──────┬──────┬────────┬───────────────────┤
│ MySQL│ Redis│  MQ  │   ES   │       OSS         │
└──────┴──────┴──────┴────────┴───────────────────┘
```

| 模块 / Module | 说明 / Description | 端口 / Port |
|---------------|-------------------|-------------|
| **mall-server** | REST API 后端服务 | 8800 |
| **mall-admin** | 管理后台前端 | 3002 |
| **mall-portal** | 用户门户前端 | 3001 |
| MySQL | 数据库 | 3306 |
| Redis | 缓存 | 6379 |
| RabbitMQ | 消息队列 | 5672 / 15672 |
| Elasticsearch | 搜索引擎 | 9200 |

---

## 目录结构 / Directory Structure

```
mall/
├── mall-server/               # 后端 Spring Boot
│   ├── src/main/java/com/qiujie/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # 控制器 (admin/ portal/)
│   │   ├── service/           # 业务层
│   │   ├── mapper/            # 数据访问层
│   │   ├── entity/            # 实体类
│   │   ├── enums/             # 枚举类
│   │   ├── exception/         # 异常处理
│   │   └── util/              # 工具类
│   └── src/main/resources/
│       ├── application.yml        # 主配置
│       ├── application-dev.yml    # 开发环境
│       └── application-prod.yml   # 生产环境
├── mall-admin/                # 管理后台 Vue 3
│   └── src/
│       ├── api/               # API 模块
│       ├── router/            # 路由
│       ├── stores/            # Pinia 状态管理
│       ├── views/             # 页面
│       └── components/        # 组件
├── mall-portal/               # 用户门户 Vue 3
│   └── src/
│       ├── api/               # API 模块
│       ├── router/            # 路由
│       └── views/             # 页面
├── docker-compose.yml         # 本地开发中间件
├── deploy/                    # 部署配置
│   ├── docker-compose.server.yml  # 服务器应用 + 中间件
│   ├── deploy.sh                  # 部署脚本
│   ├── deploy-server.sh           # 服务器端部署脚本
│   ├── nginx/                     # Nginx 配置
│   └── docker-compose.server.env.example  # 环境变量模板
└── sql/                       # 数据库初始化
```

---

## 运行测试 / Tests

```bash
# 后端测试 / Backend tests (H2 内存数据库)
cd mall-server
./mvnw test

# 前端 E2E 测试 / Frontend E2E
cd mall-admin  # or mall-portal
npx playwright test
```

---

## 部署 / Deployment

```bash
bash deploy/deploy.sh                # 标准部署 / Standard
bash deploy/deploy.sh --skip-tests   # 跳过测试 / Skip tests
```

流程 / Flow：本地构建 / Build → 推送镜像 / Push → 服务器拉取 / Pull → 健康检查 / Health → 失败回滚 / Rollback

---

## 默认账号 / Default Account

| 角色 / Role | 用户名 / Username | 密码 / Password |
|-------------|-------------------|-----------------|
| 管理员 / Admin | `admin` | `123` |

---

## 环境变量 / Environment Variables

敏感信息通过 `.env` 文件管理，模板见 `deploy/docker-compose.server.env.example`。

| 变量 / Variable | 说明 / Description |
|----------------|-------------------|
| `DB_PASSWORD` | MySQL 密码 |
| `REDIS_PASSWORD` | Redis 密码 |
| `RABBITMQ_PASSWORD` | RabbitMQ 密码 |
| `OSS_ACCESS_KEY_ID` | 阿里云 OSS AccessKey |
| `OSS_ACCESS_KEY_SECRET` | 阿里云 OSS Secret |
