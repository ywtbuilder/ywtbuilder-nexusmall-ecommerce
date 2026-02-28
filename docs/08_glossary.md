---
owner: docs
updated: 2026-02-23
scope: mall-v3
audience: all
doc_type: reference
---
# 08 - 术语表（Glossary）

> 文档导航：返回 [docs/README.md](README.md)。

## 1. 业务术语

| 术语 | 定义 | 所属模块 |
|---|---|---|
| SPU | 商品抽象单位，不区分具体规格 | `module-product` |
| SKU | SPU 的规格库存单位（颜色/容量等） | `module-product` |
| 购物车项 | 用户待购明细，按 SKU 管理 | `module-cart` |
| 订单 | 下单后的交易主单，含状态流转 | `module-order` |
| 退货申请 | 用户发起售后申请，后台审核 | `module-order` |
| 优惠券 | 营销促销凭证，支持门槛与有效期 | `module-marketing` |
| 秒杀 | 限时活动，含场次与商品关系 | `module-marketing` |
| 浏览记录/收藏/关注 | 会员行为数据（三类） | `module-member` |
| 支付日志 | 订单支付流水（当前为 Mock 链路） | `module-payment` |
| 搜索商品（EsProduct） | ES 索引商品模型，C 端检索优先查 ES，失败时回退 MySQL | `module-search` |

## 2. 架构术语

| 术语 | 定义 |
|---|---|
| BFF | 面向前端的后端聚合层（`mall-app-api` / `mall-admin-api`） |
| Job Service | 异步任务服务（`mall-job`），负责 MQ 消费与后台任务 |
| Shared | 公共层（`shared-common/security/web/test`） |
| Domain Module | 领域层（`module-*`） |
| API SDK | 前端统一请求封装（`frontend/packages/api-sdk`） |
| Monorepo | 单仓多包（本项目前端使用 pnpm workspace） |

## 3. 中间件术语

| 术语 | 本项目用途 |
|---|---|
| MySQL | 主业务库（商品、订单、营销、后台用户） |
| Redis | 验证码、Token 黑名单（含管理员登出失效） |
| MongoDB | 会员行为数据 |
| Elasticsearch | 商品检索索引 |
| RabbitMQ | 异步消息（订单超时取消、商品 ES 同步） |
| MinIO | 后台上传文件对象存储（`/minio/upload`） |
| Flyway | 迁移脚本命名沿用 Flyway（V1/V2...），当前应用默认未启用自动迁移 |

## 4. 安全术语

| 术语 | 定义 |
|---|---|
| JWT | 无状态认证令牌 |
| Token 黑名单 | Admin 端 `/admin/logout` 会写 Redis 黑名单；App 端当前以前端清理 token 为主 |
| RBAC | 基于角色的访问控制（Admin 端动态资源匹配） |
| Bearer Token | `Authorization: Bearer <token>` |

## 5. 表前缀约定

| 前缀 | 含义 |
|---|---|
| `pms_` | 商品域 |
| `oms_` | 订单/支付域 |
| `sms_` | 营销域 |
| `ums_` | 用户/权限域 |

## 6. 项目内专有命名说明

| 名称 | 说明 |
|---|---|
| `recommand_status` | 历史拼写（应为 `recommend`），数据库与代码保持该字段名 |
| `mallv3-*` | Docker 容器统一前缀 |
| `/sso/*` | C 端会员认证接口路径前缀（历史命名，不代表跨系统单点登录） |
| `runtime-logs/*` | 本地运行产物目录，不是配置事实源 |
| `mall-job*.log` | 异步消费者日志（订单取消、ES 同步排障入口） |

## 7. 维护规则

1. 新术语首次进入代码时，同步补充本表。
2. 同一概念只保留一个主名称，避免跨文档同义词混用。
3. 涉及表字段历史兼容（如拼写）时，必须在术语表显式标注。
