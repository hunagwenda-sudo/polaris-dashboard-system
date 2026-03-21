#!/bin/bash
# MySQL 初始化脚本：按顺序执行所有 SQL
set -e

MYSQL_CMD="mysql --default-character-set=utf8mb4 -u root -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE"

echo "Initializing database..."
$MYSQL_CMD < /docker-entrypoint-initdb.d/sql/schema.sql

for f in /docker-entrypoint-initdb.d/sql/migration-v*.sql; do
  if [ -f "$f" ]; then
    echo "Running $f ..."
    $MYSQL_CMD < "$f"
  fi
done

if [ -f /docker-entrypoint-initdb.d/sql/seed-data.sql ]; then
  echo "Running seed-data.sql ..."
  $MYSQL_CMD < /docker-entrypoint-initdb.d/sql/seed-data.sql
fi

echo "Database initialization complete."
