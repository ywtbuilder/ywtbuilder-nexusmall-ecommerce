---
owner: data
updated: 2026-02-26
scope: mall-v3
audience: dev,qa,ops
doc_type: guide
---

# Data 模块说明（迁移与种子）

> 文档导航：统一入口见 [../docs/README.md](../docs/README.md)。

## 1. 目录结构与职责

```text
data/
├─ migration/
│  ├─ V1__baseline.sql
│  ├─ V2__v3_schema_additions.sql
│  ├─ V3__default_order_setting.sql
│  ├─ V5__product_blob_tables.sql
│  ├─ V6__performance_search_and_asset_indexes.sql
│  └─ V9__external_asset_map.sql
└─ seed/
   ├─ V100__seed_admin_and_base_data.sql
   ├─ V101__seed_sample_products.sql
   ├─ V102__seed_huawei_watch_gt6.sql   (扩展包)
   └─ V103__seed_products_batch.sql     (扩展包)
```

| 路径 | 作用 |
|---|---|
| `data/migration/` | Schema 增量迁移（V1~V99） |
| `data/seed/` | 初始化基础账号与演示业务数据（V100+） |

## 2. 执行链路（与脚本行为对齐）

推荐通过 `scripts/init-db.ps1` 执行：

```powershell
cd d:/Desktop/work/mall/project_mall_v3
./scripts/init-db.ps1 -SeedProfile minimal
```

或使用统一 `pwsh` 入口：

```powershell
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\init-db.ps1 -SeedProfile minimal
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\init-db.ps1 -SeedProfile full
```

前置条件：

1. Docker 容器 `mallv3-mysql` 已运行。
2. 数据库连接参数与脚本一致：`root/root`，库名 `mall`。

执行顺序（按文件名排序）：

1. 若传入 `-Reset`：先 `DROP/CREATE mall`。
2. 若未传 `-SeedOnly`：依次执行 `migration/*.sql`。
3. `SeedProfile=minimal`：仅执行 `V100` + `V101`（GitHub 展示默认）。
4. `SeedProfile=full`：执行 `seed/*.sql` 全量（需本地扩展 seed 包）。

失败策略：

1. `migration` 任一文件失败会中断。
2. `seed` 单文件失败会告警并继续下一个文件。

常用参数：

1. `-Reset`：先删库重建再导入。
2. `-SeedOnly`：只导入 `seed/`，跳过 `migration/`。
3. `-SeedProfile minimal`：最小演示数据（`V100` + `V101`）。
4. `-SeedProfile full`：全量种子数据（包含扩展包 `V102` + `V103`）。

## 3. 当前迁移文件语义

| 文件 | 作用 |
|---|---|
| `V1__baseline.sql` | 基线占位，仅做连接探活（`SELECT 1`）；完整基础 DDL 由 Docker `init-db.d` 导入 |
| `V2__v3_schema_additions.sql` | 增加 `oms_order.payment_id`、`ums_member.avatar_url`，新增 `oms_payment_log` |
| `V3__default_order_setting.sql` | 若存在 `oms_order_setting`，注入默认超时配置（`INSERT IGNORE`） |
| `V5__product_blob_tables.sql` | 新增 `pms_asset/pms_product_image/pms_product_spec/pms_product_content`，并扩容 `pms_product.album_pics/detail_mobile_html` 字段 |
| `V6__performance_search_and_asset_indexes.sql` | 新增搜索与资产链路关键索引（含 `pms_product`、`sms_home_*`、`pms_asset` 等），降低首页/搜索慢查询风险 |
| `V9__external_asset_map.sql` | 新增外链资源映射表与索引（`pms_external_asset_map`），用于外部资源归档、去重与回填审计 |

## 4. 当前种子文件语义

| 文件 | 主要写入表 |
|---|---|
| `V100__seed_admin_and_base_data.sql` | `ums_admin`、`ums_role`、`ums_admin_role_relation`、`ums_member_level`、`oms_order_return_reason` |
| `V101__seed_sample_products.sql` | `pms_brand`、`pms_product_category`、`pms_product_attribute_category`、`pms_product_attribute`、`pms_product`、`pms_sku_stock`、`sms_coupon`、`sms_home_advertise`、`sms_home_recommend_product`、`sms_home_new_product` |
| `V102__seed_huawei_watch_gt6.sql` | 华为 GT6 商品深度集成数据（商品、SKU、详情图文、首页运营位，扩展包） |
| `V103__seed_products_batch.sql` | 多类目批量商品导入示例数据（扩展包） |

GitHub 主仓库默认分发 `V100` + `V101`，保证“最小可运行演示”。`V102` + `V103` 属于可选扩展数据，不作为展示仓库默认内容。

默认管理账号来自 `V100`：

1. 用户名：`admin`
2. 密码：`macro123`

## 5. 新增迁移/种子的规则

1. 文件名严格递增：`V{next}__{description}.sql`。
2. DDL/DML 尽量使用幂等写法（`IF EXISTS` / `IF NOT EXISTS` / `INSERT IGNORE`）。
3. 迁移脚本内建议保留“存在性判断 + 条件执行”模式，兼容增量升级与重复执行。
4. 同步更新：
   - `docs/03_data_migration.md`
   - 涉及字段语义变更时更新 `docs/02_api_contract.md`

## 6. 最小验证

```sql
SELECT COUNT(*) FROM ums_admin;
SELECT COUNT(*) FROM pms_product;
SELECT COUNT(*) FROM oms_payment_log;
SELECT COUNT(*) FROM oms_order_setting;
```

若表存在且行数符合预期，说明迁移与种子链路基本可用。
