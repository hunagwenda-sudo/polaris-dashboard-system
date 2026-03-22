#!/bin/bash
# MySQL 初始化脚本：建表 + 种子数据
set -e

MYSQL_CMD="mysql --default-character-set=utf8mb4 -u root -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE"

echo "Creating tables..."
$MYSQL_CMD < /docker-entrypoint-initdb.d/sql/schema.sql

echo "Inserting seed data..."
$MYSQL_CMD < /docker-entrypoint-initdb.d/sql/seed-data.sql

echo "Database initialization complete."
