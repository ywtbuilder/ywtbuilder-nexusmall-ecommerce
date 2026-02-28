---
owner: frontend
updated: 2026-02-26
scope: mall-v3
audience: dev,qa
doc_type: guide
---

# Frontend 模块总览（Mall V3）

> 文档导航：统一入口见 [../docs/README.md](../docs/README.md)。

## 1. 实测结论（2026-02-25）

1. `frontend/` 是 pnpm workspace，包含 `mall-app-web`、`mall-admin-web`、`e2e`、`@mall/api-sdk` 四个子模块。
2. App/Admin 两侧当前仅保留 `src/main.ts` 与 `src/router/index.ts` 作为入口事实源。
3. Admin 侧当前仅保留 `vite.config.ts`，实测 Vite 读取的是该文件。
4. `@mall/api-sdk` 默认直连 `http://localhost:18080`（App）和 `http://localhost:18081`（Admin），请求拦截统一注入 `Bearer` token。
5. `@mall/api-sdk` 的响应处理基于业务码：`code !== 200` 直接 `Promise.reject`，`code === 401` 会清理 `localStorage.token` 并跳转 `/login`。
6. `pnpm type-check`、`pnpm build:app` 当前都失败，失败点均为 `apps/mall-app-web/src/views/ProductDetailView.vue:709-710` 的空值类型告警。
7. `pnpm lint` 当前失败，原因是 `apps/mall-admin-web` 缺少 `eslint` 依赖。
8. `pnpm build:admin` 当前可成功，但会出现 `@mall/api-sdk` 重导出引发的 chunk 循环依赖警告与大包告警。
9. `mall-app-web` 的 JD 离线镜像插件已移除，商品图片链路统一走 `/api/asset/image/{hash}`。

## 2. 目录结构（任务相关）

```text
frontend/
├─ package.json
├─ pnpm-lock.yaml
├─ pnpm-workspace.yaml
├─ apps/
│  ├─ mall-app-web/
│  │  ├─ index.html
│  │  ├─ vite.config.ts
│  │  ├─ src/main.ts
│  │  ├─ src/router/index.ts
│  │  └─ src/views/*.vue
│  ├─ mall-admin-web/
│  │  ├─ index.html
│  │  ├─ vite.config.ts
│  │  ├─ src/main.ts
│  │  ├─ src/router/index.ts
│  │  └─ src/views/**/*.vue
│  └─ e2e/
│     ├─ playwright.config.ts
│     └─ tests/{app-golden-loop,admin-golden-loop}.spec.ts
└─ packages/
   └─ api-sdk/
      └─ src/{index.ts,app/*,admin/*}
```

## 3. 子模块详情

### 3.1 `apps/mall-app-web`

- 技术栈：Vue 3、Vue Router、Pinia、Vant、Vite。
- 入口链路：`index.html` -> `/src/main.ts` -> `App.vue` + `router` + `pinia`。
- 鉴权策略：路由 `meta.auth` 为真且本地无 `token` 时跳转 `/login`。
- 路由清单：

| Path | 名称 | 鉴权 | 组件 |
|---|---|---|---|
| `/` | `Home` | 否 | `HomeView.vue` |
| `/login` | `Login` | 否 | `LoginView.vue` |
| `/category` | `Category` | 否 | `CategoryView.vue` |
| `/product/:id` | `ProductDetail` | 否 | `ProductDetailView.vue` |
| `/jd-item` | `JdItem` | 否 | `JdItemView.vue` |
| `/jd-item/:sku` | `JdItemSku` | 否 | `JdItemView.vue` |
| `/cart` | `Cart` | 是 | `CartView.vue` |
| `/order/confirm` | `OrderConfirm` | 是 | `OrderConfirmView.vue` |
| `/order/list` | `OrderList` | 是 | `OrderListView.vue` |
| `/order/detail/:id` | `OrderDetail` | 是 | `OrderDetailView.vue` |
| `/mine` | `Mine` | 是 | `MineView.vue` |
| `/search` | `Search` | 否 | `SearchView.vue` |
| `/upcoming` | `Upcoming` | 否 | `UpcomingView.vue` |
| `/register` | `Register` | 否 | `RegisterView.vue` |
| `/address` | `Address` | 是 | `AddressView.vue` |
| `/collection` | `Collection` | 是 | `CollectionView.vue` |
| `/readHistory` | `ReadHistory` | 是 | `ReadHistoryView.vue` |
| `/attention` | `Attention` | 是 | `AttentionView.vue` |
| `/coupon` | `Coupon` | 是 | `CouponView.vue` |

### 3.2 `apps/mall-admin-web`

- 技术栈：Vue 3、Vue Router、Pinia、Element Plus、Vite。
- 入口链路：`index.html` -> `/src/main.ts` -> `App.vue` -> `LayoutView.vue`。
- 鉴权策略：除 `/login` 外，若无本地 `token` 则统一跳转 `/login`。
- 路由清单：

| Path | 名称 | 组件 |
|---|---|---|
| `/login` | `Login` | `views/login/LoginView.vue` |
| `/dashboard` | `Dashboard` | `views/dashboard/DashboardView.vue` |
| `/pms/product` | `ProductList` | `views/pms/ProductListView.vue` |
| `/pms/brand` | `BrandList` | `views/pms/BrandListView.vue` |
| `/pms/productCategory` | `ProductCategory` | `views/pms/ProductCategoryView.vue` |
| `/pms/productAttrCategory` | `ProductAttrCategory` | `views/pms/ProductAttrCategoryView.vue` |
| `/oms/order` | `OrderList` | `views/oms/OrderListView.vue` |
| `/oms/returnApply` | `ReturnApplyList` | `views/oms/ReturnApplyListView.vue` |
| `/sms/coupon` | `CouponList` | `views/sms/CouponListView.vue` |
| `/sms/flash` | `FlashList` | `views/sms/FlashListView.vue` |
| `/sms/advertise` | `AdvertiseList` | `views/sms/AdvertiseListView.vue` |
| `/ums/admin` | `AdminList` | `views/ums/AdminListView.vue` |
| `/ums/role` | `RoleList` | `views/ums/RoleListView.vue` |
| `/ums/menu` | `MenuList` | `views/ums/MenuListView.vue` |
| `/ums/resource` | `ResourceList` | `views/ums/ResourceListView.vue` |

### 3.3 `packages/api-sdk`

- 入口：`packages/api-sdk/src/index.ts`。
- 请求实例：`appRequest`、`adminRequest`（`axios.create`）。
- 默认 `baseURL`：
  - App：`http://localhost:18080`
  - Admin：`http://localhost:18081`
- 页面层调用现状：
  - 大部分页面通过导出的业务 API（如 `homeApi`、`productApi`）调用。
  - 管理端 `ums` 部分页面直接调用 `adminRequest`（仍经由 SDK，但绕过了按域拆分的 API 文件）。

### 3.4 `apps/e2e`

- 配置文件：`apps/e2e/playwright.config.ts`。
- 用例文件：`apps/e2e/tests/app-golden-loop.spec.ts`、`apps/e2e/tests/admin-golden-loop.spec.ts`。
- 运行策略：`fullyParallel=false`、`workers=1`、失败保留 screenshot/video、重试保留 trace。
- 当前偏差：
  - 配置默认 `baseURL` 是 `3100/3000` 占位值，和前端开发端口 `8091/8090` 不一致。
  - 用例注释仍写 `8085/8086` 旧 API 端口。
  - 管理端用例账号写的是 `admin/admin123`，而 seed 文件默认是 `admin/macro123`（`data/seed/V100__seed_admin_and_base_data.sql`）。

## 4. 端口与 URL 基线

| 模块 | 默认地址 | 配置来源 |
|---|---|---|
| App 前端（Vite） | `http://localhost:8091` | `apps/mall-app-web/vite.config.ts` |
| Admin 前端（Vite） | `http://localhost:8090` | `apps/mall-admin-web/vite.config.ts` |
| App API | `http://localhost:18080` | `packages/api-sdk/src/index.ts` |
| Admin API | `http://localhost:18081` | `packages/api-sdk/src/index.ts` |
| E2E App baseURL（默认） | `http://localhost:3100` | `apps/e2e/playwright.config.ts` |
| E2E Admin baseURL（默认） | `http://localhost:3000` | `apps/e2e/playwright.config.ts` |

本地执行 E2E 时建议显式覆盖：

```powershell
$env:APP_BASE_URL='http://localhost:8091'
$env:ADMIN_BASE_URL='http://localhost:8090'
pnpm test:e2e
```

## 5. 命令与验证结果（2026-02-25）

| 命令 | 结果 | 说明 |
|---|---|---|
| `pnpm --dir project_mall_v3/frontend type-check` | 失败 | `mall-app-web/src/views/ProductDetailView.vue:709-710`，`cartTarget` 可能为 `null` |
| `pnpm --dir project_mall_v3/frontend lint` | 失败 | `mall-admin-web` 缺少 `eslint` 可执行依赖 |
| `pnpm --dir project_mall_v3/frontend build:app` | 失败 | 先执行 `vue-tsc -b`，被同一处类型错误阻断 |
| `pnpm --dir project_mall_v3/frontend build:admin` | 成功（有告警） | Rollup 提示 `@mall/api-sdk` 重导出导致 chunk 循环依赖；存在 `>500 kB` chunk 告警 |
| `pnpm --dir project_mall_v3/frontend/apps/mall-app-web exec vite build --debug \| Select-String "configFile"` | 成功 | 实测读取 `apps/mall-app-web/vite.config.ts` |
| `pnpm --dir project_mall_v3/frontend/apps/mall-admin-web exec vite build --debug \| Select-String "configFile"` | 成功 | 实测读取 `apps/mall-admin-web/vite.config.ts` |

环境基线（本机实测）：

- Node.js：`v24.13.1`
- pnpm：`10.30.1`
- `package.json` engines 要求：Node `>=20`，pnpm `>=9`

## 6. 当前风险与建议

1. 需持续执行 `scripts/check-shadow-js.ps1`，防止 `src/**/*.js` 影子文件回归破坏入口事实源。
2. App/Admin 当前入口已统一为 `main.ts + router/index.ts`，后续改动应保持同一口径。
3. `pnpm lint` 阻断团队静态检查，需在 `mall-admin-web` 补齐 `eslint`（或调整根 `lint` 脚本策略）。
4. App `ProductDetailView.vue` 的空值类型问题应优先修复，否则 `type-check` 与 `build:app` 会持续失败。
5. E2E 默认端口、注释端口和测试账号存在偏差，建议统一到 8091/8090 与当前 seed 口径。
6. 商品图片分发依赖 `/api/asset/image/{hash}`，若新增静态资源入口需同步评审缓存与鉴权策略。

## 7. 文档更新触发条件

1. 路由、入口文件或鉴权逻辑变化时，更新本文件。
2. API 路径、请求参数或 SDK 导出变化时，更新本文件与 `docs/05_backend_frontend_api_usage.md`。
3. E2E 端口、前置条件、测试账号变化时，更新 `apps/e2e/playwright.config.ts` 注释和本文件。
4. 构建/校验命令状态变化时，更新本文件第 5 节“命令与验证结果”。
