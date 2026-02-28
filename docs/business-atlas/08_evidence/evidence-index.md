---
owner: docs
updated: 2026-02-27
scope: mall-v3
audience: dev,qa,ops,audit
doc_type: evidence-index
---

# 证据索引（Evidence Index）

## 1. 证据分层

1. 代码证据：controller/service/mapper/vue/sdk 真实路径
2. 数据证据：`data/migration` 与 `data/seed`
3. 运行证据：`runtime-logs/*.json|*.md`
4. 测试证据：`tests/**` 与脚本执行记录
5. 文档证据：`docs/02`、`05`、`07`、`14`

## 2. 首批证据入口

- 架构：[`../../00_architecture.md`](../../00_architecture.md)
- API 契约：[`../../02_api_contract.md`](../../02_api_contract.md)
- 前后端映射：[`../../05_backend_frontend_api_usage.md`](../../05_backend_frontend_api_usage.md)
- 模块缺口：[`../../07_module_inventory_and_doc_gap.md`](../../07_module_inventory_and_doc_gap.md)
- 文档同步：[`../../14_doc_sync_system.md`](../../14_doc_sync_system.md)
- 运行日志目录：[`../../../runtime-logs`](../../../runtime-logs)
- 数据目录：[`../../../data`](../../../data)
- 测试目录：[`../../../tests`](../../../tests)

## 3. 证据引用规范

- 引用业务结论时，至少给出一个“代码证据 + 数据或运行证据”。
- 若结论与线上表现相关，必须附带最近一次回归证据。
- 若结论仅为“设计目标”，必须显式标注“未实测”。

## 4. 待补项

1. 建立“结论 ID → 证据 ID”索引表。
2. 建立证据时效性标记（过期阈值）。

## 5. 本轮新增证据锚点（任务 108）

### 5.1 用户端页面证据

- `frontend/apps/mall-app-web/src/views/HomeView.vue`
- `frontend/apps/mall-app-web/src/views/SearchView.vue`
- `frontend/apps/mall-app-web/src/views/ProductDetailView.vue`
- `frontend/apps/mall-app-web/src/views/CartView.vue`
- `frontend/apps/mall-app-web/src/views/OrderConfirmView.vue`
- `frontend/apps/mall-app-web/src/views/OrderListView.vue`

### 5.2 SDK 证据

- `frontend/packages/api-sdk/src/app/home.ts`
- `frontend/packages/api-sdk/src/app/search.ts`
- `frontend/packages/api-sdk/src/app/cart.ts`
- `frontend/packages/api-sdk/src/app/order.ts`
- `frontend/packages/api-sdk/src/app/product.ts`
- `frontend/packages/api-sdk/src/admin/product.ts`

### 5.3 后端入口证据

- `backend/mall-app-api/src/main/java/com/mall/app/controller/HomeController.java`
- `backend/mall-app-api/src/main/java/com/mall/app/controller/SearchController.java`
- `backend/mall-app-api/src/main/java/com/mall/app/controller/ProductController.java`
- `backend/mall-app-api/src/main/java/com/mall/app/controller/CartController.java`
- `backend/mall-app-api/src/main/java/com/mall/app/controller/OrderController.java`