---
owner: docs
updated: 2026-02-27
scope: mall-v3
audience: dev,ops,audit
doc_type: admin-linkage
---

# 后台管理链路（页面 → 接口 → 后端 → 落库）

## 1. 目标

补齐 Admin 端“页面调用到后端实现再到数据库落地”的全链路映射，作为业务审查与变更评估基线。

## 2. 映射模板（首版）

| 管理端页面 | API | 后端入口 | 主要表 |
|---|---|---|---|
| 商品管理 | `GET/POST /admin/product/*` | `mall-admin-api` 对应 controller | `pms_product`、`pms_sku_stock` |
| 分类管理 | `GET/POST /admin/productCategory/*` | Admin 分类 controller | `pms_product_category` |
| 品牌管理 | `GET/POST /admin/brand/*` | Admin 品牌 controller | `pms_brand` |
| 首页广告管理 | `GET/POST /admin/home/advertise/*` | Admin 营销 controller | `sms_home_advertise` |
| 订单管理 | `GET/POST /admin/order/*` | Admin 订单 controller | `oms_order`、`oms_order_item` |

## 3. 校验要求

1. 每条页面动作必须能追溯到明确 controller 方法。
2. 每条 controller 必须给出至少一个真实落库表。
3. 用户端可见字段变化必须建立“后台动作 → 前台页面”反向验证链。

## 4. 待补项

1. 补齐每个页面的“按钮级动作”与具体端点。
2. 补齐权限点（菜单/按钮）与 RBAC 角色矩阵。

## 5. 管理端商品页（按钮级）链路样例

> 页面来源：`frontend/apps/mall-admin-web/src/views/pms/ProductListView.vue`

| 页面动作 | SDK 调用 | API 路径 | 后端入口（命名约定） | 主要表 |
|---|---|---|---|---|
| 列表查询 | `productApi.list(query)` | `GET /admin/product/list` | `PmsProductController#list` | `pms_product` |
| 新增商品 | `productApi.create(form)` | `POST /admin/product/create` | `PmsProductController#create` | `pms_product`、`pms_sku_stock` |
| 编辑商品 | `productApi.update(id,form)` | `POST /admin/product/update/{id}` | `PmsProductController#update` | `pms_product`、`pms_sku_stock` |
| 逻辑删除 | `productApi.updateDeleteStatus([id],1)` | `POST /admin/product/update/deleteStatus` | `PmsProductController#updateDeleteStatus` | `pms_product` |
| 上下架 | `productApi.updatePublishStatus([id],status)` | `POST /admin/product/update/publishStatus` | `PmsProductController#updatePublishStatus` | `pms_product` |
| 推荐开关 | `productApi.updateRecommendStatus([id],status)` | `POST /admin/product/update/recommendStatus` | `PmsProductController#updateRecommendStatus` | `pms_product` |

## 6. 前后台联动校验（商品域）

1. Admin 修改 `publishStatus` 后，用户端 `GET /product/search` 与 `GET /search/product` 只应返回 `publish_status=1`。
2. Admin 修改 `pic`/详情资源后，用户端详情页应通过 `/api/asset/image/{hash}` 正常渲染。
3. Admin 逻辑删除后，用户端首页推荐与搜索结果不应继续出现该商品。