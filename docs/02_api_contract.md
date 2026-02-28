---
owner: api
updated: 2026-02-26
scope: mall-v3
audience: dev,qa
doc_type: contract
---
# 02 - API 接口契约

> 文档导航：返回 [docs/README.md](README.md)。

## 1. 契约事实源

1. 后端 Controller 注解（`@RequestMapping` + `@GetMapping/@PostMapping`）是第一事实源。
2. OpenAPI 文档：
   - App: `http://localhost:18080/v3/api-docs`
   - Admin: `http://localhost:18081/v3/api-docs`
3. 本文以 2026-02-25 代码扫描结果为准：
   - App API：65 个端点
   - Admin API：154 个端点

## 2. App API 端点清单（`mall-app-api`，18080）

### 2.1 SSO（6）

| Method | Path |
|---|---|
| POST | `/sso/register` |
| POST | `/sso/login` |
| GET | `/sso/info` |
| GET | `/sso/getAuthCode` |
| POST | `/sso/updatePassword` |
| GET | `/sso/refreshToken` |

### 2.2 Home（6）

| Method | Path |
|---|---|
| GET | `/home/content` |
| GET | `/home/productCateList/{parentId}` |
| GET | `/home/recommendProductList` |
| GET | `/home/hotProductList` |
| GET | `/home/newProductList` |
| GET | `/home/subjectList` |

### 2.3 Product / Brand（6）

| Method | Path |
|---|---|
| GET | `/product/detail/{id}` |
| GET | `/product/categoryTreeList` |
| GET | `/product/search` |
| GET | `/brand/recommendList` |
| GET | `/brand/detail/{brandId}` |
| GET | `/brand/productList` |

说明：
1. `/product/search` 为 `ProductController` 的简单条件检索（MySQL 分页）。
2. 综合搜索走 `/search/product`（兼容路径 `/search/esProduct/search`），实现为 ES 优先、异常时回退 MySQL。

### 2.4 Cart（8）

| Method | Path |
|---|---|
| POST | `/cart/add` |
| GET | `/cart/list` |
| GET | `/cart/list/promotion` |
| GET | `/cart/update/quantity` |
| GET | `/cart/getProduct/{productId}` |
| POST | `/cart/update/attr` |
| POST | `/cart/delete` |
| POST | `/cart/clear` |

### 2.5 Order / Return / Payment（13）

| Method | Path |
|---|---|
| GET | `/order/confirm` |
| POST | `/order/generateOrder` |
| GET | `/order/list` |
| GET | `/order/detail/{orderId}` |
| POST | `/order/paySuccess` |
| POST | `/order/cancelUserOrder` |
| POST | `/order/confirmReceiveOrder` |
| POST | `/order/deleteOrder` |
| POST | `/order/cancelTimeOutOrder` |
| POST | `/returnApply/create` |
| POST | `/payment/create` |
| POST | `/payment/notify` |
| GET | `/payment/query` |

### 2.6 Member 行为（19）

| Method | Path |
|---|---|
| POST | `/member/readHistory/create` |
| POST | `/member/readHistory/delete` |
| POST | `/member/readHistory/clear` |
| GET | `/member/readHistory/list` |
| POST | `/member/productCollection/add` |
| POST | `/member/productCollection/delete` |
| GET | `/member/productCollection/list` |
| GET | `/member/productCollection/detail` |
| POST | `/member/productCollection/clear` |
| POST | `/member/attention/add` |
| POST | `/member/attention/delete` |
| GET | `/member/attention/list` |
| GET | `/member/attention/detail` |
| POST | `/member/attention/clear` |
| POST | `/member/coupon/add/{couponId}` |
| GET | `/member/coupon/list` |
| GET | `/member/coupon/list/cart/{type}` |
| GET | `/member/coupon/listHistory` |
| GET | `/member/address/list` |

### 2.7 Address / Search（5）

| Method | Path |
|---|---|
| GET | `/member/address/{id}` |
| POST | `/member/address/add` |
| POST | `/member/address/update/{id}` |
| POST | `/member/address/delete/{id}` |
| GET | `/search/product` |

说明：`/search/esProduct/search` 是 `SearchController` 同方法兼容路径，不单独计入端点统计。

### 2.8 Asset（1）

| Method | Path |
|---|---|
| GET | `/asset/image/{hash}` |

### 2.9 User Center（1）

| Method | Path |
|---|---|
| GET | `/user/center/summary` |

## 3. Admin API 端点清单（`mall-admin-api`，18081）

### 3.1 按 Tag 统计（全量 154）

| Tag | 数量 | 典型路径前缀 |
|---|---:|---|
| `UmsAdmin` | 12 | `/admin/*` |
| `UmsRole` | 10 | `/role/*` |
| `UmsMenu` | 7 | `/menu/*` |
| `UmsResource` | 6 | `/resource/*` |
| `UmsResourceCategory` | 4 | `/resourceCategory/*` |
| `UmsMemberLevel` | 1 | `/memberLevel/list` |
| `PmsProduct` | 10 | `/product/*` |
| `PmsBrand` | 9 | `/brand/*` |
| `PmsProductCategory` | 8 | `/productCategory/*` |
| `PmsProductAttribute` | 6 | `/productAttribute/*` |
| `PmsProductAttrCategory` | 5 | `/productAttribute/category/*` |
| `PmsSkuStock` | 2 | `/sku/*` |
| `OmsOrder` | 8 | `/order/*` |
| `OmsOrderReturnApply` | 4 | `/returnApply/*` |
| `OmsOrderReturnReason` | 6 | `/returnReason/*` |
| `OmsOrderSetting` | 2 | `/orderSetting/*` |
| `OmsCompanyAddress` | 1 | `/companyAddress/list` |
| `SmsCoupon` | 5 | `/coupon/*` |
| `SmsFlashPromotion` | 6 | `/flash/*` |
| `SmsFlashPromotionSession` | 6 | `/flashSession/*` |
| `SmsFlashPromotionProductRelation` | 4 | `/flashProductRelation/*` |
| `SmsHomeAdvertise` | 6 | `/home/advertise/*` |
| `SmsHomeBrand` | 5 | `/home/brand/*` |
| `SmsHomeNewProduct` | 5 | `/home/newProduct/*` |
| `SmsHomeRecommendProduct` | 5 | `/home/recommendProduct/*` |
| `SmsHomeRecommendSubject` | 5 | `/home/recommendSubject/*` |
| `EsProduct` | 4 | `/esProduct/*` |
| `MinIOUpload` | 2 | `/minio/*` |

### 3.2 高风险路径（建议回归优先）

| 模块 | 路径 |
|---|---|
| RBAC | `/admin/login`, `/admin/logout`, `/admin/refreshToken`, `/resource/*`, `/role/*` |
| 商品 | `/product/*`, `/brand/*`, `/productCategory/*`, `/sku/*` |
| 订单 | `/order/*`, `/returnApply/*`, `/returnReason/*` |
| 营销 | `/coupon/*`, `/flash*`, `/home/*` |
| 搜索索引 | `/esProduct/importAll`, `/esProduct/create/{id}`, `/esProduct/delete/{id}` |

## 4. 当前已知契约差异（代码现状）

1. `MemberCoupon` 后端当前没有 `/member/coupon/listByProduct/{productId}`，但 App SDK 仍保留该方法。
2. Admin SDK 中部分路径仍与后端注解不一致（尤其 `{id}` 路径变量位置），应按 Controller 契约修正后再发布。
3. `scripts/run-tests.ps1` 的独立 Java 脚本仍引用历史路径（如 `/product/list`、`/brand/list`、`/search/simple`），与当前契约不一致。

## 5. 维护规则

1. Controller 注解改动后，本文必须同日更新。
2. SDK 改动必须以本文和 OpenAPI 为基准，不允许“SDK 先行定义”。
3. PR 合并前至少执行一次契约抽样验证（登录、商品、订单、搜索、权限）。
