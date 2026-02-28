---
owner: backend-data
updated: 2026-02-26
scope: mall-v3
audience: dev,ops
doc_type: migration
---
# 03 - 数据迁移策略

> 文档导航：返回 [docs/README.md](README.md)。

## 1. 目录与命名

| 目录 | 当前文件 | 作用 |
|---|---|---|
| `data/migration/` | `V1`, `V2`, `V3`, `V5`, `V6`, `V9` | 结构迁移与默认配置 |
| `data/seed/` | `V100`, `V101`, `V102`, `V103` | 基础账号、样例业务数据、JD 深度集成商品与批量商品数据 |

命名规则：

1. 迁移：`V{1..99}__*.sql`
2. 种子：`V{100+}__*.sql`

## 2. 当前真实执行链路

### 2.1 Docker 首次初始化（容器卷全新时）

`infra/docker-compose.local.yml` 将 `data/migration/` 挂载到 MySQL 的 `/docker-entrypoint-initdb.d`。

含义：

1. 全新 `mysql_data` 卷第一次启动时，`V1/V2/V3/V5/V6/V9` 会被 MySQL 入口脚本执行。
2. 非首次启动不会自动重复执行。

### 2.2 手工初始化脚本（推荐入口）

使用 `scripts/init-db.ps1`：

```powershell
cd d:/Desktop/work/mall/project_mall_v3
./scripts/init-db.ps1
```

行为：

1. 默认：执行 `migration/*.sql` + `seed/*.sql`
2. `-Reset`：先重建 `mall` 库，再执行导入
3. `-SeedOnly`：仅执行 `seed/*.sql`
4. 当前脚本通过 PowerShell 管道写入 `docker exec mysql`，中文内容存在编码风险（与仓库 AGENTS 推荐口径不一致）。

### 2.3 SQL 执行编码建议（生产与中文数据）

推荐使用 `docker cp + mysql source`，避免管道导致乱码：

```powershell
cd d:/Desktop/work/mall/project_mall_v3
docker cp data/seed/V102__seed_huawei_watch_gt6.sql mallv3-mysql:/tmp/v102.sql
docker exec mallv3-mysql mysql -uroot -proot --default-character-set=utf8mb4 mall -e "source /tmp/v102.sql"
```

### 2.4 Flyway 状态（当前未启用）

应用配置里存在 Flyway 参数，但仅 `mall-app-api` 与 `mall-admin-api` 配置了 Flyway，且均为：

1. `spring.flyway.enabled=false`
2. `spring.flyway.locations=classpath:db/migration`

仓库内没有 `backend/**/resources/db/migration` 脚本，因此当前生产事实是“脚本执行为主，不是应用启动自动迁移”。

## 3. 迁移文件语义

| 文件 | 关键语义 |
|---|---|
| `V1__baseline.sql` | 基线占位（`SELECT 1`），不包含全量建表 DDL |
| `V2__v3_schema_additions.sql` | 幂等新增：`oms_order.payment_id`、`ums_member.avatar_url`、`oms_payment_log` |
| `V3__default_order_setting.sql` | 幂等写入默认订单超时配置（`INSERT IGNORE`） |
| `V5__product_blob_tables.sql` | 新增商品图片 BLOB 与内容归档相关表：`pms_asset`、`pms_product_image`、`pms_product_spec`、`pms_product_content`，并调整 `pms_product.album_pics/detail_mobile_html` 字段类型 |
| `V6__performance_search_and_asset_indexes.sql` | 新增性能与检索相关索引，优化首页/搜索/资产查询链路 |
| `V9__external_asset_map.sql` | 新增外链资源映射表，支撑外链迁移批次审计与回滚 |

### 3.1 基线 schema 前置条件

`data/migration/` 当前不包含完整基础建表脚本。对“全新空库”执行时，需要先准备基础 schema（例如导入 `project_mall_v1/mall_v1/document/sql/mall.sql` 或使用已有已初始化的数据卷），再执行 V2/V3/V5/V6/V9 与 seed 导入。

## 4. 种子文件语义

| 文件 | 关键数据 |
|---|---|
| `V100__seed_admin_and_base_data.sql` | 管理员 `admin/macro123`、角色关系、会员等级、退货原因 |
| `V101__seed_sample_products.sql` | 品牌、分类、商品、SKU、优惠券、首页推荐/广告样例数据 |
| `V102__seed_huawei_watch_gt6.sql` | 华为 GT6 深度集成商品（含详情图文、SKU 规格、首页运营位联动） |
| `V103__seed_products_batch.sql` | 批量商品导入样例（多类目、多品牌） |

## 5. 变更约束

1. Schema 变更必须新增 `V{next}__*.sql`，禁止直接改历史版本。
2. 脚本必须尽量幂等（`IF EXISTS`、`IF NOT EXISTS`、`INSERT IGNORE`）。
3. 改迁移脚本时必须同步更新本文和 `data/README.md`。

## 6. 最小验收

```powershell
cd d:/Desktop/work/mall/project_mall_v3
./scripts/init-db.ps1 -Reset
```

```sql
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='mall';
SELECT COUNT(*) FROM ums_admin;
SELECT COUNT(*) FROM pms_product;
SELECT COUNT(*) FROM oms_payment_log;
```
