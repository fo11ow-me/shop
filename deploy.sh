#!/bin/bash
# 本地一键构建部署脚本
#
# 流程: 测试 → 打包 → 构建镜像 → 推送 Registry → 触发 CD 部署
#
# 前置条件:
#   1. Docker 已安装并登录 ghcr.io: docker login ghcr.io
#   2. Maven + Node.js 已安装
#   3. Git 工作区干净（无未提交变更）
#
# 用法: bash deploy.sh

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

SHA="$(git rev-parse --short HEAD)"
REGISTRY="ghcr.io"
OWNER="fo11ow-me"
IMAGE_SERVER="${REGISTRY}/${OWNER}/mall-server"
IMAGE_NGINX="${REGISTRY}/${OWNER}/mall-nginx"

echo "========== 1. 运行后端测试 =========="
cd mall-server
./mvnw test -q
cd ..

echo "========== 2. 打包后端 JAR =========="
cd mall-server
./mvnw clean package -DskipTests -q
cd ..

echo "========== 3. 构建前端 =========="
cd mall-admin && npm run build 2>&1 | tail -1 && cd ..
cd mall-portal && npm run build 2>&1 | tail -1 && cd ..

echo "========== 4. 构建 Docker 镜像 (latest + ${SHA}) =========="
docker build --platform linux/amd64 \
  -t ${IMAGE_SERVER}:latest \
  -t ${IMAGE_SERVER}:${SHA} \
  -f mall-server/Dockerfile mall-server/

docker build --platform linux/amd64 \
  -t ${IMAGE_NGINX}:latest \
  -t ${IMAGE_NGINX}:${SHA} \
  -f Dockerfile.nginx .

echo "========== 5. 推送镜像到 Registry =========="
docker push ${IMAGE_SERVER}:latest
docker push ${IMAGE_SERVER}:${SHA}
docker push ${IMAGE_NGINX}:latest
docker push ${IMAGE_NGINX}:${SHA}

echo "========== 6. 推送代码触发 CD 部署 =========="
git push origin dev

echo ""
echo "========== 完成 =========="
echo "镜像:"
echo "  ${IMAGE_SERVER}:latest"
echo "  ${IMAGE_SERVER}:${SHA}"
echo "  ${IMAGE_NGINX}:latest"
echo "  ${IMAGE_NGINX}:${SHA}"
echo ""
echo "CD 已触发，监控: https://github.com/${OWNER}/mall/actions"
