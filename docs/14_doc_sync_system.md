---
owner: docs
updated: 2026-02-26
scope: mall-v3
audience: dev,qa,ops
doc_type: standard
---
# 14 - 文档联动系统（防过期）

> 文档导航：返回 [docs/README.md](README.md)。

## 1. 目标

本系统用于解决“项目变了，但文档没跟上”的问题。核心思路是：

1. 用脚本从代码和目录中提取事实基线。
2. 把事实快照落盘为可审计文件。
3. 在文档门禁中强制比对快照，发现漂移即失败。

## 2. 组成

| 组件 | 路径 | 作用 |
|---|---|---|
| 联动清单 | `docs/docsync_manifest.json` | 定义忽略目录、快照路径、领域到文档映射 |
| 门禁脚本 | `scripts/check-docs.ps1` | frontmatter 校验 + 事实快照比对 |
| 事实快照（JSON） | `docs/_generated/project_facts.snapshot.json` | 机器可读基线（用于比对） |
| 事实快照（Markdown） | `docs/_generated/project_facts.md` | 人类可读基线（用于审阅） |
| Markdown 清单脚本 | `scripts/doc-catalog.ps1` | 扫描全工作区 Markdown，输出清单并检测漂移 |
| Markdown 清单（JSON） | `docs/_generated/markdown_catalog.snapshot.json` | Markdown 文件机器快照（路径/分类/大小/修改时间） |
| Markdown 清单（Markdown） | `docs/_generated/markdown_catalog.md` | Markdown 人类可读清单（分类统计+文件列表） |

## 3. 当前监控字段

1. 数据层：`migration/*.sql` 与 `seed/*.sql` 清单。
2. 后端层：App/Admin Controller 数与端点估算数。
3. 前端层：App/Admin 视图数、`main.*`/`router/index.*` 并存情况、Admin Vite 配置文件。
4. 脚本层：`scripts/*.ps1` 清单与数量。
5. 文档层：全工作区 Markdown 文件清单（默认排除 `node_modules/.git/.vite/dist/target/coverage`）。

## 4. 执行方式

### 4.1 日常检查（提交前）

```powershell
cd d:/Desktop/work/mall/project_mall_v3
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-docs.ps1
```

### 4.2 代码事实变化后刷新快照

```powershell
cd d:/Desktop/work/mall/project_mall_v3
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-docs.ps1 -RefreshFacts
```

### 4.3 单独刷新 Markdown 清单

```powershell
cd d:/Desktop/work/mall/project_mall_v3
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\doc-catalog.ps1 -Refresh
```

### 4.4 实时模式（持续监控 Markdown 变动）

```powershell
cd d:/Desktop/work/mall/project_mall_v3
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\doc-catalog.ps1 -Watch -IntervalSec 5
```

## 5. 漂移处理规则

当出现 `FACTS_DRIFT` 时，按脚本提示更新对应文档。领域映射如下：

| 漂移领域 | 至少同步这些文档 |
|---|---|
| `data` | `data/README.md`、`docs/03_data_migration.md`、`README.md` |
| `backend` | `backend/README.md`、`docs/00_architecture.md`、`docs/02_api_contract.md`、`docs/05_backend_frontend_api_usage.md` |
| `frontend` | `frontend/README.md`、`docs/05_backend_frontend_api_usage.md`、`docs/07_module_inventory_and_doc_gap.md` |
| `scripts` | `scripts/README.md`、`docs/04_release_runbook.md`、`docs/README.md` |

## 6. 治理边界

1. `assets/**` 不纳入 frontmatter 门禁（镜像快照文档不作为核心治理文档）。
2. 核心文档仍必须满足 `owner/updated/scope/audience/doc_type` 五字段。
3. 快照文件由脚本生成，不手改；变更必须通过 `-RefreshFacts`。
4. `check-docs.ps1` 默认会联动执行 `doc-catalog.ps1`；若仅调试 frontmatter，可临时使用 `-SkipCatalog`。
