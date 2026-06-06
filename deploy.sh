#!/bin/bash
# 本地开发辅助脚本
# 用于开发者本地构建 Docker 镜像并推送到 ghcr.io
#
# 前置条件:
#   1. 已安装 Docker 并登录 ghcr.io: docker login ghcr.io
#   2. 已安装 Maven 和 Node.js
#
# 注意: 此脚本仅供本地开发使用，生产部署由 GitHub Actions 自动完成

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

echo "========== 1. 构建后端 JAR =========="
cd mall-server
mvn clean package -DskipTests -q
cd ..

echo "========== 2. 构建前端 =========="
cd mall-admin && npm run build 2>&1 | tail -1 && cd ..
cd mall-portal && npm run build 2>&1 | tail -1 && cd ..

echo "========== 3. 构建 Docker 镜像 =========="
docker build --platform linux/amd64 -t ghcr.io/fo11ow-me/mall-server:latest -f mall-server/Dockerfile mall-server/
docker build --platform linux/amd64 -t ghcr.io/fo11ow-me/mall-nginx:latest -f Dockerfile.nginx .

echo "========== 4. 推送镜像到 GitHub Container Registry =========="
docker push ghcr.io/fo11ow-me/mall-server:latest
docker push ghcr.io/fo11ow-me/mall-nginx:latest

echo "========== 部署完成 =========="
echo "镜像已推送到:"
echo "  ghcr.io/fo11ow-me/mall-server:latest"
echo "  ghcr.io/fo11ow-me/mall-nginx:latest"
echo ""
echo "如需部署到服务器，请运行 GitHub Actions CD 工作流"
echo "或手动执行服务器端部署:"
echo "  ssh <your-server> 'cd /opt/app/mall && docker compose pull && docker compose up -d'"
