# 商城系统实施方案

## 一、目标

实现覆盖商城核心业务的系统：**用户注册登录 → 浏览商品 → 加入购物车 → 下单结算 → 订单管理**。
* 对于商品图片等文件的保存采用阿里的对象存储
* mall-admin的技术栈升级到Vue 3 + Element Plus + Vite
* mall-server的技术栈也升级到springboot3
* 原代码如果有设计得不合理的地方，请进行优化
* 对于数据查询，希望采用mapper.xml文件的方式

[//]: # (* 搜索引擎采用Elasticsearch)
[//]: # (* 消息队列采用RabbitMQ)

* 每个功能模块实现完成后，你需要生成一个该功能模块的md文档，说明该模块已实现的功能、实现思路、相关代码

---

## 二、架构

```
┌──────────────────────────────────────┐
│               前端                     │
│  mall-admin (Vue 3 + Element Plus)   │
│  mall-portal (Vue 3 + Element Plus)  │
└─────────────────┬────────────────────┘
                  │ HTTP/REST
┌─────────────────▼────────────────────┐
│       mall-server (Spring Boot 3)     │
│  ┌──────────┬──────────┬──────────┐   │
│  │ 管理模块  │ 门户模块   │ 公共模块  │   │
│  └──────────┴──────────┴──────────┘   │
└─────────────────┬────────────────────┘
                  │
     ┌────────────┼────────────┐
     ▼            ▼            ▼
┌─────────┐ ┌─────────┐ ┌──────────┐
│  MySQL  │ │  Redis  │ │ 阿里云OSS │
└─────────┘ └─────────┘ └──────────┘
```

---

## 三、技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Java 17 + Spring Boot 3.2 | 基础框架 |
| Spring Security 6 + JWT | 认证授权 |
| MyBatis-Plus 3.5 | ORM，复杂查询使用 mapper.xml |
| MySQL 8.0 | 持久化存储 |
| Redis | 验证码缓存 |
| 阿里云 OSS | 文件存储（图片、导出文件） |
| springdoc-openapi | API 文档 |

### 前端

| 模块 | 技术栈 | 状态 |
|------|--------|------|
| mall-admin | Vue 3 + Element Plus + Vite + Pinia | ✅ 已完成 |
| mall-portal | Vue 3 + Element Plus + Vite | ✅ 已完成 |

---

## 四、数据库设计

| 表名 | 说明 |
|------|------|
| sys_user | 系统用户（管理员+门户用户共用） |
| per_menu | 菜单权限 |
| per_role | 角色 |
| per_role_menu | 角色-菜单关联 |
| per_user_role | 用户-角色关联 |
| pms_category | 商品分类（两级） |
| pms_product | 商品 |
| pms_product_img | 商品图片 |
| oms_cart | 购物车 |
| oms_order | 订单 |
| oms_order_item | 订单明细 |
| pay_payment | 支付记录 |
| sms_seckill_session | 秒杀场次（预留） |
| sms_seckill_order | 秒杀订单（预留） |

订单状态流转：`0-待支付 → 1-已支付 → 2-已发货 → 3-已完成 → 4-已取消`

标准化 SQL 文件：`docs/mall.sql`

---

## 五、接口设计

### 门户 API (`/api/portal/**`)

| 模块 | 端点 | 功能 |
|------|------|------|
| 认证 | POST /api/auth/register, /api/auth/login | 注册、登录 |
| 商品 | GET /product/home, /categories, /category/{id}, /search, /detail/{id} | 首页、分类、搜索、详情 |
| 购物车 | GET /cart/list, POST /cart/add, PUT /cart/update, DELETE /cart/delete/{id}, /batchDelete | CRUD |
| 订单 | POST /order/create, GET /order/list, /detail/{id}, PUT /pay/{id}, DELETE /delete/{id} | 下单、支付、查询 |

### 管理后台 API

| 模块 | 端点 | 功能 |
|------|------|------|
| 登录 | POST /login/{validateCode}, GET /validate/code | 管理员登录 |
| 用户 | /user CRUD + setRole/export/import | 用户管理 |
| 角色 | /role CRUD + setMenu/export/import | 角色管理 |
| 菜单 | /menu CRUD + all/staff/permission | 菜单管理 |
| 商品 | /api/admin/product/list, POST/PUT/DELETE | 商品管理 |
| 分类 | /api/admin/category/tree, POST/PUT/DELETE | 分类管理 |
| 订单 | /api/admin/order/list, /detail/{id}, PUT /deliver/{id} | 订单管理 |

---

## 六、实施计划

### Phase 1：基础升级 ✅

- Spring Boot 3.2 升级，javax→jakarta
- Swagger 2 → springdoc-openapi
- jjwt 0.12.x 升级
- sys_staff→sys_user，Staff→User 重命名
- 数据库标准化（移除部门，统一命名）
- 构造器注入改造

### Phase 2：OSS 文件存储集成（基础能力）

> 作为前置基础能力，统一管理系统中的各类文件资源。用户头像、商品图片等功能均依赖此阶段。

- 引入阿里云 OSS Java SDK (`aliyun-sdk-oss`)
- 配置 `OssConfig` 读取 endpoint / accessKey / secretKey / bucket
- 封装 `OssService`：文件上传、删除、签名 URL 生成
- `application-dev.yml` 新增 `aliyun.oss.*` 配置项
- 替换 `application.yml` 中本地 `file-path` 为 OSS 方案
- 提供统一文件操作接口，供后续业务模块复用

### Phase 3：门户 API 实现 ✅

- 用户模块（注册、登录）
- 商品模块（分类、搜索、详情）
- 购物车模块（CRUD）
- 订单模块（下单、模拟支付、列表）

### Phase 4：管理后台升级 ✅

- 管理后台商品管理（CRUD、上下架）
- 管理后台分类管理（树形 CRUD）
- 管理后台订单管理（列表、详情、发货）
- 首页仪表板（统计卡片）

### 后续阶段

| 阶段 | 内容 | 状态 |
|------|------|------|
| Elasticsearch 搜索 | 商品搜索从 MySQL LIKE 迁移至 ES，支持分词、高亮、聚合 | 待实施 |
| RabbitMQ + 秒杀 | 消息队列削峰，秒杀场次 + 秒杀订单 | 待实施 |

---

## 七、验证方案

1. **后端 API**：curl 逐接口测试，注册→登录→加购→下单→支付全流程通过
2. **OSS 文件存储**：图片上传至 OSS 并返回 URL，商品详情中图片正常加载
3. **门户前端**：mall-portal (3001) 页面正常加载，API 调用正常
4. **管理后台**：mall-admin (3002) 登录、用户/角色/菜单/商品/分类/订单 CRUD 正常
5. **数据库**：标准化 SQL 文件 `docs/mall.sql` 可直接建库
