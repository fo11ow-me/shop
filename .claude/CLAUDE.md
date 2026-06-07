# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# mall — 全栈电商系统

基于 Spring Boot 3 + Vue 3 的 B2C 电商平台，涵盖用户注册登录、商品浏览、购物车、下单、支付全流程。

## 开发环境依赖

- **Java 17+** + Maven 3
- **Node.js 18+** + npm
- **Docker** (MySQL 8.0 + Redis 7 + RabbitMQ + Elasticsearch 通过 docker-compose 提供)

### 端口占用

| 服务 | host 端口 | 容器端口 |
|------|----------|----------|
| MySQL 8.0 | 3306 | 3306 |
| Redis 7 | 6379 | 6379 |
| RabbitMQ 3 | 5672, 15672 | 5672, 15672 |
| Elasticsearch 7 | 9200, 9300 | 9200, 9300 |
| mall-server | 8800 | — |
| mall-portal | 3001 | — |
| mall-admin | 3002 | — |

## 快速启动

```bash
# 1. 启动中间件（首次启动自动创建数据库并导入 docs/mall.sql）
docker compose up -d

# 2. 启动后端 (端口 8800)
cd mall-server
./mvnw spring-boot:run

# 3. 启动管理后台 (端口 3002)
cd mall-admin
npm install
npm run dev

# 4. 启动用户门户 (端口 3001)
cd mall-portal
npm install
npm run dev
```

## 模块架构

| 模块 | 说明 | 技术栈 | 端口 |
|------|------|--------|------|
| **mall-server** | 后端服务，REST API | Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + MySQL + Redis + JWT + Actuator | 8800 |
| **mall-admin** | 管理后台前端 | Vue 3.4 + Element Plus 2.5 + Pinia + ECharts + UnoCSS | 3002 |
| **mall-portal** | 用户门户前端 | Vue 3.4 + Element Plus 2.5 + Axios | 3001 |

## 运行测试

### 后端测试

```bash
cd mall-server

# 运行所有测试（单元 + 集成 + API 层，使用 H2 内存数据库）
./mvnw test

# 运行特定测试类
./mvnw test -Dtest="AuthApiTest"

# 运行特定测试分组（unit / integration / api）
./mvnw test -Dtest="*ApiTest"           # API 契约测试
./mvnw test -Dtest="*MapperTest"        # 集成测试（H2 内存数据库）
./mvnw test -Dtest="*ServiceImplTest"   # Service 单元测试

# 测试覆盖率报告（JaCoCo）
# 报告输出: target/site/jacoco/index.html
```

测试目录分层:
- `src/test/java/com/qiujie/unit/` — 纯单元测试（工具类：JWT, Redis, BCrypt, 验证码）
- `src/test/java/com/qiujie/integration/` — Mapper 集成测试（使用 H2 内存数据库）
- `src/test/java/com/qiujie/service/` — Service 层测试（Mock 依赖）
- `src/test/java/com/qiujie/api/` — API 契约测试 + Security 测试（全 Spring 上下文 + MockMvc）

> 后端测试使用 H2 内存数据库，不依赖本地 MySQL。Redis 部分在 CI 中由 service container 提供，本地需 Docker 运行 Redis。

### 前端测试（Playwright E2E）

```bash
cd mall-admin  # or mall-portal
npx playwright test                  # 运行所有 E2E 测试
npx playwright test --headed         # 可视化运行
npx playwright test --ui             # 交互式 UI 模式
npx playwright test --debug          # 逐步调试
npx playwright test --project=chromium  # 指定浏览器
```

E2E 测试文件: `mall-admin/e2e/admin-workflow.spec.cjs`, `mall-portal/e2e/purchase-flow.spec.cjs`, `mall-portal/e2e/error-handling.spec.cjs`

## 关键路径

### 后端 (mall-server/)
- 主配置: `mall-server/src/main/resources/application.yml`
- 开发配置: `mall-server/src/main/resources/application-dev.yml`（H2 + Redis localhost）
- 生产配置: `mall-server/src/main/resources/application-prod.yml`（MySQL + Redis Docker 服务名）
- 安全配置: `mall-server/src/main/java/com/qiujie/config/SecurityConfig.java`
- 管理端 API: `controller/admin/`
- 门户端 API: `controller/portal/`
- 业务层: `service/`
- 数据层: `mapper/`（MyBatis-Plus BaseMapper + XML 映射: `src/main/resources/mapper/*.xml`）
- 实体: `entity/`
- 枚举: `enums/`（MyBatis-Plus 自动映射）
- 工具: `util/`（JwtUtil, RedisUtil, ValidateCodeUtil…）
- 健康检查: `http://localhost:8800/actuator/health`

### 管理后台 (mall-admin/)
- Vite 配置: `vite.config.mjs`（端口 3002, 代理 `/dev` → `/admin`, UnoCSS + SCSS）
- 路由: `src/router/index.js`（history 模式）
- 状态管理: `src/stores/`（menu, token, user, tag, permission）
- API 模块: `src/api/`
- 页面: `src/views/`
- 组件: `src/components/`（Aside, Header, Tag）
- HTTP 工具: `src/utils/request.js`
- 权限指令: `src/directive/permission.js`

### 用户门户 (mall-portal/)
- Vite 配置: `vite.config.js`（端口 3001, 代理 `/dev` → `/portal`）
- 路由: `src/router/index.js`（history 模式）
- API 模块: `src/api/`（auth, cart, order, product, user）
- 页面: `src/views/`
- HTTP 工具: `src/utils/request.js`

## 敏感信息管理

所有密码、密钥、Token 通过 **环境变量** 管理，严禁硬编码。

- **Spring Boot**：使用 `${ENV_VAR:default}` 语法引用，本地开发有合理的默认值
- **Docker Compose（本地）**：`${ENV_VAR:-default}` 语法，默认值为 `123456` 方便本地开发
- **Docker Compose（服务器）**：`${ENV_VAR}` 无默认值，强制通过 `.env` 文件提供
- **服务器 `.env`**：位于 `/opt/app/mall/.env`，权限 `chmod 600`
- **本地 `.env`**：位于项目根目录，不提交

环境变量模板: `docker-compose.server.env.example`

## 部署

### 流程

```bash
bash deploy.sh                 # 标准部署
bash deploy.sh --skip-tests    # 跳过测试（紧急热修复）
```

脚本自动完成：后端测试 → 打包 JAR → 前端构建 → Docker 构建 → 推送到服务器 Registry → SSH 触发服务器部署。

### Docker 镜像

- 镜像仓库：服务器自建 Registry（`<服务器IP>:5000`）
- 镜像: `localhost:5000/mall/mall-server:latest` / `mall-nginx:latest`
- `mall-server/Dockerfile` — 基于 `eclipse-temurin:17-jre-jammy`，通过 `SPRING_PROFILES_ACTIVE` 环境变量控制 profile
- `Dockerfile.nginx` — 基于 `nginx:alpine`，将两个前端 dist + `nginx.conf` 打包

### 服务器部署

部署路径: `/opt/app/mall/`，使用 `docker-compose.server.yml`（包含 mall-server、mall-nginx、mall-registry 容器，与中间件共享 `my_network` 外部网络）。

### 回滚

```bash
ssh <服务器> bash /opt/app/mall/deploy-server.sh rollback          # 回滚到上一版本
ssh <服务器> bash /opt/app/mall/deploy-server.sh rollback abc1234  # 回滚到指定 SHA
```

## 数据库

- **MySQL 数据库名**: `mall`（开发环境 `127.0.0.1:3306`, 服务器 `mall-mysql:3306`）
- **初始化 SQL**: `docs/mall.sql`（表结构 + 种子数据）
- **关闭服务**: 使用 `bash docker-down.sh`，自动 `mysqldump` 导出数据库后停止容器。直接 `docker compose down` 会丢弃数据变更
- MyBatis-Plus 配置: 驼峰映射、逻辑删除 (logic-delete-value=1)、枚举自动映射
- Druid 连接池: 初始化 5, 最小空闲 5, 最大活跃 30

## API 契约

- springdoc-openapi 文档: `http://localhost:8800/swagger-ui/index.html`
- 管理端代理: Vite 将 `/dev` 代理至 `http://localhost:8800`，路径重写为 `/admin`
- 门户端代理: Vite 将 `/dev` 代理至 `http://localhost:8800`，路径重写为 `/portal`
- 生产环境: nginx 反向代理 `/dev/` → `mall-server:8800/portal/`, `/admin-api/` → `mall-server:8800/admin/`
- 接口变更时后端必须同步更新 springdoc 注解

## Agent 协作规范

本项目采用 Agent Teams 模式，角色定义见 `.claude/agents/` 目录：

| Agent | 职责范围 | 角色文件 |
|-------|---------|----------|
| Agent T | 全系统测试 | `.claude/agents/test-engineer.md` |
| Agent A | mall-admin/ | `.claude/agents/admin-developer.md` |
| Agent B | mall-portal/ | `.claude/agents/portal-developer.md` |
| Agent C | mall-server/ | `.claude/agents/server-developer.md` |

### 核心协作流程: 测→修→验 循环

```
Agent T 执行测试 → 发现 Bug → 记录 docs/bugs.md → 指派开发 Agent
→ 开发 Agent 立即修复 → Agent T 回归验证 → 继续测试
```

- 每个开发 Agent 只修改自己负责的模块目录，禁止跨模块修改
- Agent T 只读代码、定位问题、记录 Bug，不修改业务代码
- 前后端接口变更：后端 Agent 更新 API → 同步 springdoc 注解 → 前端 Agent 按文档调整
- 并行开发时使用 `superpowers:dispatching-parallel-agents` 技能分发任务
- 协作流程详见 `.claude/agents/README.md`
