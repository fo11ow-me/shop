# 服务器部署指南

> CI/CD 自动化部署已配置。push 到 `dev` 分支自动触发构建、镜像推送、SSH 部署和健康验证。

## 前置条件

1. 服务器安装 Docker 和 Docker Compose
2. 服务器可以访问 ghcr.io（GitHub Container Registry）
3. 已将部署 SSH 公钥添加到服务器的 `~/.ssh/authorized_keys`

## 1. 服务器目录结构

```bash
# 创建部署目录
mkdir -p /opt/app/mall
mkdir -p /opt/docker/mall-mysql/data
mkdir -p /opt/docker/mall-mysql/conf.d
mkdir -p /opt/docker/mall-redis/data
mkdir -p /opt/docker/mall-es/data
```

## 2. 环境变量配置

将仓库中的 `docker-compose.server.yml` 复制为服务器上的 `docker-compose.yml`。
将仓库中的 `docker-compose.server.env.example` 复制为服务器上的 `.env`。

```bash
# 在服务器上
cd /opt/app/mall
cp docker-compose.server.env.example .env
chmod 600 .env

# 编辑 .env，将 CHANGEME 替换为真实值
vim .env
```

### 生成安全密钥

```bash
# 生成 JWT 密钥（256 位）
openssl rand -base64 32

# 生成数据库/Redis 密码
openssl rand -base64 16
```

## 3. Docker 网络

```bash
# 创建外部网络（如不存在）
docker network create my_network 2>/dev/null || true
```

## 4. 启动服务

```bash
cd /opt/app/mall
docker compose pull
docker compose up -d
```

## 5. GitHub Secrets 配置

在仓库 `https://github.com/fo11ow-me/mall/settings/secrets/actions` 添加以下 Secrets：

| Secret 名称 | 说明 |
|-------------|------|
| `SERVER_HOST` | 服务器地址 |
| `SERVER_USER` | SSH 登录用户名 |
| `SERVER_PORT` | SSH 端口（默认 22） |
| `SERVER_SSH_KEY` | SSH 私钥（用于 GitHub Actions 连接服务器） |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 |
| `REDIS_PASSWORD` | Redis 密码 |
| `RABBITMQ_USER` | RabbitMQ 用户名 |
| `RABBITMQ_PASSWORD` | RabbitMQ 密码 |
| `DB_PASSWORD` | 数据库密码（通常与 MYSQL_ROOT_PASSWORD 相同） |
| `OSS_ACCESS_KEY_ID` | 阿里云 OSS AccessKey ID |
| `OSS_ACCESS_KEY_SECRET` | 阿里云 OSS AccessKey Secret |
| `JWT_ADMIN_SECRET` | 管理后台 JWT 签名密钥 |
| `JWT_PORTAL_SECRET` | 用户门户 JWT 签名密钥 |

### 生成部署 SSH 密钥对

```bash
# 在本地生成专用部署密钥
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_actions_deploy

# 将公钥添加到服务器
ssh-copy-id -i ~/.ssh/github_actions_deploy.pub <SERVER_USER>@<SERVER_HOST>

# 将私钥内容添加到 GitHub Secret SERVER_SSH_KEY
cat ~/.ssh/github_actions_deploy
```

## 6. 部署验证

推送代码到 `dev` 分支后，GitHub Actions 自动执行以下流程：

1. CI 工作流：运行后端测试 + 前端构建验证
2. CD 工作流：构建 Docker 镜像 → 推送到 ghcr.io → SSH 部署 → 健康检查

验证部署：

```bash
# 检查容器状态
ssh <SERVER_USER>@<SERVER_HOST> "docker ps --filter 'name=mall'"

# 检查健康端点
curl -s "http://<SERVER_HOST>/health"

# 访问服务
# 门户:     http://<SERVER_HOST>/
# 管理后台: http://<SERVER_HOST>/admin
```

## 7. 常用运维命令

```bash
# 查看容器日志
ssh <SERVER_USER>@<SERVER_HOST> "docker logs mall-server --tail 100"

# 重启服务
ssh <SERVER_USER>@<SERVER_HOST> "cd /opt/app/mall && docker compose restart"

# 更新并重启（手动部署）
ssh <SERVER_USER>@<SERVER_HOST> "cd /opt/app/mall && docker compose pull && docker compose up -d"

# 查看所有容器状态
ssh <SERVER_USER>@<SERVER_HOST> "docker compose ps"
```
