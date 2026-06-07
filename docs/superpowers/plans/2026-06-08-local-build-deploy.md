# 本地构建部署 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 放弃 GitHub Actions，本地一键部署：测试 → 构建 → 推送到服务器自建 Registry → SSH 触发服务器部署

**Architecture:** 本地 `deploy.sh` 负责测试/构建/推送镜像，通过 SSH 调用服务器 `deploy-server.sh` 完成 pull → up → health check。Registry 作为 docker-compose 服务运行在服务器 `:5000`。

**Tech Stack:** Bash, Docker, Docker Compose, Maven, npm, SSH

---

### Task 1: 删除 GitHub Actions 工作流

**Files:**
- Delete: `.github/workflows/ci.yml`
- Delete: `.github/workflows/cd.yml`
- Delete: `.github/workflows/claude.yml`

- [ ] **Step 1: 删除 workflow 文件**

```bash
cd D:/project/idea/mall
git rm -r .github/workflows/
```

- [ ] **Step 2: 提交**

```bash
git commit -m "chore: 删除 GitHub Actions，改为本地部署"
```

---

### Task 2: 重写 `deploy.sh`

**Files:**
- Modify: `deploy.sh`（完全重写）

- [ ] **Step 1: 写入脚本**

```bash
#!/bin/bash
# 本地一键构建部署脚本
# 用法: bash deploy.sh [--skip-tests]
set -euo pipefail

SKIP_TESTS=false
if [ "${1:-}" = "--skip-tests" ]; then
  SKIP_TESTS=true
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# 加载 .env
if [ -f .env ]; then
  set -a; source .env; set +a
fi

REQUIRED_VARS="SERVER_HOST SERVER_USER REGISTRY_USER REGISTRY_PASSWORD"
for v in $REQUIRED_VARS; do
  if [ -z "${!v:-}" ]; then
    echo "错误: .env 缺少变量 $v"
    exit 1
  fi
done

SHA="$(git rev-parse --short HEAD)"
REGISTRY="${SERVER_HOST}:5000"
IMAGE_SERVER="${REGISTRY}/mall/mall-server"
IMAGE_NGINX="${REGISTRY}/mall/mall-nginx"

echo "========== 1. 检查前置条件 =========="

# Docker
if ! docker info > /dev/null 2>&1; then
  echo "错误: Docker 未运行"
  exit 1
fi

# Git 工作区干净
if [ -n "$(git status --porcelain)" ]; then
  echo "错误: 工作区有未提交变更，请先 commit"
  exit 1
fi

# SSH 连通性
if ! ssh -o ConnectTimeout=5 -o BatchMode=yes "${SERVER_USER}@${SERVER_HOST}" echo ok > /dev/null 2>&1; then
  echo "错误: 无法 SSH 连接到 ${SERVER_USER}@${SERVER_HOST}"
  exit 1
fi
echo "Docker: ok  SSH: ok  Git: clean"

echo ""
echo "========== 2. 运行后端测试 =========="
if $SKIP_TESTS; then
  echo "跳过 (--skip-tests)"
else
  cd mall-server
  ./mvnw test -q
  cd ..
  echo "Tests: passed"
fi

echo ""
echo "========== 3. 打包后端 JAR =========="
cd mall-server
./mvnw clean package -DskipTests -q
cd ..

echo "========== 4. 构建前端 =========="
cd mall-admin && npm run build 2>&1 | tail -1 && cd ..
cd mall-portal && npm run build 2>&1 | tail -1 && cd ..

echo ""
echo "========== 5. 构建 Docker 镜像 (latest + ${SHA}) =========="
echo "${REGISTRY_PASSWORD}" | docker login "${REGISTRY}" -u "${REGISTRY_USER}" --password-stdin > /dev/null 2>&1

docker build --platform linux/amd64 \
  -t "${IMAGE_SERVER}:latest" \
  -t "${IMAGE_SERVER}:${SHA}" \
  -f mall-server/Dockerfile mall-server/

docker build --platform linux/amd64 \
  -t "${IMAGE_NGINX}:latest" \
  -t "${IMAGE_NGINX}:${SHA}" \
  -f Dockerfile.nginx .

echo "  ${IMAGE_SERVER}:latest"
echo "  ${IMAGE_SERVER}:${SHA}"
echo "  ${IMAGE_NGINX}:latest"
echo "  ${IMAGE_NGINX}:${SHA}"

echo ""
echo "========== 6. 推送镜像到 Registry =========="
docker push "${IMAGE_SERVER}:latest"
docker push "${IMAGE_SERVER}:${SHA}"
docker push "${IMAGE_NGINX}:latest"
docker push "${IMAGE_NGINX}:${SHA}"
echo "推送完成"

# 清理本地旧标签（保留最近 3 个 SHA）
echo ""
echo "清理本地旧标签..."
ALL_TAGS=$(docker image ls --format '{{.Repository}}:{{.Tag}}' \
  | grep "^${REGISTRY}/mall/" \
  | grep -v ':latest' \
  | sort)
KEEP_COUNT=3
COUNT=0
echo "$ALL_TAGS" | while read -r tag; do
  COUNT=$((COUNT + 1))
  if [ $COUNT -gt $KEEP_COUNT ]; then
    docker rmi "$tag" 2>/dev/null || true
    echo "  删除: $tag"
  fi
done

echo ""
echo "========== 7. SSH 触发服务器部署 =========="
ssh -o StrictHostKeyChecking=no \
  "${SERVER_USER}@${SERVER_HOST}" \
  "bash /opt/app/mall/deploy-server.sh"

echo ""
echo "========== 完成 =========="
echo "当前版本: ${SHA}"
```

- [ ] **Step 2: 赋予执行权限并提交**

```bash
chmod +x deploy.sh
git add deploy.sh
git commit -m "feat: 重写本地部署脚本 — 测试/构建/推送/SSH 部署"
```

---

### Task 3: 创建服务器部署脚本 `deploy-server.sh`

**Files:**
- Create: 服务器 `/opt/app/mall/deploy-server.sh`（通过 SSH 远程创建）

- [ ] **Step 1: 通过 SCP 上传脚本到服务器**

先本地创建脚本文件：

```bash
cat > /tmp/deploy-server.sh << 'SCRIPT'
#!/bin/bash
# 服务器部署脚本（由本地 deploy.sh 通过 SSH 调用）
set -euo pipefail

cd /opt/app/mall

ACTION="${1:-deploy}"
SHA="${2:-}"

rollback() {
  local target_sha="${1}"
  echo "回滚中..."
  if [ -n "$target_sha" ]; then
    docker tag "localhost:5000/mall/mall-server:${target_sha}" localhost:5000/mall/mall-server:rollback 2>/dev/null || true
    docker tag "localhost:5000/mall/mall-nginx:${target_sha}" localhost:5000/mall/mall-nginx:rollback 2>/dev/null || true
  fi
  docker tag localhost:5000/mall/mall-server:rollback localhost:5000/mall/mall-server:latest 2>/dev/null || true
  docker tag localhost:5000/mall/mall-nginx:rollback localhost:5000/mall/mall-nginx:latest 2>/dev/null || true
  docker compose up -d --no-deps mall-server mall-nginx
  echo "回滚完成"
}

if [ "$ACTION" = "rollback" ]; then
  rollback "$SHA"
  exit 0
fi

echo "========== 1. 快照当前镜像 =========="
echo "${REGISTRY_PASSWORD}" | docker login localhost:5000 -u "${REGISTRY_USER}" --password-stdin > /dev/null 2>&1 || true
docker tag localhost:5000/mall/mall-server:latest localhost:5000/mall/mall-server:rollback 2>/dev/null || true
docker tag localhost:5000/mall/mall-nginx:latest localhost:5000/mall/mall-nginx:rollback 2>/dev/null || true
echo "快照: rollback"

echo "========== 2. 拉取最新镜像 =========="
docker compose pull mall-server mall-nginx

echo "========== 3. 重启容器 =========="
docker compose up -d --no-deps mall-server mall-nginx

echo "========== 4. 等待启动 =========="
sleep 10

echo "========== 5. 健康检查 =========="
for i in $(seq 1 10); do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 http://localhost/health)
  if [ "$STATUS" = "200" ]; then
    echo "健康检查通过 (第 ${i} 次)"
    break
  fi
  if [ "$i" -eq 10 ]; then
    echo "健康检查失败"
    rollback ""
    exit 1
  fi
  echo "第 ${i} 次: status=${STATUS}, 重试..."
  sleep 5
done

echo "========== 6. 前端验证 =========="
HTTP_PORTAL=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 http://localhost/)
HTTP_ADMIN=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 http://localhost/admin/)
if [ "$HTTP_PORTAL" != "200" ]; then
  echo "门户验证失败: ${HTTP_PORTAL}"
  rollback ""
  exit 1
fi
if [ "$HTTP_ADMIN" != "200" ]; then
  echo "管理后台验证失败: ${HTTP_ADMIN}"
  rollback ""
  exit 1
fi
echo "门户: ${HTTP_PORTAL}, 管理后台: ${HTTP_ADMIN}"

echo ""
echo "========== 部署成功 =========="
docker inspect mall-server --format='镜像: {{index .Config.Image}}' 2>/dev/null
SCRIPT

# 上传到服务器
scp /tmp/deploy-server.sh "${SERVER_USER}@${SERVER_HOST}:/opt/app/mall/deploy-server.sh"
ssh "${SERVER_USER}@${SERVER_HOST}" "chmod +x /opt/app/mall/deploy-server.sh"
```

- [ ] **Step 2: 测试脚本可执行**

```bash
ssh "${SERVER_USER}@${SERVER_HOST}" "bash /opt/app/mall/deploy-server.sh"
```

验证部署成功，无报错。

---

### Task 4: 更新 `docker-compose.server.yml`

**Files:**
- Modify: `docker-compose.server.yml`

- [ ] **Step 1: 修改 mall-server 和 mall-nginx 镜像地址**

将：
```yaml
image: ghcr.io/fo11ow-me/mall-server:latest
```
改为：
```yaml
image: localhost:5000/mall/mall-server:latest
```

将：
```yaml
image: ghcr.io/fo11ow-me/mall-nginx:latest
```
改为：
```yaml
image: localhost:5000/mall/mall-nginx:latest
```

- [ ] **Step 2: 新增 Registry 服务**

在 `services:` 块中添加（放在 mall-mysql 之前或之后均可）：

```yaml
  mall-registry:
    image: registry:2
    container_name: mall-registry
    mem_limit: 256m
    environment:
      REGISTRY_STORAGE_DELETE_ENABLED: "true"
      REGISTRY_AUTH: htpasswd
      REGISTRY_AUTH_HTPASSWD_REALM: "Registry Realm"
      REGISTRY_AUTH_HTPASSWD_PATH: /auth/htpasswd
    volumes:
      - /opt/docker/mall-registry/data:/var/lib/registry
      - /opt/docker/mall-registry/auth:/auth
      - /etc/localtime:/etc/localtime:ro
    networks:
      - my_network
    restart: unless-stopped
```

- [ ] **Step 3: 提交**

```bash
git add docker-compose.server.yml
git commit -m "feat: 镜像源改为服务器自建 Registry + 新增 registry 服务"
```

---

### Task 5: 更新 CLAUDE.md

**Files:**
- Modify: `.claude/CLAUDE.md`（项目级）

- [ ] **Step 1: 替换部署相关章节**

找到 "CI/CD" 章节，替换为：

```markdown
## 部署

### 流程

```bash
bash deploy.sh                 # 标准部署
bash deploy.sh --skip-tests    # 跳过测试（紧急热修复）
```

脚本自动完成：后端测试 → 打包 → 前端构建 → Docker 构建 → 推送到服务器 Registry → SSH 触发服务器部署。

### 服务器

- 部署路径: `/opt/app/mall/`
- 部署脚本: `/opt/app/mall/deploy-server.sh`
- Registry: `localhost:5000`
- 镜像: `localhost:5000/mall/mall-server:latest` / `mall-nginx:latest`

### 回滚

```bash
ssh <服务器> bash /opt/app/mall/deploy-server.sh rollback          # 回滚到上一版本
ssh <服务器> bash /opt/app/mall/deploy-server.sh rollback abc1234  # 回滚到指定 SHA
```
```

- [ ] **Step 2: 提交**

```bash
git add .claude/CLAUDE.md
git commit -m "docs: 更新部署说明 — 本地构建 + 服务器 Registry"
```

---

## 验证清单

- [ ] `deploy.sh` 本地可执行（`--skip-tests` 模式快速验证）
- [ ] 服务器 Registry 容器正常运行
- [ ] 镜像推送成功（`docker push localhost:5000/mall/mall-server:latest`）
- [ ] `deploy-server.sh` 可拉取部署并健康检查通过
- [ ] 健康检查失败时自动回滚
- [ ] 前端门户/管理后台可访问
