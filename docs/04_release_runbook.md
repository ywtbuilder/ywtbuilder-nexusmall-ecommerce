---
owner: ops
updated: 2026-02-25
scope: mall-v3
audience: dev,ops
doc_type: runbook
---
# 04 - 发布运行手册

> 文档导航：返回 [docs/README.md](README.md)。

## 1. 适用范围

本手册对应当前仓库 `project_mall_v3` 的实际运行方式：  
以 `scripts/*.ps1` 和 `infra/docker-compose.local.yml` 为准，覆盖本地联调、提测、回退。

说明：

1. 仓库中已落地的是 `local` 环境和本地测试流程。
2. `staging/production` 部署编排（如 K8s/Helm/Nginx upstream）不在本仓库内维护。
3. 基础设施依赖建议按 `MySQL -> Redis -> Elasticsearch -> RabbitMQ` 顺序确认可用后再启动后端。

## 2. 当前真实环境矩阵

### 2.1 应用端口

| 服务 | 端口 | 健康检查 |
|---|---:|---|
| mall-app-api | 18080 | `http://localhost:18080/actuator/health` |
| mall-admin-api | 18081 | `http://localhost:18081/actuator/health` |
| mall-job | 18082 | `http://localhost:18082/actuator/health` |
| mall-app-web | 8091 | `http://localhost:8091` |
| mall-admin-web | 8090 | `http://localhost:8090` |

### 2.2 基础设施端口（Docker Compose）

| 组件 | 端口 |
|---|---|
| MySQL | 13306 |
| Redis | 16379 |
| MongoDB | 27018 |
| RabbitMQ | 5673 / 15673 |
| Elasticsearch | 9201 / 9301 |
| MinIO | 19090 / 19001 |

## 3. 上线/提测前检查清单（按仓库现状）

- [ ] `pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\preflight-v3.ps1` 通过（Java / Docker / 端口检查）
- [ ] `pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-v3.ps1 -Frontend` 启动成功
- [ ] 3 个后端健康检查均返回 `UP`
- [ ] `pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\init-db.ps1` 执行完成（首次环境或重置后必做）
- [ ] `backend` 模块测试与编译通过（`mvnw test` / `mvnw compile`）
- [ ] `pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-api-v3.ps1` 通过（当前推荐 API 冒烟门禁）
- [ ] `scripts/run-tests.ps1` 仅作为历史补充脚本（当前包含历史接口路径，不能作为发布门禁）
- [ ] 关键页面冒烟通过（登录、首页、商品详情、购物车、下单）
- [ ] 发布涉及文档已同步（至少 `02/04/05`）

## 4. 标准发布步骤（本地/测试环境）

### 4.1 启动

```powershell
cd d:/Desktop/work/mall/project_mall_v3
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\preflight-v3.ps1
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-v3.ps1 -Frontend
```

### 4.2 初始化数据（按需）

```powershell
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\init-db.ps1
```

如需重建库：

```powershell
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\init-db.ps1 -Reset
```

### 4.3 健康检查

```powershell
curl http://localhost:18080/actuator/health
curl http://localhost:18081/actuator/health
curl http://localhost:18082/actuator/health
```

### 4.4 回归测试

```powershell
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-api-v3.ps1
```

可选补充（历史脚本，非发布门禁）：

```powershell
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-tests.ps1
```

> 注意：`run-tests.ps1` 当前仍包含历史接口路径（`/product/list`、`/brand/list`、`/search/simple`），只能作为补充检查；发布验收以 `smoke-api-v3.ps1 + backend 模块测试` 为准。

## 5. 回滚与恢复

### 5.1 应用层回滚（推荐）

1. 回退代码到目标提交（由发布负责人执行）。
2. 停服务：`pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\stop-v3.ps1`
3. 重启服务：`pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-v3.ps1 -Frontend`
4. 重新做健康检查和关键路径验证。

### 5.2 数据层回滚（当前策略）

当前仓库未提供自动数据库回滚脚本。  
若迁移造成问题，按以下顺序处理：

1. 先执行应用层回滚。
2. 使用人工回滚 SQL（按具体变更单执行）。
3. 必要时在测试环境使用 `pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\init-db.ps1 -Reset` 重建验证。

## 6. 运行监控与日志

### 6.1 当前可用检查项

1. Actuator：`/actuator/health`、`/actuator/info`
2. 容器状态：`docker compose -f infra/docker-compose.local.yml ps`
3. 关键日志目录：`runtime-logs/`

### 6.2 常用日志文件

1. `runtime-logs/mall-app-api.log`
2. `runtime-logs/mall-admin-api.log`
3. `runtime-logs/mall-job.log`
4. `runtime-logs/mall-app-web.log`
5. `runtime-logs/mall-admin-web.log`

### 6.3 快速查看日志

```powershell
Get-Content ./runtime-logs/mall-app-api.log -Tail 200
Get-Content ./runtime-logs/mall-admin-api.log -Tail 200
Get-Content ./runtime-logs/mall-job.log -Tail 200
```

## 7. 故障处理速查

### 7.1 端口冲突

```powershell
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\stop-v3.ps1 -ForcePortKill
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-v3.ps1 -Frontend
```

### 7.2 仅重启应用，不停中间件

```powershell
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\stop-v3.ps1 -KeepInfra
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-v3.ps1 -SkipInfra -Frontend
```

### 7.3 Docker 异常

```powershell
cd d:/Desktop/work/mall/project_mall_v3/infra
docker compose -f docker-compose.local.yml down --remove-orphans
docker compose -f docker-compose.local.yml up -d
```

## 8. 与仓库现状不一致项说明

以下能力当前仓库未落地，不应作为发布必选项写入流程：

1. `mvn flyway:validate`（当前迁移通过 `scripts/init-db.ps1` 执行 SQL）
2. Prometheus 指标采集（当前仅暴露 `health/info`）
3. staging/production 端口与流量切换脚本（仓库未包含）
4. `scripts/run-tests.ps1` 仍为历史接口路径脚本，需重写后才能恢复“强制门禁”角色
