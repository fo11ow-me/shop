# 本地构建部署方案

> 目标：放弃 GitHub Actions，本地测试 + 构建 → 推送自建 Registry → 服务器部署，一键完成

---

## 一、整体架构

```
本地开发机                          服务器
┌─────────────────┐              ┌──────────────────────────┐
│ 1. mvnw test    │              │ Registry (:5000)         │
│ 2. mvnw package │   push ──>   │  mall-server:latest       │
│ 3. npm run build│              │  mall-server:<sha>        │
│ 4. docker build │              │  mall-nginx:latest       │
│ 5. docker push  │              │  mall-nginx:<sha>         │
│ 6. ssh deploy   │   ──────────>│                          │
└─────────────────┘              │ deploy-server.sh:        │
                                 │  pull → up → health      │
                                 │  → 成功 / 自动回滚       │
                                 └──────────────────────────┘
```

### 核心原则

- 本地测试通过才可部署（`--skip-tests` 仅紧急情况）
- 镜像一次构建，本地与服务器使用同一镜像
- 每次打 `latest` + `<commit-sha>` 双标签
- 保留最近 3 个 SHA 版本，服务器保留 `rollback` 标签

---

## 二、本地脚本 `deploy.sh`

位于项目根目录。

### 流程

| 步骤 | 操作 | 失败处理 |
|------|------|---------|
| 1. 检查前置条件 | Docker 运行中、工作区干净 | 提示并退出 |
| 2. 后端测试 | `./mvnw test`（`--skip-tests` 跳过） | 终止部署 |
| 3. 构建 | Maven 打包 JAR → npm build × 2 | 终止 |
| 4. Docker 构建 | `docker build` mall-server + mall-nginx，双标签 | 终止 |
| 5. 推送镜像 | `docker push` 到 Registry，清理本地旧标签 | 终止 |
| 6. SSH 部署 | 调用服务器 `deploy-server.sh` | 显示失败 |

### 使用方式

```bash
bash deploy.sh                 # 标准部署
bash deploy.sh --skip-tests    # 跳过测试（紧急热修复）
```

### 前置条件检查

- `docker info` 确认 Docker 运行
- `git status --porcelain` 确认无未提交变更
- SSH 密钥可用（`ssh -o ConnectTimeout=5 $SERVER_HOST echo ok`）

---

## 三、服务器脚本 `deploy-server.sh`

位于 `/opt/app/mall/deploy-server.sh`，由本地 `deploy.sh` 通过 SSH 调用。

### 流程

| 步骤 | 操作 |
|------|------|
| 1. 快照当前镜像 | `docker tag` 当前版本为 `rollback` 标签 |
| 2. 拉取最新镜像 | `docker compose pull mall-server mall-nginx` |
| 3. 重启容器 | `docker compose up -d --no-deps mall-server mall-nginx` |
| 4. 等待启动 | sleep 10 |
| 5. 健康检查 | `/actuator/health`，最多 10 次重试，间隔 5 秒 |
| 6. 前端验证 | `curl` 门户 `/` 和管理后台 `/admin/`，期望 200 |
| 7. 失败回滚 | 任一验证失败 → `docker tag rollback latest` → `up -d` |
| 8. 输出结果 | 成功/失败信息 + 当前镜像版本 |

### 手动回滚

```bash
bash deploy-server.sh rollback          # 回滚到上一版本（rollback 标签）
bash deploy-server.sh rollback abc1234  # 回滚到指定 SHA 版本
```

---

## 四、Docker Registry

### 部署

在 `docker-compose.server.yml` 中新增：

```yaml
mall-registry:
  image: registry:2
  container_name: mall-registry
  mem_limit: 256m
  environment:
    REGISTRY_STORAGE_DELETE_ENABLED: "true"
  volumes:
    - /opt/docker/mall-registry/data:/var/lib/registry
    - /opt/docker/mall-registry/auth:/auth
    - /etc/localtime:/etc/localtime:ro
  networks:
    - my_network
  restart: unless-stopped
```

### 认证

htpasswd 基本认证，凭据在 `.env` 中：

```bash
REGISTRY_USER=<用户名>
REGISTRY_PASSWORD=<密码>
```

### 镜像命名

```
<服务器IP>:5000/mall/mall-server:latest
<服务器IP>:5000/mall/mall-server:<commit-sha>
<服务器IP>:5000/mall/mall-nginx:latest
<服务器IP>:5000/mall/mall-nginx:<commit-sha>
```

### 版本保留

- 本地：保留最近 3 个 SHA 标签，`deploy.sh` 推送后自动清理旧标签
- 服务器：保留 `rollback` 标签用于快速回滚

---

## 五、镜像地址变更

`docker-compose.server.yml` 中 mall-server 和 mall-nginx 的镜像地址：

| 服务 | 之前 | 之后 |
|------|------|------|
| mall-server | `ghcr.io/fo11ow-me/mall-server:latest` | `localhost:5000/mall/mall-server:latest` |
| mall-nginx | `ghcr.io/fo11ow-me/mall-nginx:latest` | `localhost:5000/mall/mall-nginx:latest` |

中间件（MySQL、Redis、RabbitMQ、ES）镜像地址不变。

---

## 六、敏感信息管理

### `.env` 文件

| 环境 | 位置 | 权限 |
|------|------|------|
| 本地 | 项目根目录 `.env` | 不提交 |
| 服务器 | `/opt/app/mall/.env` | chmod 600 |

### 内容

```bash
# 数据库
MYSQL_ROOT_PASSWORD=<密码>
DB_PASSWORD=<密码>

# Redis
REDIS_PASSWORD=<密码>

# RabbitMQ
RABBITMQ_PASSWORD=<密码>

# JWT
JWT_ADMIN_SECRET=<密钥>
JWT_PORTAL_SECRET=<密钥>

# OSS
OSS_ACCESS_KEY_ID=<ID>
OSS_ACCESS_KEY_SECRET=<密钥>

# Registry
REGISTRY_USER=<用户名>
REGISTRY_PASSWORD=<密码>

# SSH
SERVER_HOST=<服务器IP>
SERVER_USER=root
SERVER_PORT=22
```

---

## 七、清理项

| 操作 | 内容 |
|------|------|
| 删除 | `.github/workflows/` 整个目录 |
| 修改 | `docker-compose.server.yml` 镜像地址 + 新增 Registry 服务 |
| 新建 | 服务器 `/opt/app/mall/deploy-server.sh` |
| 重写 | 本地 `deploy.sh` |
| 更新 | `CLAUDE.md` 部署说明 |

---

## 八、部署流程（完整）

```
1. 本地开发完成，代码已提交
2. bash deploy.sh
3. 脚本自动：测试 → 打包 → 构建镜像 → 推送 Registry → SSH 触发服务器部署
4. 服务器：拉取镜像 → restart → 健康检查 → 验证前端
5. 成功：输出镜像版本，完成
6. 失败：自动回滚到上一版本，输出失败日志
```

### 示例输出

```
========== 1. 检查前置条件 ==========
Docker: ok  SSH: ok  Git: clean

========== 2. 运行后端测试 ==========
Tests run: 200, Failures: 0, Errors: 0

========== 3. 打包后端 JAR ==========
BUILD SUCCESS

========== 4. 构建前端 ==========
admin: ✓ built in 12.3s
portal: ✓ built in 8.7s

========== 5. 构建 Docker 镜像 (latest + abc1234) ==========
mall-server:latest  built
mall-server:abc1234 built
mall-nginx:latest   built
mall-nginx:abc1234  built

========== 6. 推送镜像到 Registry ==========
Pushed: mall-server:latest, mall-server:abc1234
Pushed: mall-nginx:latest, mall-nginx:abc1234
Cleaned old tags: def5678, ghi9012, jkl3456

========== 7. SSH 触发服务器部署 ==========
Deploying...
Server: pull complete
Server: containers restarted
Server: health check passed (3/10 attempts)
Server: portal 200, admin 200

========== 完成 ==========
当前版本: abc1234
```
