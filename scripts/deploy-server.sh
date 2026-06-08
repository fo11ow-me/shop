#!/bin/bash
# 服务器部署脚本（由本地 deploy.sh 通过 SSH 调用）
# 用法: bash deploy-server.sh [deploy|rollback] [sha]
set -euo pipefail

cd /opt/app/mall

# 加载 .env
if [ -f .env ]; then
  set -a; source .env; set +a
fi

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
echo "${REGISTRY_PASSWORD:-}" | docker login localhost:5000 -u "${REGISTRY_USER:-}" --password-stdin > /dev/null 2>&1 || true
docker tag localhost:5000/mall/mall-server:latest localhost:5000/mall/mall-server:rollback 2>/dev/null || true
docker tag localhost:5000/mall/mall-nginx:latest localhost:5000/mall/mall-nginx:rollback 2>/dev/null || true
echo "快照: rollback"

echo "========== 2. 拉取最新镜像 =========="
docker compose pull mall-server mall-nginx

echo "========== 3. 重启容器 =========="
docker compose up -d mall-server mall-nginx

echo "========== 4. 等待启动 =========="
sleep 10

echo "========== 5. 健康检查 =========="
for i in $(seq 1 10); do
  HEALTH=$(docker exec mall-nginx curl -s --max-time 5 http://mall-server:8800/actuator/health/liveness)
  if echo "$HEALTH" | grep -q '"status":"UP"'; then
    echo "健康检查通过 (第 ${i} 次)"
    break
  fi
  if [ "$i" -eq 10 ]; then
    echo "健康检查失败: ${HEALTH}"
    rollback ""
    exit 1
  fi
  echo "第 ${i} 次: ${HEALTH}, 重试..."
  sleep 5
done

echo "========== 6. 前端验证 =========="
HTTP_PORTAL=$(docker exec mall-nginx curl -s -o /dev/null -w "%{http_code}" --max-time 5 http://mall-server:8800)
HTTP_ADMIN=$(docker exec mall-nginx curl -s -o /dev/null -w "%{http_code}" --max-time 5 http://mall-server:8800/admin/)
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
