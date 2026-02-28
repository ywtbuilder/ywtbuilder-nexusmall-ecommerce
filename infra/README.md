---
owner: infra
updated: 2026-02-23
scope: mall-v3
audience: dev,qa,ops
doc_type: runbook
---

# Infra 模块说明（本地依赖编排）

> 文档导航：统一入口见 [../docs/README.md](../docs/README.md)。

## 1. 目标

`infra/` 负责本地开发环境的中间件依赖编排，入口文件是 `infra/docker-compose.local.yml`。

## 2. 服务清单

| 服务 | 镜像 | 容器名 | 端口映射 | 默认账号 | 健康检查 |
|---|---|---|---|---|---|
| MySQL 8.0 | `mysql:8.0` | `mallv3-mysql` | `13306:3306` | `root/root` | `mysqladmin ping` |
| Redis 7 | `redis:7-alpine` | `mallv3-redis` | `16379:6379` | 无 | `redis-cli ping` |
| MongoDB 4 | `mongo:4` | `mallv3-mongodb` | `27018:27017` | 无 | 无（默认） |
| RabbitMQ 3.9 + 管理台 | `rabbitmq:3.9-management` | `mallv3-rabbitmq` | `5673:5672`, `15673:15672` | `mall/mall`, vhost=`/mall` | `rabbitmq-diagnostics -q ping` |
| Elasticsearch 7.17.21 | `elasticsearch:7.17.21` | `mallv3-es` | `9201:9200`, `9301:9300` | 无鉴权（本地） | `curl http://localhost:9200/_cluster/health` |
| MinIO (latest) | `minio/minio` | `mallv3-minio` | `19090:9000`, `19001:9001` | `minioadmin/minioadmin` | 无（默认） |

### 2.1 数据持久化卷

所有服务使用 Docker named volume，确保容器重建后数据保留：

| Volume | 服务 |
|---|---|
| `mysql_data` | MySQL |
| `redis_data` | Redis |
| `mongo_data` | MongoDB |
| `rabbitmq_data` | RabbitMQ |
| `es_data` | Elasticsearch |
| `minio_data` | MinIO |

### 2.2 MySQL 特殊配置

- 字符集：`utf8mb4`，排序规则：`utf8mb4_unicode_ci`（通过 `command` 参数配置）
- 初始化脚本：`data/migration/` 目录会被挂载到 `/docker-entrypoint-initdb.d:ro`，**首次启动**时自动执行
- 数据库：`mall`（通过 `MYSQL_DATABASE` 环境变量自动创建）

### 2.3 Elasticsearch 特殊配置

- 运行模式：单节点（`discovery.type=single-node`）
- JVM 堆：`-Xms256m -Xmx256m`
- 安全：已关闭 X-Pack 安全（`xpack.security.enabled=false`）

### 2.4 MinIO 特殊配置

- Console 端口：`9001`（映射到 `19001`）
- 启动命令：`server /data --console-address ":9001"`

## 3. 常用命令

```powershell
cd d:/Desktop/work/mall/project_mall_v3/infra

# 启动
docker compose -f docker-compose.local.yml up -d

# 查看状态
docker compose -f docker-compose.local.yml ps

# 查看服务列表
docker compose -f docker-compose.local.yml config --services

# 停止并清理
docker compose -f docker-compose.local.yml down --remove-orphans

# 停止并清除数据卷（慎用：不可恢复）
docker compose -f docker-compose.local.yml down --remove-orphans -v
```

也可通过项目脚本统一控制：

```powershell
cd d:/Desktop/work/mall/project_mall_v3
./scripts/start-v3.ps1        # 启动 infra + backend
./scripts/stop-v3.ps1         # 停止全部
./scripts/stop-v3.ps1 -KeepInfra  # 仅停应用，保留 infra
```

## 4. 健康检查

### 4.1 自动探针（Compose 内置）

| 服务 | 探针命令 | 间隔 | 超时 | 重试 |
|---|---|---|---|---|
| MySQL | `mysqladmin ping -h localhost` | 10s | 5s | 5 |
| Redis | `redis-cli ping` | 10s | 3s | 5 |
| RabbitMQ | `rabbitmq-diagnostics -q ping` | 30s | 10s | 5 |
| Elasticsearch | `curl -f http://localhost:9200/_cluster/health` | 30s | 10s | 5 |

MongoDB 和 MinIO 未配置 healthcheck。

### 4.2 手动验证命令

```powershell
# MySQL
docker exec mallv3-mysql mysqladmin ping -h localhost --silent

# Redis
docker exec mallv3-redis redis-cli ping

# Elasticsearch
curl http://localhost:9201/_cluster/health

# RabbitMQ（broker 探活，等价于 compose 健康检查）
docker exec mallv3-rabbitmq rabbitmq-diagnostics -q ping

# RabbitMQ（管理台页面验证）
curl http://localhost:15673

# MongoDB
docker exec mallv3-mongodb mongosh --eval "db.runCommand({ ping: 1 })"

# MinIO
curl http://localhost:19090/minio/health/live
```

## 5. 变更规则

1. 调整端口/镜像版本/账号后，同步更新：
   - `backend/*/application.yml`（连接配置）
   - `docs/04_release_runbook.md`（发布手册）
   - 根 `README.md` 端口表
   - `.github/workflows/ci.yml`（CI 依赖服务版本）
2. 对外约定端口（18080/18081/18082, 8090/8091）不要随意改动，改动需同步前端 SDK 与脚本。
3. 修改 Docker Compose 文件后，建议执行 `docker compose -f docker-compose.local.yml config --services` 做语法校验。

