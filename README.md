# mall — 全栈电商系统

基于 **Spring Boot 3 + Vue 3** 的 B2C 电商平台，涵盖用户注册登录、商品浏览、购物车、下单、秒杀全流程。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2、MyBatis-Plus 3.5、MySQL 8.0、Redis 7、RabbitMQ 3、Elasticsearch 7 |
| 管理后台 | Vue 3.4、Element Plus 2.5、Pinia、ECharts、UnoCSS |
| 用户门户 | Vue 3.4、Element Plus 2.5、Axios |
| 基础设施 | Docker Compose、Nginx、JWT |

## 项目结构

```
mall/
├── mall-server/       # Spring Boot 后端（端口 8800）
├── mall-admin/        # Vue 3 管理后台（端口 3002）
├── mall-portal/       # Vue 3 用户门户（端口 3001）
├── deploy/            # Docker Compose + Nginx + 部署脚本
└── sql/               # 数据库初始化脚本
```

## 快速启动

```bash
# 1. 启动中间件
docker compose -f deploy/docker-compose.yml up -d

# 2. 启动后端
cd mall-server && ./mvnw spring-boot:run

# 3. 启动管理后台
cd mall-admin && npm install && npm run dev

# 4. 启动用户门户
cd mall-portal && npm install && npm run dev
```

| 服务 | 地址 |
|------|------|
| 后端 API | http://localhost:8800 |
| Swagger 文档 | http://localhost:8800/swagger-ui/index.html |
| 管理后台 | http://localhost:3002/admin/ |
| 用户门户 | http://localhost:3001/ |

## 默认账号

- 用户名：`admin`
- 密码：`123`

## 运行测试

```bash
# 后端测试
cd mall-server && ./mvnw test

# 前端 E2E 测试
cd mall-admin && npx playwright test
cd mall-portal && npx playwright test
```

## 部署

```bash
bash deploy/deploy.sh                 # 标准部署
bash deploy/deploy.sh --skip-tests    # 跳过测试
```
