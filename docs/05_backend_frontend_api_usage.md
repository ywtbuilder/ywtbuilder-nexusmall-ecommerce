---
owner: app-stack
updated: 2026-02-26
scope: mall-v3
audience: dev,qa
doc_type: analysis
---
# 05 - 后端接口与前端使用对照（mall-app-web）

> 文档导航：返回 [docs/README.md](README.md)。

基线说明（2026-02-25）：

1. 后端来源：`backend/mall-app-api/src/main/java/com/mall/app/controller/*.java`、`backend/mall-admin-api/src/main/java/com/mall/admin/controller/*.java`
2. SDK 来源：`frontend/packages/api-sdk/src/app/*.ts`
3. 前端使用来源：`frontend/apps/mall-app-web/src/views/*.vue`
4. 本文“使用”定义：仅统计 `mall-app-web` 页面层实际调用，不包含 `mall-admin-web`

## 1. 总览结论

| 指标 | 数值 |
|---|---:|
| 后端接口总数（App + Admin） | 219 |
| App 后端接口数（mall-app-api） | 65 |
| Admin 后端接口数（mall-admin-api） | 154 |
| App SDK 已封装动作数（`api-sdk/src/app`） | 58 |
| mall-app-web 实际调用动作数（唯一） | 38 |
| mall-app-web 实际落到 App 后端接口数（唯一） | 38 |
| App 后端未被 mall-app-web 使用接口数 | 27 |
| 后端有但 App SDK 未封装接口数 | 8 |
| App SDK 存在但后端无对应接口数 | 1 |

## 2. 前端功能入口（mall-app-web 路由）

说明：当前路由条目 19 个，对应视图文件 18 个（`/jd-item` 与 `/jd-item/:sku` 复用同一视图）。

| 路由 | 视图文件 | 需登录 | 说明 |
|---|---|---|---|
| `/` | `frontend/apps/mall-app-web/src/views/HomeView.vue` | 否 | 首页聚合 |
| `/login` | `frontend/apps/mall-app-web/src/views/LoginView.vue` | 否 | 会员登录 |
| `/register` | `frontend/apps/mall-app-web/src/views/RegisterView.vue` | 否 | 会员注册 |
| `/category` | `frontend/apps/mall-app-web/src/views/CategoryView.vue` | 否 | 分类浏览 |
| `/search` | `frontend/apps/mall-app-web/src/views/SearchView.vue` | 否 | 商品搜索 |
| `/product/:id` | `frontend/apps/mall-app-web/src/views/ProductDetailView.vue` | 否 | 商品详情 |
| `/jd-item` | `frontend/apps/mall-app-web/src/views/JdItemView.vue` | 否 | JD 离线镜像预览 |
| `/jd-item/:sku` | `frontend/apps/mall-app-web/src/views/JdItemView.vue` | 否 | 指定 SKU 离线镜像预览 |
| `/upcoming` | `frontend/apps/mall-app-web/src/views/UpcomingView.vue` | 否 | 即将上架页面 |
| `/cart` | `frontend/apps/mall-app-web/src/views/CartView.vue` | 是 | 购物车 |
| `/order/confirm` | `frontend/apps/mall-app-web/src/views/OrderConfirmView.vue` | 是 | 确认下单 |
| `/order/list` | `frontend/apps/mall-app-web/src/views/OrderListView.vue` | 是 | 订单列表 |
| `/order/detail/:id` | `frontend/apps/mall-app-web/src/views/OrderDetailView.vue` | 是 | 订单详情 |
| `/mine` | `frontend/apps/mall-app-web/src/views/MineView.vue` | 是 | 个人中心 |
| `/address` | `frontend/apps/mall-app-web/src/views/AddressView.vue` | 是 | 收货地址 |
| `/collection` | `frontend/apps/mall-app-web/src/views/CollectionView.vue` | 是 | 商品收藏 |
| `/readHistory` | `frontend/apps/mall-app-web/src/views/ReadHistoryView.vue` | 是 | 浏览记录 |
| `/attention` | `frontend/apps/mall-app-web/src/views/AttentionView.vue` | 是 | 品牌关注 |
| `/coupon` | `frontend/apps/mall-app-web/src/views/CouponView.vue` | 是 | 我的优惠券 |

## 3. 前端实际使用的接口（38 条）

| 前端调用 | HTTP | 后端路径 | 调用页面 |
|---|---|---|---|
| `appAuthApi.getAuthCode` | GET | `/sso/getAuthCode` | `RegisterView.vue` |
| `appAuthApi.info` | GET | `/sso/info` | `MineView.vue` |
| `appAuthApi.login` | POST | `/sso/login` | `LoginView.vue` |
| `appAuthApi.register` | POST | `/sso/register` | `RegisterView.vue` |
| `appProductApi.detail` | GET | `/product/detail/{id}` | `ProductDetailView.vue` |
| `appProductApi.search` | GET | `/product/search` | `HomeView.vue`、`CategoryView.vue` |
| `cartApi.add` | POST | `/cart/add` | `ProductDetailView.vue` |
| `cartApi.clear` | POST | `/cart/clear` | `CartView.vue` |
| `cartApi.delete` | POST | `/cart/delete` | `CartView.vue` |
| `cartApi.list` | GET | `/cart/list` | `CartView.vue` |
| `cartApi.updateQuantity` | GET | `/cart/update/quantity` | `CartView.vue` |
| `homeApi.content` | GET | `/home/content` | `HomeView.vue` |
| `homeApi.productCateList` | GET | `/home/productCateList/{parentId}` | `CategoryView.vue` |
| `memberAddressApi.add` | POST | `/member/address/add` | `AddressView.vue` |
| `memberAddressApi.delete` | POST | `/member/address/delete/{id}` | `AddressView.vue` |
| `memberAddressApi.list` | GET | `/member/address/list` | `AddressView.vue`、`ProductDetailView.vue` |
| `memberAddressApi.update` | POST | `/member/address/update/{id}` | `AddressView.vue` |
| `memberAttentionApi.delete` | POST | `/member/attention/delete` | `AttentionView.vue` |
| `memberAttentionApi.list` | GET | `/member/attention/list` | `AttentionView.vue` |
| `memberCollectionApi.add` | POST | `/member/productCollection/add` | `ProductDetailView.vue` |
| `memberCollectionApi.delete` | POST | `/member/productCollection/delete` | `CollectionView.vue`、`ProductDetailView.vue` |
| `memberCollectionApi.detail` | GET | `/member/productCollection/detail` | `ProductDetailView.vue` |
| `memberCollectionApi.list` | GET | `/member/productCollection/list` | `CollectionView.vue` |
| `memberCouponApi.list` | GET | `/member/coupon/list` | `CouponView.vue` |
| `memberReadHistoryApi.clear` | POST | `/member/readHistory/clear` | `ReadHistoryView.vue` |
| `memberReadHistoryApi.create` | POST | `/member/readHistory/create` | `ProductDetailView.vue` |
| `memberReadHistoryApi.delete` | POST | `/member/readHistory/delete` | `ReadHistoryView.vue` |
| `memberReadHistoryApi.list` | GET | `/member/readHistory/list` | `ReadHistoryView.vue` |
| `portalOrderApi.cancelUserOrder` | POST | `/order/cancelUserOrder` | `OrderListView.vue`、`OrderDetailView.vue` |
| `portalOrderApi.confirmReceiveOrder` | POST | `/order/confirmReceiveOrder` | `OrderListView.vue`、`OrderDetailView.vue` |
| `portalOrderApi.deleteOrder` | POST | `/order/deleteOrder` | `OrderListView.vue`、`OrderDetailView.vue` |
| `portalOrderApi.detail` | GET | `/order/detail/{orderId}` | `OrderDetailView.vue` |
| `portalOrderApi.generateConfirmOrder` | GET | `/order/confirm` | `OrderConfirmView.vue` |
| `portalOrderApi.generateOrder` | POST | `/order/generateOrder` | `OrderConfirmView.vue` |
| `portalOrderApi.list` | GET | `/order/list` | `OrderListView.vue` |
| `portalOrderApi.paySuccess` | POST | `/order/paySuccess` | `OrderListView.vue`、`OrderDetailView.vue` |
| `searchApi.search` | GET | `/search/product` | `SearchView.vue` |
| `userCenterApi.summary` | GET | `/user/center/summary` | `MineView.vue` |

## 4. App 后端未被 mall-app-web 使用的接口（27 条）

| HTTP | 路径 |
|---|---|
| GET | `/brand/detail/{brandId}` |
| GET | `/brand/productList` |
| GET | `/brand/recommendList` |
| GET | `/cart/getProduct/{productId}` |
| GET | `/cart/list/promotion` |
| POST | `/cart/update/attr` |
| GET | `/home/hotProductList` |
| GET | `/home/newProductList` |
| GET | `/home/recommendProductList` |
| GET | `/home/subjectList` |
| GET | `/member/address/{id}` |
| POST | `/member/attention/add` |
| POST | `/member/attention/clear` |
| GET | `/member/attention/detail` |
| POST | `/member/coupon/add/{couponId}` |
| GET | `/member/coupon/list/cart/{type}` |
| GET | `/member/coupon/listHistory` |
| POST | `/member/productCollection/clear` |
| POST | `/order/cancelTimeOutOrder` |
| POST | `/payment/create` |
| POST | `/payment/notify` |
| GET | `/payment/query` |
| GET | `/product/categoryTreeList` |
| POST | `/returnApply/create` |
| GET | `/asset/image/{hash}` |
| GET | `/sso/refreshToken` |
| POST | `/sso/updatePassword` |

## 5. SDK 与后端契约偏差

### 5.1 后端存在但 App SDK 未封装（8 条）

1. `GET /brand/detail/{brandId}`
2. `GET /brand/productList`
3. `GET /brand/recommendList`
4. `POST /member/attention/clear`
5. `GET /member/coupon/listHistory`
6. `POST /member/productCollection/clear`
7. `POST /order/cancelTimeOutOrder`
8. `GET /asset/image/{hash}`

### 5.2 App SDK 存在但后端无对应接口（1 条）

1. `GET /member/coupon/listByProduct/{productId}`（`memberCouponApi.listByProduct`）

## 6. 维护规则（避免再次失真）

1. 页面调用变化时，先改 `frontend/apps/mall-app-web/src/views/*.vue`，再同步更新本文第 3 节。
2. App 控制器新增/删除路由时，同步检查第 4 节和第 5 节。
3. 接口签名变更时，先改 `frontend/packages/api-sdk/src/app/*.ts`，再更新 `docs/02_api_contract.md` 与本文。
4. 为减少噪声，本文不再记录易漂移的“具体行号”，仅保留文件级定位。
