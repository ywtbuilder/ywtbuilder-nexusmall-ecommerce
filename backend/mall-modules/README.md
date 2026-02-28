---
owner: backend-domain
updated: 2026-02-23
scope: mall-v3
audience: dev,qa
doc_type: guide
---

# 领域模块说明（mall-modules）

> 文档导航：统一入口见 [../../docs/README.md](../../docs/README.md)。

## 1. 目标

本文件聚焦 `backend/mall-modules/*` 领域层，回答以下问题：

1. 每个 `module-*` 的业务边界是什么。
2. 该模块依赖哪些存储/中间件。
3. App/Admin/Job 分别通过哪些模块组合业务能力。

## 2. 模块一览

| 模块 | 核心职责 | 数据存储/中间件 | 关键实现类 |
|---|---|---|---|
| `module-member` | 会员注册登录、地址、浏览记录、收藏、关注 | MySQL + MongoDB + Redis（验证码） | `MemberServiceImpl`, `MemberReadHistoryServiceImpl` |
| `module-product` | 商品/SPU/SKU、品牌、分类、属性 | MySQL | `ProductServiceImpl`, `BrandServiceImpl` |
| `module-cart` | 购物车增删改查 | MySQL | `CartServiceImpl` |
| `module-order` | 下单、订单状态流转、售后、后台订单操作 | MySQL + RabbitMQ（超时取消） | `PortalOrderServiceImpl`, `AdminOrderServiceImpl` |
| `module-marketing` | 优惠券、秒杀、首页广告/推荐/新品 | MySQL | `CouponServiceImpl`, `HomeAdvertiseServiceImpl` |
| `module-payment` | 支付日志与支付服务抽象（当前 mock） | MySQL | `MockPaymentServiceImpl` |
| `module-search` | ES 商品索引与检索 | Elasticsearch + RabbitMQ（同步消息） | `EsProductServiceImpl` |

## 3. BFF/Job 调用矩阵

| 调用方 | 使用的领域模块 |
|---|---|
| `mall-app-api` | member, product, cart, order, marketing, payment, search |
| `mall-admin-api` | member, product, order, marketing, search |
| `mall-job` | order, search, marketing |

说明：

1. 领域模块不直接暴露 HTTP 接口，HTTP Controller 在 BFF 层。
2. `module-search` 不直接依赖 `module-product`，MySQL -> ES 映射在 BFF 层（当前在 `mall-admin-api`）完成。

## 4. 当前实现特征与注意事项

1. `module-order` 的 `generateConfirmOrder` 当前是骨架实现，后续需要补完整的跨模块聚合逻辑。
2. `module-payment` 当前默认 Mock 支付，创建后会自动标记支付成功，适合联调但不代表真实支付网关语义。
3. `module-member` 中浏览记录/收藏/关注采用 MongoDB Repository，会员基础信息采用 MySQL Mapper，属于混合存储模型。
4. `module-search` 提供检索能力，App 侧已做 MySQL 回退，避免 ES 异常导致功能不可用。

## 5. 新增功能时的落地规则

1. 不在 BFF controller 写复杂领域逻辑，优先落到 `module-*/service`。
2. 新增表字段先加 `data/migration/V{next}__*.sql`，再改 `entity/mapper/service`。
3. 跨模块协作优先通过 BFF 聚合或事件驱动，不做“跨模块直接查表”。
4. 影响外部接口时，必须同步更新 `docs/02_api_contract.md` 与 `frontend/packages/api-sdk`。

## 6. 最小验证建议

```powershell
cd d:/Desktop/work/mall/project_mall_v3/backend
./mvnw.cmd test
./mvnw.cmd package -DskipTests
```

