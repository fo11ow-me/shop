#!/bin/bash
# 秒杀压测场景清理脚本 — 在场景切换时执行，确保无数据交叉污染
set -euo pipefail

_ERROR_HANDLED=false

_error_handler() {
  local exit_code=$?
  local failed_cmd="$1"
  local failed_line="$2"
  trap '' ERR
  if [ "$_ERROR_HANDLED" = true ]; then
    exit "$exit_code"
  fi
  echo "错误: 脚本执行失败" >&2
  echo "  脚本: $(basename "$0")" >&2
  echo "  行号: ${failed_line}" >&2
  echo "  命令: ${failed_cmd}" >&2
  echo "  退出码: ${exit_code}" >&2
  exit "$exit_code"
}

trap '_error_handler "${BASH_COMMAND}" "${LINENO}"' ERR

SESSION_ID=${1:-99}
PRODUCT_ID=${2:-19}
STOCK=${3:-500}

echo "=== 场景清理: session=$SESSION_ID product=$PRODUCT_ID stock=$STOCK ==="

# 1. 停后端
echo "[1/6] 停止后端..."
PID=$(netstat -ano | grep ":8800" | grep LISTENING | awk '{print $NF}' | head -1) || true
if [ -n "${PID:-}" ]; then
  taskkill //PID "$PID" //F 2>/dev/null || true
  sleep 3
fi

# 2. 清空所有 RabbitMQ 队列
echo "[2/6] 清空队列..."
for q in seckill.order.queue seckill.retry.delay.queue order.delay.queue order.timeout.queue; do
  docker exec mall-rabbitmq rabbitmqctl purge_queue "$q" 2>/dev/null || true
done

# 3. 清理测试订单
echo "[3/6] 清理订单..."
docker exec mall-mysql mysql -uroot -p123456 mall -e "
  DELETE FROM oms_order_item WHERE order_id IN (SELECT id FROM oms_order WHERE seckill_session_id=$SESSION_ID);
  DELETE FROM oms_order WHERE seckill_session_id=$SESSION_ID;
" 2>/dev/null || true
echo "[4/6] 恢复商品库存..."
docker exec mall-mysql mysql -uroot -p123456 mall -e "UPDATE pms_product SET stock=$STOCK WHERE id=$PRODUCT_ID;" 2>/dev/null \
  || { echo "错误: 恢复商品库存失败 (product=$PRODUCT_ID)" >&2; _ERROR_HANDLED=true; exit 1; }

# 5. 重置 Redis + 清除缓存
echo "[5/6] 重置 Redis..."
docker exec mall-redis redis-cli -a 123456 --no-auth-warning -n 1 \
  SET "seckill:stock:$SESSION_ID" "$STOCK" 2>/dev/null \
  || { echo "错误: 重置 Redis 库存失败" >&2; _ERROR_HANDLED=true; exit 1; }
docker exec mall-redis redis-cli -a 123456 --no-auth-warning -n 1 \
  DEL "cache:seckill:active" "cache:seckill:upcoming" 2>/dev/null \
  || { echo "错误: 清除 Redis 缓存失败" >&2; _ERROR_HANDLED=true; exit 1; }

# 6. 重启后端
echo "[6/6] 重启后端..."
cd D:/project/idea/mall/mall-server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev > /dev/null 2>&1 &

# 等待后端就绪
echo -n "等待后端就绪"
for i in $(seq 1 60); do
  if curl -s http://localhost:8800/actuator/health 2>/dev/null | grep -q UP; then
    echo " OK"
    break
  fi
  echo -n "."
  sleep 2
done

echo "=== 清理完成: stock=$STOCK, orders=0, queues=0 ==="
