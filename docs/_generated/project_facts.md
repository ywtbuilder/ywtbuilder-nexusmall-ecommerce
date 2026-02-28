---
owner: docs
updated: 2026-02-27
scope: mall-v3
audience: dev,qa,ops
doc_type: analysis
---

# 文档联动事实快照（自动生成）

> 由 scripts/check-docs.ps1 -RefreshFacts 自动生成，请勿手工修改。

## 1. 数据基线

| 指标 | 当前值 |
|---|---|
| migration 文件 | V1__baseline.sql, V2__v3_schema_additions.sql, V3__default_order_setting.sql, V5__product_blob_tables.sql, V6__performance_search_and_asset_indexes.sql, V9__external_asset_map.sql |
| seed 文件 | V100__seed_admin_and_base_data.sql, V101__seed_sample_products.sql, V102__seed_huawei_watch_gt6.sql, V103__seed_products_batch.sql |

## 2. 后端基线

| 指标 | 当前值 |
|---|---:|
| App Controller 数 | 17 |
| App 端点估算数 | 68 |
| Admin Controller 数 | 28 |
| Admin 端点估算数 | 154 |

## 3. 前端基线

| 指标 | 当前值 |
|---|---|
| App 视图数 | 18 |
| Admin 视图数 | 16 |
| App 入口文件 | main.ts |
| Admin 入口文件 | main.ts |
| App 路由文件 | index.ts |
| Admin 路由文件 | index.ts |
| Admin Vite 配置 | vite.config.ts |

## 4. 脚本基线

| 指标 | 当前值 |
|---|---|
| 脚本数量 | 29 |
| 脚本清单 | audit-home-network-hosts.ps1, audit-resource-origin.ps1, build-be.ps1, check-docs.ps1, check-product-detail-data.ps1, check-shadow-js.ps1, dev-start-v3.ps1, diagnose-home-slow.ps1, doc-catalog.ps1, import-taobao-catalog.ps1, init-db.ps1, logs.ps1, menu.ps1, migrate-external-assets.ps1, normalize-taobao-assets.ps1, perf-check-v3.ps1, preflight-v3.ps1, restart-be.ps1, restart-fe.ps1, restart.ps1, run-tests.ps1, smoke-api-v3.ps1, start-fe-prod.ps1, start-v3.ps1, status.ps1, stop-fe-prod.ps1, stop-v3.ps1, sync-carousel-overlay.ps1, toolbox.ps1 |

## 5. 生成时间

- 2026-02-27T20:49:18+08:00
