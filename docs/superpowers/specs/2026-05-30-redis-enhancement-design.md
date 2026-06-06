# Redis 功能增强 — 设计方案

**日期**: 2026-05-30
**范围**: mall-server（后端）、mall-admin（管理端前端）、mall-portal（门户前端）

---

## 概述

为 mall 项目补充 Redis 相关能力，涵盖 5 个子功能：验证码键隔离、JWT 黑名单、秒杀后端、商品分类缓存、接口限流。

---

## 1. 验证码键隔离

### 问题

所有用户共用一个 `validate:code` 键，多用户同时登录会互相覆盖验证码。

### 方案

```
验证码获取 → 生成 uuid → Redis 键 validate:code:{uuid}，TTL 60s → 返回 uuid 给前端
登录提交  → 前端同时传 uuid + 验证码 → 后端用 uuid 查 Redis 比对
```

### 改动范围

| 文件 | 改动 |
|------|------|
| `AuthService.java` | `getVerificationCode` 返回 `String`（uuid） |
| `AuthServiceImpl.java` | 键改为 `validate:code:{uuid}`，login 接收 uuid 参数 |
| `portal/AuthController.java` | 返回 uuid 给前端 |
| `admin/AuthController.java` | 返回 uuid 给前端 |
| portal `Login.vue` | 获取验证码时存储 uuid，登录时一并提交 |
| admin `Login.vue` | 同上 |
| portal `api/auth.js` | getVerificationCode 返回 uuid |
| admin `api/auth.js` | 同上 |

### 测试

- `AuthApiTest` 和 `AuthServiceImplTest` 中验证码准备需更新为新键格式

---

## 2. JWT 黑名单

### 问题

JWT 完全无状态，用户退出登录或管理员禁用账号后令牌仍然有效。

### 方案

```
JWT 签发   → payload 增加 jti（UUID 唯一标识）
JWT 过滤器  → 解析 token 得到 jti → 检查 token:blacklist:{jti} 是否在 Redis 中
退出登录   → redisUtil.set("token:blacklist:" + jti, "1", 剩余有效期秒数)
管理员禁用 → 用户关联 token 已通过退出加入黑名单，无需额外处理
```

### 改动范围

| 文件 | 改动 |
|------|------|
| `JwtUtil.java` | `generateToken()` 加 jti，新增 `extractJti()`、`extractRemainingSeconds()` |
| `JwtAuthenticationFilter.java` | 注入 `RedisUtil`，校验前查黑名单 |
| `AuthService.java` | login 返回值改为包含 jti |
| `AuthServiceImpl.java` | login 返回 jti |
| `portal/AuthController.java` | 新增 `POST /portal/auth/logout` |
| `admin/AuthController.java` | 新增 `POST /admin/auth/logout` |
| `SecurityConfig.java` | 放行 `/portal/auth/logout`、`/admin/auth/logout` |

### 测试

- 新增测试：退出后 token 不可用、黑名单过期后可恢复

---

## 3. 秒杀后端

### 数据库

```sql
CREATE TABLE sms_seckill_session (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    seckill_price DECIMAL(10,2) NOT NULL,
    seckill_stock INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);
```

### Redis 键设计

| 键 | 用途 |
|---|------|
| `seckill:stock:{sessionId}` | 库存计数，预热时加载 |
| `seckill:order:{sessionId}:{userId}` | 用户抢购标记，防重复下单 |
| `seckill:result:{sessionId}:{userId}` | 抢购结果（JSON），供前端轮询 |

### 核心流程

1. **预热**：场次开始前将 `seckill_stock` 加载到 Redis
2. **抢购**：校验时间 → 防重复检查 → DECR 扣库存 → 标记用户已抢 → 异步下单 → 写入结果
3. **轮询**：前端每 2s 查询 `seckill:result:{sessionId}:{userId}`，最多 30 次

### API（与前端已有调用一致）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/portal/seckill/sessions` | 进行中的场次列表 |
| GET | `/portal/seckill/sessions/upcoming` | 即将开始的场次列表 |
| POST | `/portal/seckill/execute?sessionId=` | 执行秒杀 |
| GET | `/portal/seckill/result/{sessionId}` | 轮询秒杀结果 |

### 新增文件

| 文件 | 说明 |
|------|------|
| `entity/SeckillSession.java` | 实体类 |
| `mapper/SeckillSessionMapper.java` | Mapper 接口 + XML |
| `service/SeckillService.java` + `impl/` | 业务逻辑 |
| `controller/portal/SeckillController.java` | 控制器 |
| `enums/SeckillResultEnum.java` | 结果枚举（排队中/成功/失败） |

### 测试

- Service 单元测试（Mock Redis）
- API 契约测试（MockMvc，预置 Redis 状态）

---

## 4. 商品/分类缓存

### 方案

使用已封装的 `CacheClient`（含缓存穿透/击穿保护），对高频只读接口添加缓存。

| 方法 | 缓存键 | TTL | 失效策略 |
|------|--------|-----|----------|
| `ProductServiceImpl.detail(id)` | `cache:product:{id}` | 30 min | 商品编辑/删除后删除 |
| `ProductServiceImpl.categories()` | `cache:category:tree` | 30 min | 分类增删改后删除 |
| `ProductServiceImpl.home()` | `cache:product:home` | 10 min | 商品/分类变更后删除 |

### 改动范围

| 文件 | 改动 |
|------|------|
| `ProductServiceImpl.java` | 注入 `CacheClient`，detail/categories/home 加缓存 |
| `CategoryServiceImpl.java` | 注入 `CacheClient`，增删改后删除 `cache:category:tree` |
| `admin/ProductController.java` | 增删改后删除对应缓存键 |
| `RedisConstants.java` | 新增商品/分类相关常量 |

### 测试

- Service 单元测试（Mock CacheClient 验证缓存读写和失效行为）

---

## 5. 接口限流

### 方案

自定义 `@RateLimit` 注解 + Spring 拦截器，基于 Redis INCR + EXPIRE 实现滑动窗口。

```java
@RateLimit(key = "rate:login:", limit = 5, window = 60)
```

### 限流策略

| 接口 | 限制 |
|------|------|
| `POST /portal/auth/login` | 5 次/60s/IP |
| `POST /admin/auth/login` | 5 次/60s/IP |
| `GET /portal/auth/verificationCode` | 1 次/60s/IP |
| `GET /admin/auth/verificationCode` | 1 次/60s/IP |
| `POST /portal/auth/register` | 3 次/60s/IP |

### 新增文件

| 文件 | 说明 |
|------|------|
| `annotation/RateLimit.java` | 限流注解 |
| `interceptor/RateLimitInterceptor.java` | 拦截器（Redis INCR 计数） |
| `config/WebMvcConfig.java` | 注册拦截器 |

`BusinessStatusEnum` 新增 `RATE_LIMIT_EXCEEDED` 错误码。

### 测试

- 拦截器单元测试（Mock Redis 验证限流行为）

---

## 改动汇总

### 新建文件（12 个）

- `entity/SeckillSession.java`
- `mapper/SeckillSessionMapper.java` + XML
- `service/SeckillService.java`
- `service/impl/SeckillServiceImpl.java`
- `controller/portal/SeckillController.java`
- `enums/SeckillResultEnum.java`
- `annotation/RateLimit.java`
- `interceptor/RateLimitInterceptor.java`
- `config/WebMvcConfig.java`

### 修改文件（后端 ~15 个）

- `AuthService.java` / `AuthServiceImpl.java` — 验证码隔离 + JWT 黑名单
- `JwtUtil.java` — jti 支持
- `JwtAuthenticationFilter.java` — 黑名单检查
- `SecurityConfig.java` — 放行 logout 接口
- `portal/AuthController.java` / `admin/AuthController.java` — 验证码 uuid + logout
- `ProductServiceImpl.java` — 接入缓存
- `CategoryServiceImpl.java` — 缓存失效
- `RedisConstants.java` — 新增常量
- `BusinessStatusEnum.java` — 新增错误码
- `docs/mall.sql` — 新增秒杀表

### 修改文件（前端 ~4 个）

- portal `Login.vue` + `api/auth.js`
- admin `Login.vue` + `api/auth.js`

---

## 风险与约束

- 秒杀库存预热依赖定时任务或手动触发，本阶段采用"首次查询时预热"策略简化实现
- 新增 Redis 键遵循现有命名规范，不与已有键冲突
- 缓存时间需在配置中可调整
- 所有新增功能需写测试覆盖
