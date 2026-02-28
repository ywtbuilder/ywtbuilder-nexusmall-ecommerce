---
owner: docs
updated: 2026-02-27
scope: mall-v3
audience: dev,qa,audit
doc_type: data-flow
---

# 数据流映射：API → DB → UI

## 1. 目标

建立“接口字段到数据库字段再到页面消费点”的可追溯映射，降低联调与审计成本。

## 2. 样例映射（首版）

| API | 响应字段 | DB 来源 | UI 消费点 |
|---|---|---|---|
| `GET /home/content` | `advertiseList[].pic` | `sms_home_advertise.pic` | `HomeView.vue` 轮播图 |
| `GET /home/content` | `recommendProductList[].name` | `pms_product.name` | `HomeView.vue` 推荐区 |
| `GET /product/detail/{id}` | `product.pic` | `pms_product.pic` | `ProductDetailView.vue` 主图 |
| `GET /product/detail/{id}` | `skuStockList[].price` | `pms_sku_stock.price` | `ProductDetailView.vue` SKU价格 |
| `GET /cart/list/promotion` | `cartPromotionItemList` | `oms_cart_item` + 营销规则 | `CartView.vue` |

## 3. 审核规则

1. 每条映射必须有代码消费点与 SQL 来源。
2. 关键业务字段（价格、库存、状态）必须标记“权威来源”。
3. 若存在降级逻辑（如 ES → MySQL），必须标注回退条件。

## 4. 待补项

1. 管理端页面字段映射矩阵。
2. 订单与支付字段的全链路追踪表。

## 5. 四大主链路页面级映射（实测代码口径）

### 5.1 首页链路（Home）

| 页面动作 | SDK 调用 | API 路径 | 后端入口 | 主要表 |
|---|---|---|---|---|
| 首屏加载轮播 | `homeApi.contentLite()` | `GET /home/content-lite` | `HomeController#contentLite` | `sms_home_advertise` |
| 延迟加载推荐区 | `homeApi.content()` | `GET /home/content` | `HomeController#content` | `sms_home_advertise`、`sms_home_new_product`、`sms_home_recommend_product`、`pms_product`、`pms_brand` |

### 5.2 搜索链路（Search）

| 页面动作 | SDK 调用 | API 路径 | 后端入口 | 主要表 |
|---|---|---|---|---|
| 关键词/分类搜索 | `searchApi.search(params)` | `GET /search/product` | `SearchController#search` | `pms_product`（ES 可选回退路径） |
| 列表图片加载 | `toSearchThumbUrl(pic)` | `GET /asset/image/{hash}?variant=search` | `AssetController#image` | `pms_asset` |

### 5.3 购物车链路（Cart）

| 页面动作 | SDK 调用 | API 路径 | 后端入口 | 主要表 |
|---|---|---|---|---|
| 详情页加入购物车 | `cartApi.add(payload)` | `POST /cart/add` | `CartController#add` | `oms_cart_item`、`pms_product`、`pms_sku_stock` |
| 购物车页拉取列表 | `cartApi.list()` | `GET /cart/list` | `CartController#list` | `oms_cart_item` |
| 修改数量 | `cartApi.updateQuantity(id,qty)` | `GET /cart/update/quantity` | `CartController#updateQuantity` | `oms_cart_item` |
| 删除商品 | `cartApi.delete(ids)` | `POST /cart/delete` | `CartController#delete` | `oms_cart_item` |
| 清空购物车 | `cartApi.clear()` | `POST /cart/clear` | `CartController#clear` | `oms_cart_item` |

### 5.4 订单链路（Order）

| 页面动作 | SDK 调用 | API 路径 | 后端入口 | 主要表 |
|---|---|---|---|---|
| 结算页生成确认单 | `portalOrderApi.generateConfirmOrder(cartIds)` | `GET /order/confirm` | `OrderController#confirm` | `oms_cart_item`、`ums_member_receive_address` |
| 提交订单 | `portalOrderApi.generateOrder(payload)` | `POST /order/generateOrder` | `OrderController#generateOrder` | `oms_order`、`oms_order_item` |
| 订单列表 | `portalOrderApi.list(params)` | `GET /order/list` | `OrderController#list` | `oms_order`、`oms_order_item` |
| 订单详情 | `portalOrderApi.detail(orderId)` | `GET /order/detail/{orderId}` | `OrderController#detail` | `oms_order`、`oms_order_item` |
| 支付成功回写 | `portalOrderApi.paySuccess(orderId,payType)` | `POST /order/paySuccess` | `OrderController#paySuccess` | `oms_order` |
| 取消/收货/删除 | `cancelUserOrder` / `confirmReceiveOrder` / `deleteOrder` | `POST /order/*` | `OrderController#cancelUserOrder/#confirmReceiveOrder/#deleteOrder` | `oms_order` |

## 6. 事实来源（本次新增）

- 前端页面：`frontend/apps/mall-app-web/src/views/{HomeView,SearchView,ProductDetailView,CartView,OrderConfirmView,OrderListView}.vue`
- SDK：`frontend/packages/api-sdk/src/app/{home,search,cart,order,product}.ts`
- 后端入口：`backend/mall-app-api/src/main/java/com/mall/app/controller/{HomeController,SearchController,CartController,OrderController,ProductController}.java`