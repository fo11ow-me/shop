#!/bin/bash
# 导出数据库 → 替换旧 SQL → 停止容器

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

CONTAINER="mall-mysql"
SQL_FILE="sql/mall.sql"

if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
  echo "==> 导出数据库 $CONTAINER 到 $SQL_FILE ..."
  docker exec "$CONTAINER" mysqldump -uroot -p"${MYSQL_ROOT_PASSWORD:-123456}" --skip-column-statistics mall > "$SQL_FILE"
  echo "==> 数据库已导出到 $SQL_FILE"
else
  echo "==> 容器 $CONTAINER 未运行，跳过导出"
fi

echo "==> 停止所有容器..."
docker compose down
echo "==> 完成"
