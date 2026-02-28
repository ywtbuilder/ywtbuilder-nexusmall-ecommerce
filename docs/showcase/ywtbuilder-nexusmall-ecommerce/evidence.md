# ywtbuilder-nexusmall-ecommerce 展示证据页

## 一句话价值

面向技术展示的企业级全栈电商平台，7 大业务域完整实现，53 条淘宝真实 SKU 可演示，支持本地一键复现。

## 演示媒体

- **演示视频**：`docs/showcase/ywtbuilder-nexusmall-ecommerce/demo.mp4`
  - 建议镜头：首页聚合 → 商品搜索（ES 命中）→ 商品详情 → 加购 → 管理端商品管理
- **截图 1**（`shot-01.png`）：买家端首页——轮播广告 + 分类导航 + 推荐商品 + 新品上架
- **截图 2**（`shot-02.png`）：商品搜索结果（ES 全文检索 / 降级 MySQL）
- **截图 3**（`shot-03.png`）：管理后台商品/订单管理页面

## 一键运行命令（PowerShell 7，生产模式）

```powershell
cd ywtbuilder-nexusmall-ecommerce
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\preflight-v3.ps1
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-v3.ps1 -Frontend
```

## 核心技术决策

| 决策 | 问题 | 方案 | 收益 |
|------|------|------|------|
| 双端 BFF | 买家端与管理端需求差异大，统一 API 耦合严重 | `mall-app-api` + `mall-admin-api` 分别聚合 | 前端按需获取，无多余字段 |
| ES 降级 | ES 单点故障时搜索不可用 | ES 异常捕获 → 自动 fallback MySQL LIKE | 搜索链路 100% 可用性 |
| 图片 BLOB 服务 | CDN 外链在演示环境不可用，图片断链 | 图片入 `pms_asset`，`/api/asset/image/{hash}` 提供 | 完全离线，`sha256` 去重 |
| MQ 异步任务 | 订单超时取消若用轮询，DB 开销大 | RabbitMQ TTL + 死信队列 | 无定时扫表，事件驱动取消 |
| pnpm Monorepo + api-sdk | 前端直接写 `fetch` 缺类型安全，Admin/App URL 重复 | `packages/api-sdk`：`app/` + `admin/` 独立入口 | TS 类型保护，构建时接口契约检查 |

## 性能/稳定性证据（本机实测，2026-02-27）

| 指标 | 目标 | **实测值** | 测量方式 |
|------|------|-----------|---------|
| 首屏可交互时间（生产模式）| ≤ 1000ms | **p95 = 139.7ms** ✅ | Playwright 20 次采样，DevTools Performance API |
| `GET /home/content` 响应延迟 | ≤ 300ms | **p95 = 52ms** ✅ | `diagnose-home-slow.ps1` 本机实测 |
| `GET /search/product` 响应延迟 | ≤ 200ms | **p95 = 6.1ms** ✅ | `perf-check-v3.ps1 -Mode after -Samples 20` |
| 生产模式 FCP（热缓存） | ≤ 5000ms | **3,968–4,852ms** ✅ | Chrome DevTools 4 轮实测 |
| API 冒烟测试通过率 | `critical failures=0` | **0 critical failures** ✅ | `smoke-api-v3.ps1` |
| 商品数据完整性 | `violations=0` | **0 violations** ✅ | `check-product-detail-data.ps1 -FailOnViolation` |
| 外链图片合规率 | `violations=0` | **0 violations** ✅ | `audit-resource-origin.ps1 -FailOnViolation` |
| ES 降级可用性 | 100% | **100%**（ES 停止后 MySQL 自动接管）✅ | 手动停止 ES 容器后搜索验证 |
| API 响应体大小 | — | `/home/content` ≈ 35KB，`/product/search` ≈ 10KB | Network 面板抓包 |

## 后端测试规模

| 类型 | 数量 | 说明 |
|------|------|------|
| 买家端契约测试 | **43 个 @Test** | `AppApiContractTest.java` |
| 管理端契约测试 | **53 个 @Test** | `AdminApiContractTest.java` |
| App 集成测试 | **18 个 @Test** | Testcontainers |
| Admin 集成测试 | **14 个 @Test** | Testcontainers |
| Shared 单元测试 | **25 个 @Test** | `shared-common` + `shared-security` |
| E2E 黄金链路 | **12 条** | Playwright（App 6 + Admin 6）|
| **合计** | **154 @Test + 12 E2E** | — |

## 数据规模

| 维度 | 数量 |
|------|------|
| 商品 SKU | **53 条**（淘宝爬取） |
| 商品分类 | **10 个**（手机/平板/笔记本/服饰/运动鞋/智能穿戴/图书/家电/美妆/食品） |
| `pms_asset` 图片资产 | 53 条主图 + 详情图（sha256 去重入库）|
| 首页轮播 | **5 张**（含前景叠加 overlay 模式）|

## 面试可提问点

1. **为什么采用双端 BFF，而不是统一 API 网关直连领域服务？**
   > 买家端与管理端对同一资源的聚合粒度、字段取舍差异较大；统一网关难以在不引入大量 query param 的情况下满足两端需求，BFF 层可以各自做最合适的聚合，同时复用下游同一套领域模块。

2. **ES 降级触发条件是什么，如何防止误降级？**
   > `module-search` 捕获 `ElasticsearchException`，降级逻辑为"异常即切换"；目前未做错误率阈值/熔断器，属于演示级简化实现，生产应引入 Resilience4j Circuit Breaker。

3. **订单超时取消为什么用 MQ 死信队列而不是定时任务？**
   > 定时扫表方案存在轮询 DB 开销 + 最大延迟 = 扫描间隔 的问题；死信队列设置 TTL 后，到期直接投递死信队列，精度高且无额外 DB 扫描。代价是消息幂等处理必须保证（当前通过状态检查 `status=0` 实现）。

4. **图片 BLOB 入库的收益与代价？**
   > 收益：完全离线可用，无外链依赖，`sha256` 天然去重；代价：MySQL BLOB 会占用大量存储，大图会影响查询性能，需要配合 `pms_asset` 表独立化 + ETag 缓存减少重复传输。生产环境应改为 MinIO/OSS + URL 存储。

5. **多模块 Maven 项目如何做依赖边界治理？**
   > 通过父 `pom.xml` 统一版本管理，领域模块（`mall-modules/*`）只依赖 `shared-*`，不允许相互依赖；BFF（`mall-app-api`/`mall-admin-api`）通过显式 `<dependency>` 按需引入领域模块，`mall-job` 只引入需要消费的模块。


