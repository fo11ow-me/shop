# Agent C — 后端服务开发

## 职责范围

`mall-server/` 目录的全部内容。Spring Boot 后端服务，提供 REST API。

## 技术栈

- Java 17
- Spring Boot 3.2.5
- MyBatis-Plus 3.5.5 (BaseMapper + XML 混合模式)
- MySQL 8.0 (Druid 连接池)
- Redis (Lettuce 连接池)
- Spring Security 6 + JWT (jjwt 0.12.5)
- springdoc-openapi 2.3.0 (API 文档)
- Hutool 5.8.25, Fastjson2, Apache POI 5.2.5
- Lombok

## 分层架构

```
mall-server/src/main/java/com/qiujie/
├── controller/
│   ├── admin/    # 管理端 API (Login, User, Role, Menu, Product, Category, Order, Home)
│   └── portal/   # 门户端 API (Auth, Cart, Order, Product)
├── service/      # 业务逻辑层
├── mapper/       # MyBatis-Plus BaseMapper 接口
├── entity/       # 数据库实体 (User, Role, Menu, Product, Cart, Order, Category…)
├── dto/          # 数据传输对象 (Response, ResponseDTO)
├── vo/           # 视图对象
├── enums/        # 枚举 (BaseEnum, BusinessStatusEnum, GenderEnum)
├── config/       # 配置类 (SecurityConfig, RedisConfig, MybatisPlusConfig, SwaggerConfig)
├── filter/       # JwtAuthenticationFilter
├── handler/      # 安全异常处理器
├── exception/    # 全局异常处理 (BaseExceptionHandler, ServiceException)
├── util/         # 工具类 (JwtUtil, RedisUtil, ValidateCodeUtil…)
└── annotation/   # 自定义注解 (@ExcelColumn)
```

## 边界约束

- **禁止修改** `mall-admin/` 和 `mall-portal/` 目录
- 接口变更必须同步更新 controller 上的 springdoc 注解（@Operation、@Schema 等）
- 确保 springdoc JSON 文档对前端 Agent 可访问
- 数据库 schema 变更需同步更新 `docs/mall.sql`

## 关键配置

| 配置项 | 文件 | 说明 |
|--------|------|------|
| 服务端口 | `application.yml` | 8800 |
| 数据库 | `application-dev.yml` | MySQL mall, root/123456 |
| Redis | `application-dev.yml` | localhost:6379, 密码 123456 |
| 文件路径 | `application.yml` | `D:/project/idea/mall/file/` |
| 安全 | `SecurityConfig.java` | JWT 无状态认证，路径权限控制 |
| 逻辑删除 | `application.yml` | deleted 字段标记 |

## 常用命令

| 命令 | 说明 |
|------|------|
| `cd mall-server && mvn spring-boot:run` | 启动后端服务 |
| `cd mall-server && mvn test` | 运行测试 |
| `cd mall-server && mvn clean package -DskipTests` | 构建 JAR |
