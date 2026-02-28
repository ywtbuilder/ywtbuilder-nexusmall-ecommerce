---
owner: docs
updated: 2026-02-27
scope: mall-v3
audience: dev,product,ops,audit
doc_type: business-context
---

# 系统上下文图（System Context）

## 1. 目标

说明 Mall V3 的核心参与者、系统边界、关键依赖与高层交互路径。

## 2. 上下文图

```mermaid
flowchart LR
  U[用户] --> APP[mall-app-web]
  A[管理员] --> ADM[mall-admin-web]

  APP --> SDK[@mall/api-sdk]
  ADM --> SDKA[@mall/api-sdk-admin]

  SDK --> APPAPI[mall-app-api]
  SDKA --> ADMINAPI[mall-admin-api]

  APPAPI --> MOD[mall-modules]
  ADMINAPI --> MOD

  MOD --> MYSQL[(MySQL)]
  MOD --> REDIS[(Redis)]
  MOD --> ES[(Elasticsearch)]
  MOD --> MQ[(RabbitMQ)]
  MOD --> MONGO[(MongoDB 可选)]

  APPAPI --> ASSET[/asset/image/{hash}]
  ASSET --> U
```

## 3. 边界说明

- 前端边界：`frontend/apps/*` 负责页面与交互，不承载领域规则。
- API 边界：`mall-app-api`/`mall-admin-api` 负责协议编排与鉴权入口。
- 领域边界：`backend/mall-modules/module-*` 承载业务规则与事务。
- 数据边界：MySQL 为主存，Redis/ES/MQ 为能力扩展。

## 4. 事实来源

- [`../../00_architecture.md`](../../00_architecture.md)
- [`../../02_api_contract.md`](../../02_api_contract.md)
- [`../../../backend/mall-modules`](../../../backend/mall-modules)
- [`../../../infra/docker-compose.local.yml`](../../../infra/docker-compose.local.yml)

## 5. 已验证项

1. 前后台分离架构与端口职责边界已在项目文档中明确。
2. 核心中间件依赖顺序（MySQL/Redis/ES/RabbitMQ）已有明确运行约束。

## 6. 待补项

1. 第三方支付网关目标态上下文图（当前偏 Mock 语义）。
2. 审计日志跨服务追踪链路图。