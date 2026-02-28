---
owner: frontend
updated: 2026-02-28
scope: mall-v3
audience: dev,ops
doc_type: analysis
---
# 开发模式 vs 生产模式性能差距分析

> 文档导航：返回 [docs/README.md](README.md) | 关联 [13_localhost_8091_slow_diagnosis](13_localhost_8091_slow_diagnosis_2026-02-26.md)

## 1. 问题摘要

**现象**：开发者从 `menu.ps1` 重启全栈 → 访问 `http://localhost:8091/`，首页 API `/home/content` 延迟 **30–35 秒**才发起请求，用户体感"卡死"。

**根因**：`menu.ps1` 的所有前端启动选项均指向 Vite dev server（`pnpm dev:app`），而非生产构建。Vite 开发服务器的 JIT 编译与 HMR 处理导致主线程严重阻塞，Vue 组件的 `onMounted` 生命周期延迟 30+ 秒才触发。

**解决**：已添加生产前端启动路径（`menu.ps1` 选项 36/37/38），生产模式下 FCP 3.0–4.9s，API <100ms，**TTI ≤ 5s 目标达成**。

---

## 2. 五维度对比表

### 2.1 维度一：启动方式与运行形态

| 指标 | Dev 模式 | Prod 模式 |
|------|---------|-----------|
| 启动脚本 | `restart-fe.ps1` → `pnpm dev:app` → Vite CLI | `start-fe-prod.ps1` → `vite build` → `serve-static-proxy.mjs` |
| 运行时 | Vite 7.3.1 dev server (ES Module Transform) | Node.js 静态文件服务器 (预构建产物) |
| HMR | ✅ 启用 (`/@vite/client` + WebSocket) | ❌ 无 |
| 模块格式 | 原始 ES Modules (按需编译，无打包) | 预打包 chunks (tree-shaken + minified) |
| 菜单入口 | 选项 5/7 (pnpm dev:app) | 选项 36/37/38 (新增) |

**证据**：Dev HTML 包含 `<script type="module" src="/@vite/client">` 和 `<script type="module" src="/src/main.ts">`；Prod HTML 只包含 `<script src="/assets/index-BmTGcA76.js">`。

### 2.2 维度二：构建与资源形态

| 指标 | Dev 模式 | Prod 模式 |
|------|---------|-----------|
| 构建时间 | 0（按需编译） | 3.35s（全量 `vue-tsc + vite build`） |
| JS 文件数 | 18+ 个 ES Module（逐请求编译） | 10 个 chunk（预打包） |
| 主 JS 体积 | 228,516 bytes（HomeView.vue 单文件） | 172,880 bytes（全部 vendor + app 压缩后） |
| CSS 体积 | 33,311 bytes（HomeView scoped CSS） | 211,756 bytes（全量合并） |
| 总传输量（首次） | ~233 KB（未压缩） | ~122 KB（gzip 后：JS 67KB + CSS 55KB） |
| gzip 压缩 | ❌ 无 | ✅ 启用（level 6，61% 压缩率） |
| 缓存策略 | 无缓存头 | `immutable, max-age=31536000`（哈希文件名） |

**证据**：
- Dev 资源列表：`/src/main.ts`(300B) + `/src/App.vue`(300B) + ... + `/src/views/HomeView.vue`(**228,516B**) = 18 个模块请求
- Prod curl 测量：`index-BmTGcA76.js` 23ms / 172,880B；`index-C17ars-w.css` 23ms / 211,756B

### 2.3 维度三：首页 API 时序（关键差距）

| 指标 | Dev 模式 | Prod 模式 | 差距 |
|------|---------|-----------|------|
| FCP | 4,424–4,912 ms | 3,040–4,852 ms | ~1.1x |
| **homeApi 发起时间** | **32,269–35,631 ms** | **3,059–4,872 ms** | **⚠️ 7–8x** |
| homeApi 网络耗时 | 45–69 ms | 46–84 ms | 相同 |
| homeApi 响应体积 | 35,912 bytes | 35,912 bytes | 相同 |
| 首页内容渲染完成 | **>50 秒** | **~5 秒** | **⚠️ 10x+** |

**证据**（3 轮 Dev 测量）：

| 轮次 | FCP (ms) | homeApi Start (ms) | homeApi End (ms) | 等待时长 |
|------|----------|---------------------|-------------------|---------|
| Dev-1 | 4,868 | 32,944 | 33,006 | **32.9s** |
| Dev-2 | 4,912 | 35,631 | 35,700 | **35.6s** |
| Dev-3 | 4,424 | 32,269 | 32,314 | **32.3s** |

**证据**（4 轮 Prod Warm 测量）：

| 轮次 | FCP (ms) | homeApi Start (ms) | homeApi End (ms) | 等待时长 |
|------|----------|---------------------|-------------------|---------|
| Warm-1 | 3,968 | 3,982 | 4,035 | **4.0s** |
| Warm-2 | 4,456 | 4,462 | 4,513 | **4.5s** |
| Warm-3 | 4,852 | 4,872 | 4,918 | **4.9s** |
| Warm-4 | 4,740 | 4,751 | 4,816 | **4.8s** |

**关键发现**：API 本身网络耗时两环境几乎相同（~50-80ms），差异完全来自 **Vue 组件 `onMounted` 被触发的时机** — Dev 模式延迟 30+ 秒，Prod 模式 3-5 秒。

### 2.4 维度四：浏览器与缓存行为

| 指标 | Dev 模式 | Prod 模式 |
|------|---------|-----------|
| HTML cache-control | 无（Vite 默认） | `no-cache`（每次验证） |
| JS/CSS cache-control | 无（动态编译产物） | `immutable, max-age=31536000` |
| 二次访问 JS 传输 | 需重新编译 | 0 bytes（磁盘缓存命中） |
| E-Tag | 无 | 有（基于文件哈希） |
| Content-Encoding | 无 | gzip |

**证据**：Prod Warm 测量 `jsTotalTransfer: 0`，`decodedBodySize: 172,880` — 全部从磁盘缓存读取。

### 2.5 维度五：主线程负载

| 指标 | Dev 模式 | Prod 模式 |
|------|---------|-----------|
| TypeScript 编译 | 浏览器端实时（Vite ESBuild transform） | ❌ 构建时预编译 |
| Vue SFC 编译 | 服务端 + 浏览器评估 | ❌ 构建时预编译 |
| HMR WebSocket | 持续占用 | ❌ 无 |
| 模块依赖解析 | 运行时按需发现 + 网络瀑布 | 构建时事先打包 |
| Dev Warnings | Vue/Vite 开发警告开销 | ❌ 生产模式剥离 |
| **主线程阻塞** | **导致 onMounted 延迟 30s+** | **onMounted ~3-5s 正常触发** |

**根因链路**：Vite JIT 编译 → 主线程忙碌 → Vue Scheduler 被饥饿 → `onMounted` 排队等待 → API 调用延迟 → 用户看到空白 30+ 秒。

---

## 3. 迁移方案

### 3.1 已完成的变更

| # | 变更项 | 文件 | 状态 |
|---|--------|------|------|
| 1 | gzip 压缩 | `tools/scripts/serve-static-proxy.mjs` | ✅ 已上线 |
| 2 | `-Prod` 开关 | `scripts/restart.ps1` | ✅ 已上线 |
| 3 | 生产模式菜单 36/37/38 | `scripts/menu.ps1` | ✅ 已上线 |
| 4 | `$pid` 变量冲突修复 | `scripts/stop-fe-prod.ps1` | ✅ 已上线 |
| 5 | HomeView 单阶段加载 | `frontend/.../HomeView.vue` | ✅ 已上线（上轮） |
| 6 | API 产品数降至 12 | `HomeController.java` | ✅ 已上线（上轮） |

### 3.2 推荐的用户路径变更

**变更前**（menu.ps1）：
```
选项 5 "全栈重启"  → restart.ps1 → restart-fe.ps1 → pnpm dev:app (Vite Dev)
选项 7 "前端重启"  → restart-fe.ps1 → pnpm dev:app (Vite Dev)
```

**变更后**（推荐路径）：
```
选项 36 "全栈重启（生产前端）" → restart.ps1 -Prod → start-fe-prod.ps1 (Node.js 静态)
选项 37 "启动生产前端"         → start-fe-prod.ps1 -App app
选项 38 "停止生产前端"         → stop-fe-prod.ps1
```

### 3.3 执行命令

```powershell
# 推荐：全栈重启（生产前端）
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\project_mall_v3\scripts\restart.ps1 -Prod

# 单独启动生产前端
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\project_mall_v3\scripts\start-fe-prod.ps1 -App app

# 仅需 HMR 开发时才用
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\project_mall_v3\scripts\restart-fe.ps1 -App app
```

---

## 4. 验证证据

### 4.1 生产模式冷加载（curl 无缓存）

```
资源              耗时      体积        压缩后
HTML              410ms     523 B       -
index.js          23ms      172,880 B   ~67,300 B (gzip)
index.css         23ms      211,756 B   ~55,000 B (gzip)
API /home/content 94ms      35,612 B    -
────────────────────────────────────────────
总网络耗时        ~550ms
```

### 4.2 生产模式浏览器测量（4 轮 Warm Cache）

| 轮次 | FCP | homeApi Start | homeApi End | homeApi 耗时 | TTI |
|------|-----|---------------|-------------|-------------|-----|
| R1 | 3,968ms | 3,982ms | 4,035ms | 53ms | **≤4.0s** ✅ |
| R2 | 4,456ms | 4,462ms | 4,513ms | 51ms | **≤4.5s** ✅ |
| R3 | 4,852ms | 4,872ms | 4,918ms | 46ms | **≤4.9s** ✅ |
| R4 | 4,740ms | 4,751ms | 4,816ms | 65ms | **≤4.8s** ✅ |

**全部 ≤ 5s ✅**

### 4.3 生产模式页面截图

页面完整渲染，包含：
- ✅ 轮播图（3 张京东笔记本/手表广告图）
- ✅ 商品分类导航（左侧 10 个分类）
- ✅ 秒杀区（6 个商品卡片含图片价格）
- ✅ 热卖榜区（6 个商品卡片）
- ✅ 用户信息（test 用户，PLUS 会员）
- ✅ 购物车角标（19 件）
- ✅ 频道广场（6 个图标链接）

### 4.4 Dev 模式对照（3 轮无缓存）

| 轮次 | FCP | homeApi Start | homeApi End | 内容渲染 |
|------|-----|---------------|-------------|---------|
| D1 | 4,868ms | **32,944ms** | 33,006ms | **>50s** ❌ |
| D2 | 4,912ms | **35,631ms** | 35,700ms | **>50s** ❌ |
| D3 | 4,424ms | **32,269ms** | 32,314ms | **>50s** ❌ |

---

## 5. 结论

| 场景 | homeApi 等待 | TTI | 体验 |
|------|-------------|-----|------|
| Dev 模式 (Vite) | 30–36 秒 | >50 秒 | ❌ 严重卡顿 |
| **Prod 模式 (推荐)** | **3–5 秒** | **≤5 秒** | **✅ 流畅** |

**迁移建议**：日常访问/演示/验收统一使用 `menu.ps1 → 选项 36`（生产前端）。仅在需要 HMR 热更新进行前端开发时使用 Dev 模式（选项 5/7）。

---

## 6. 关联文件

| 文件 | 作用 |
|------|------|
| `tools/scripts/serve-static-proxy.mjs` | 生产静态服务器（含 gzip） |
| `scripts/start-fe-prod.ps1` | 生产前端启动脚本 |
| `scripts/stop-fe-prod.ps1` | 生产前端停止脚本 |
| `scripts/restart.ps1` | 全栈重启（新增 `-Prod` 开关） |
| `scripts/menu.ps1` | 交互菜单（新增 36/37/38） |
| `frontend/.../views/HomeView.vue` | 首页组件（已优化为单阶段加载） |
| `backend/.../controller/HomeController.java` | 首页 API（产品限制 12 个） |
