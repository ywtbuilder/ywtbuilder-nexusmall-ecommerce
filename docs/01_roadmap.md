---
owner: project-core
updated: 2026-02-26
scope: mall-v3
audience: dev
doc_type: roadmap
---
# 01 - V3 路线图与里程碑

> 文档导航：返回 [docs/README.md](README.md)。

## 1. 校对说明

1. 本文已按当前仓库实际代码与配置校对。
2. `[x]` 表示仓库中已有实现（代码/配置/脚本/测试文件）。
3. 计数口径：
   - 后端测试数量按 `backend/**/src/test/java` 中 `@Test` 数统计。
   - 前端页面数量按 `frontend/apps/**/src/views/**/*.vue` 实际文件统计。
4. 本次更新同步纳入新增项：`UserCenterController`、`AssetController`、App 端订单详情页、`JdItemView`、`UpcomingView`。

## Phase 0: 脚手架搭建
- [x] 后端 Maven 多模块聚合根（`backend/pom.xml`）
- [x] `shared-common / shared-security / shared-web / shared-test` 四个 shared 模块
- [x] 7 个业务模块骨架（`member / product / cart / order / marketing / payment / search`）
- [x] `mall-app-api / mall-admin-api / mall-job` 三个运行单元
- [x] 前端 pnpm workspace（`apps/mall-app-web`、`apps/mall-admin-web`、`apps/e2e`、`packages/api-sdk`）
- [x] Docker Compose 基础设施（`infra/docker-compose.local.yml`）
- [x] 数据迁移与种子基线（`data/migration/*` + `data/seed/*`）

## Phase 1: shared 层 + 中间件集成
- [x] `CommonResult / CommonPage / ApiException` 统一
- [x] JWT 鉴权体系统一（生成/解析/黑名单/刷新）
- [x] Redis 与验证码能力（`RedisService`、`AuthCodeService`）
- [x] MyBatis-Plus 统一配置（分页插件 + `MetaObjectHandler` 自动填充）
- [x] OpenAPI 基础配置（`BaseSwaggerConfig` + JWT Bearer）
- [x] 全局异常/CORS/Jackson 时间格式统一
- [x] shared 层单元测试（`shared-common + shared-security` 共 25 个 `@Test`）
- [x] `shared-test` 测试基座（`AbstractIntegrationTest` / `AbstractMvcIntegrationTest`）

## Phase 2: 业务模块补齐（核心域）
- [x] `module-member`：注册/登录/验证码/改密/refresh token + 地址/浏览/收藏/关注
- [x] `module-product`：品牌/分类/属性/商品/SKU 管理能力
- [x] `module-cart`：8 个购物车端点（add/list/promotion/quantity/attr/delete/clear/getProduct）
- [x] `module-order`：App 与 Admin 订单流程 + 退货申请
- [x] `module-marketing`：优惠券/秒杀/首页推荐与广告
- [x] `module-payment`：支付抽象 + Mock 支付实现
- [x] `module-search`：ES 索引读写 + 检索能力

## Phase 3: BFF 层与异步任务
- [x] `mall-app-api` 作为 C 端 BFF（按领域拆分控制器）
- [x] `mall-admin-api` 作为管理端 BFF（PMS/OMS/SMS/UMS + MinIO + ES 管理入口）
- [x] `mall-job` 实现 MQ 消费者（订单超时取消、ES 同步）

## Phase 4: 前端落地与 SDK 统一
- [x] `api-sdk` 模块完整落地（`app/*` 8 个模块，`admin/*` 5 个模块）
- [x] App 端页面 18 个（`frontend/apps/mall-app-web/src/views/*.vue`）
- [x] Admin 端视图 16 个（含 `login/layout/dashboard`）
- [x] E2E 骨架与黄金链路（App 6 条 + Admin 6 条）
- [x] workspace 命令统一（`dev/build/type-check/test:e2e`）

## Phase 5: 测试门禁
- [x] App API 契约测试：43 个 `@Test`（`backend/mall-app-api/.../AppApiContractTest.java`）
- [x] Admin API 契约测试：53 个 `@Test`（`backend/mall-admin-api/.../AdminApiContractTest.java`）
- [x] App 核心流程集成测试：18 个 `@Test`
- [x] Admin 核心流程集成测试：14 个 `@Test`
- [x] shared 层单元测试：25 个 `@Test`
- [x] 根目录独立黑盒测试脚本（`scripts/run-tests.ps1` + `tests/*`）

## Phase 6: 工程化收尾
- [x] GitHub Actions CI（`backend` + `frontend` 双 Job）
- [x] 启停与预检脚本（`preflight-v3.ps1` / `start-v3.ps1` / `stop-v3.ps1`）
- [x] 数据初始化脚本（`init-db.ps1`）
- [x] 文档门禁脚本（`check-docs.ps1`）
- [x] 运行日志与 PID 管理（`runtime-logs/` + 脚本自动维护）

## Phase 7: 安全加固与质量修补
- [x] 订单与支付归属校验（`OrderController` / `PortalOrderServiceImpl` / `PaymentController`）
- [x] 退货、购物车、浏览记录的用户归属防越权校验
- [x] ES 删除接口修正为 `POST /esProduct/delete/{id}`
- [x] 优惠券领取并发控制（`UPDATE ... WHERE count > 0`）
- [x] Mock 支付金额改为取订单真实 `totalAmount`
- [x] `deleteOrder` 事务注解补齐（`@Transactional`）
- [x] JWT 密钥外部化（3 个 `application.yml` 使用 `${MALL_JWT_SECRET:...}`）
- [x] `mall-job` 补齐 `mall.auth` 配置段

## 8. 当前里程碑快照（2026-02-25）

| 维度 | 当前状态 |
|---|---|
| 后端模块 | 14（shared 4 + domain 7 + app/admin/job 3） |
| 前端工作区 | apps 3 + packages 1 |
| 后端测试规模 | `@Test` 总数 154（契约/集成/单元） |
| E2E 规模 | 12 条黄金链路（App 6 + Admin 6） |
| 阶段结论 | Phase 0-7 均已落地，进入持续迭代与维护阶段 |

## 9. 当前遗留（文档与工程一致性）

- [ ] `scripts/run-tests.ps1` 仍是历史端点脚本（`/product/list`、`/brand/list`、`/search/simple`），需按当前接口契约重写。
- [x] 路由同名文件并存风险已清理：当前仅保留 `router/index.ts`。
- [x] `main.ts` 已统一为 `import router from './router/index'` 入口写法。
