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

if ! docker info > /dev/null 2>&1; then
  echo "错误: Docker 未运行"
  exit 1
fi

if [ -n "$(git status --porcelain)" ]; then
  echo "错误: 工作区有未提交变更，请先 commit"
  exit 1
fi

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
