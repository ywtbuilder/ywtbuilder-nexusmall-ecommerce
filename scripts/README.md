---
owner: scripts
updated: 2026-02-27
scope: mall-v3
audience: dev,qa,ops
doc_type: runbook
---

# Scripts — Mall V3 本地开发脚本工具箱
> 文档导航：统一入口见 [../docs/README.md](../docs/README.md)
>
> **📌 最常用：拖 `menu.ps1` 到终端 → 按回车 → 选编号执行任意操作**

## 1. 脚本全览

| 脚本 | 作用 | 典型用法 |
|---|---|---|
| **`menu.ps1`** ⭐ | **交互式操作菜单（主入口，拖进终端按回车）** | `.\menu.ps1` |
| **`toolbox.ps1`** ⭐ | **脚本/工具统一治理入口（清单、分类、体检、文档生成）** | `.\toolbox.ps1 list` |
| **`restart-be.ps1`** ⭐ | **重启后端服务（最常用）** | `.\restart-be.ps1` |
| **`restart-fe.ps1`** ⭐ | **重启前端开发服务器（最常用）** | `.\restart-fe.ps1` |
| **`start-fe-prod.ps1`** ⭐ | **构建并启动前端生产模式静态服务** | `.\start-fe-prod.ps1` |
| `stop-fe-prod.ps1` | 停止前端生产模式静态服务 | `.\stop-fe-prod.ps1` |
| **`restart.ps1`** ⭐ | **一键重启全部（默认 `-Prod` 生产前端，TTI<3s）** | `.\restart.ps1 -Prod` |
| `status.ps1` | 服务状态仪表盘（Docker/进程/内存/健康） | `.\status.ps1` |
| `logs.ps1` | 日志查看器（颜色高亮 + tail -f 实时追踪） | `.\logs.ps1 -Service app -Follow` |
| `build-be.ps1` | 单独构建后端（不重启） | `.\build-be.ps1` |
| `start-v3.ps1` | 完整启动（基础设施+后端+可选前端） | `.\start-v3.ps1 -Frontend` |
| `stop-v3.ps1` | 完整停止所有服务 | `.\stop-v3.ps1` |
| `dev-start-v3.ps1` | Dev 模式启动（spring-boot:run，支持 DevTools） | `.\dev-start-v3.ps1` |
| `init-db.ps1` | 数据库初始化（migration + seed，支持 reset） | `.\init-db.ps1` |
| `preflight-v3.ps1` | 环境预检（Java/Docker/Node/端口扫描） | `.\preflight-v3.ps1` |
| `run-tests.ps1` | 历史 contract/integration 测试脚本（保留并行） | `.\run-tests.ps1` |
| `smoke-api-v3.ps1` | 当前接口契约冒烟（推荐替代 run-tests） | `.\smoke-api-v3.ps1` |
| `perf-check-v3.ps1` | 核心链路性能采样（P50/P95/Max + baseline/after diff） | `.\perf-check-v3.ps1 -Mode baseline` |
| `audit-resource-origin.ps1` | 资源来源合规审计（SQL + 源码扫描） | `.\audit-resource-origin.ps1 -FailOnViolation` |
| `audit-home-network-hosts.ps1` | 首页网络主机审计（30 次采样 + 外域 host 清单） | `.\audit-home-network-hosts.ps1 -FailOnViolation` |
| `migrate-external-assets.ps1` | 外链资源迁移到 MySQL（dry-run/apply/rollback） | `.\migrate-external-assets.ps1 -Apply` |
| `check-product-detail-data.ps1` | 商品详情数据完整性检查与回填（简介图/详情图/规格核心项） | `.\check-product-detail-data.ps1 -FailOnViolation` |
| `normalize-taobao-assets.ps1` | 淘宝素材目录/命名/元数据标准化（统一成可入库结构） | `.\normalize-taobao-assets.ps1` |
| `import-taobao-catalog.ps1` | 淘宝标准化目录全量导入并替换商品域 | `.\import-taobao-catalog.ps1` |
| `diagnose-home-slow.ps1` | 首页慢加载一键诊断（JSON + Markdown 报告） | `.\diagnose-home-slow.ps1` |
| `check-shadow-js.ps1` | 检查并阻断 `src/**/*.js` 影子文件回归 | `.\check-shadow-js.ps1` |
| `sync-carousel-overlay.ps1` | 轮播图 1~5 资产入库 + 广告位标准化（前景叠加元数据） | `.\sync-carousel-overlay.ps1` |
| `check-docs.ps1` | 校验文档 frontmatter + 事实快照漂移 | `.\check-docs.ps1` |
| `doc-catalog.ps1` | 校验/生成全工作区 Markdown 清单（支持实时 watch） | `.\doc-catalog.ps1 -CheckOnly` |

---

## 2. 快速上手（拖脚本到终端按回车）

### 主入口：交互式菜单

```powershell
.\menu.ps1
```
弹出带编号的操作菜单，输入数字 → 按回车执行。覆盖所有日常操作。

**常用菜单编号速查（生产模式为默认）：**

| 编号 | 说明 | 等价命令 |
|------|------|---------|
| **1** | ★ 全栈重启（生产前端） | `restart.ps1 -Prod` |
| **4** | 仅启动生产前端 | `start-fe-prod.ps1` |
| **5** | 重启全栈（生产前端，自动构建） | `restart.ps1 -Prod` |
| **6** | 重启全栈（生产前端，跳过构建） | `restart.ps1 -Prod -SkipBuild` |
| **9** | 仅重启生产前端（跳过构建） | `start-fe-prod.ps1 -SkipBuild` |
| 36 | 启动前端（dev/HMR 开发模式） | `restart-fe.ps1` |
| 37 | 重启全栈（dev 前端） | `restart.ps1` |
| 39 | 首次完整初始化（含基础设施） | `start-v3.ps1 -Frontend` |

### 最常用的 3 条命令（生产模式默认，TTI<3s）

```powershell
# 修改了后端代码 → 重启后端（自动检测是否需要 Maven 构建）
.\restart-be.ps1

# 修改了前端代码 → 重新构建并启动生产前端（TTI<3s，已编译最快）
.\start-fe-prod.ps1 -SkipBuild     # -SkipBuild 跳过构建步骤，仅重启静态服务

# 前后端都改了 → 一键重启全部（生产前端）
.\restart.ps1 -Prod
```

> **开发者注意**：需要 HMR 热更新（Vite dev）时，改用以下几个命令：
```powershell
.\restart-fe.ps1          # dev 模式前端（HMR，但 TTI 30s+）
.\restart.ps1              # dev 前端 + 后端重启
.\start-v3.ps1 -Frontend  # 首次完整初始化（含基础设施，dev 前端）
```

### 工具治理入口（新增）

```powershell
# 全量查看脚本清单（含分类）
.\toolbox.ps1 list

# 体检（路径覆盖/硬编码风险/分类统计）
.\toolbox.ps1 doctor

# 生成工具文档
.\toolbox.ps1 docs
```

---

## 3. 新增脚本详解

### `restart-be.ps1` — 重启后端

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-Service` | `all` | `all` / `app` / `admin` / `job` |
| `-SkipBuild` | 否 | 跳过 Maven 构建（直接用现有 JAR） |
| `-Profile` | `local` | Spring 配置 profile |
| `-HealthTimeoutSec` | `90` | 等待健康就绪最长秒数 |
| `-ForceKill` | 否 | 强制重建（忽略 fingerprint 缓存） |

**流程：**
1. 停止现有后端进程（PID 文件 + 端口检测双重保障）
2. 检测代码指纹变化，决定是否重新 Maven 构建
3. 启动 JAR（后台进程，日志写入 `runtime-logs/`）
4. 等待端口监听就绪，打印结果

```powershell
.\restart-be.ps1                      # 全部重启，自动判断是否构建
.\restart-be.ps1 -SkipBuild           # 跳过构建（代码没变但想快速重启）
.\restart-be.ps1 -Service app         # 只重启 mall-app-api (:18080)
.\restart-be.ps1 -Service admin       # 只重启 mall-admin-api (:18081)
```

---

### `restart-fe.ps1` — 重启前端

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-App` | `all` | `all` / `app` / `admin` |
| `-WaitSec` | `60` | 等待前端就绪最长秒数 |
| `-NoWait` | 否 | 启动后立即返回，不等待就绪 |

**流程：**
1. 杀死 PID 文件 + 端口 + 关键字三重方式清理旧进程
2. 后台启动 `pnpm dev:admin` / `pnpm dev:app`
3. 扫描日志中实际端口，等待 HTTP 响应
4. 保存 PID 文件，打印访问地址

```powershell
.\restart-fe.ps1                      # 重启 admin-web (:8090) + app-web (:8091)
.\restart-fe.ps1 -App app             # 只重启 app-web
.\restart-fe.ps1 -NoWait              # 启动后不等待，立即返回
```

---

### `start-fe-prod.ps1` / `stop-fe-prod.ps1` — 前端生产模式

`start-fe-prod.ps1` 会先构建 `dist`，再以 Node 静态服务托管：
- `http://localhost:8091`（用户端）
- `http://localhost:8090`（管理端）

并内置同源反向代理：
- `/api -> http://localhost:18080`
- `/admin-api -> http://localhost:18081`

```powershell
.\start-fe-prod.ps1
.\start-fe-prod.ps1 -App app
.\start-fe-prod.ps1 -SkipBuild
.\stop-fe-prod.ps1
```

---

### `restart.ps1` — 一键重启全部

> 默认使用生产前端模式（`-Prod`），TTI<3s；不传 `-Prod` 则使用 dev 前端（HMR，TTI 30s+）。

```powershell
.\restart.ps1 -Prod                   # 生产前端重启（★ 日常推荐）
.\restart.ps1 -Prod -SkipBuild        # 跳过 Maven，直接重启（生产前端）
.\restart.ps1 -SkipFrontend           # 只重启后端
.\restart.ps1 -Prod -App app          # 只重启 app 相关（生产前端）
# dev 模式（需要 HMR 时才用）
.\restart.ps1                         # dev 前端 + 后端重启
```

---

### `status.ps1` — 状态仪表盘

实时显示：Docker 容器状态 / Java 进程 PID+内存+运行时长+HTTP健康 / 前端进程 / 最近 5 行日志 / 访问地址汇总

```powershell
.\status.ps1           # 单次显示
.\status.ps1 -Watch    # 持续刷新（每 4s，Ctrl+C 退出）
```

---

### `logs.ps1` — 日志查看器

| Service | 对应文件 |
|---|---|
| `app` | `mall-app-api.log` |
| `admin` | `mall-admin-api.log` |
| `job` | `mall-job.log` |
| `fe-app` | `mall-app-web.log` |
| `fe-admin` | `mall-admin-web.log` |
| `all` | 所有服务各最后 20 行 |

颜色：ERROR/Exception=红，WARN=黄，DEBUG=暗灰

```powershell
.\logs.ps1                                  # app-api 最后 50 行
.\logs.ps1 -Service app -Follow             # 实时追踪（tail -f）
.\logs.ps1 -Service all                     # 所有服务摘要
.\logs.ps1 -Service admin -Lines 100        # admin-api 最后 100 行
.\logs.ps1 -Service app -IncludeErrors      # 同时显示 stderr
```

---

### `build-be.ps1` — 构建后端（不重启）

```powershell
.\build-be.ps1                  # 增量构建全部模块（跳测试）
.\build-be.ps1 -Clean           # 全量重建（clean package）
.\build-be.ps1 -Module app      # 只构建 mall-app-api
.\build-be.ps1 -Test            # 包含单元测试
```

---

### `menu.ps1` — 交互式主入口

```powershell
.\menu.ps1
```
覆盖当前菜单的全部操作（重启/构建/启动/停止/日志/数据库/预检/审计/工具治理）。
当前菜单项按脚本实际清单动态维护，新增能力以 `menu.ps1` 实际菜单为准（含首页网络主机审计与外链资源迁移）。

---

### `audit-resource-origin.ps1` — 资源来源合规审计

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-DbContainer` | `mallv3-mysql` | MySQL Docker 容器名 |
| `-DbUser` | `root` | MySQL 用户 |
| `-DbPassword` | `root` | MySQL 密码 |
| `-DbName` | `mall` | 数据库名 |
| `-ReportPath` | `./runtime-logs/resource-origin-audit.json` | 审计报告输出路径 |
| `-FailOnViolation` | 否 | 命中违规时返回退出码 1 |

**检查项：**
1. `sms_home_advertise(status=1).pic` 是否为 `/api/asset/image/{hash}` 或 `data:image/*`。
2. `pms_product(publish_status=1, delete_status=0)` 的 `pic/album_pics/detail_html` 是否命中允许来源。
3. 前后台关键源码中是否出现外链域名与 `/images/` 路径。

```powershell
.\audit-resource-origin.ps1
.\audit-resource-origin.ps1 -FailOnViolation
```

---

### `check-product-detail-data.ps1` — 商品详情数据完整性检查

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-DbContainer` | `mallv3-mysql` | MySQL Docker 容器名 |
| `-DbUser` / `-DbPassword` | `root` / `root` | MySQL 账号 |
| `-DbName` | `mall` | 数据库名 |
| `-ReportPath` | `./runtime-logs/product-detail-data-check.json` | 检查报告输出路径 |
| `-AutoFix` | 否 | 自动回填缺失 `type=1/2` 图片并重建异常 `detail_html` |
| `-FailOnViolation` | 否 | 命中问题时返回退出码 1 |

**检查项：**
1. 上架商品缺 `intro`（`image_type=2`）列表。
2. 上架商品缺 `detail`（`image_type=1`）列表。
3. 缺核心规格字段（品牌/型号/商品编号/特色功能/包装清单）列表。
4. 详情首图异常小图（宽高/文件大小阈值）列表。
5. `pic` 字段非法路径与 `detail_html` 首图 hash 复用异常。

```powershell
.\check-product-detail-data.ps1
.\check-product-detail-data.ps1 -AutoFix
.\check-product-detail-data.ps1 -FailOnViolation
```

---

### `smoke-api-v3.ps1` — API 冒烟检查（当前推荐）

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-AppBase` | `http://localhost:18080` | App API 根地址 |
| `-AdminBase` | `http://localhost:18081` | Admin API 根地址 |
| `-AdminUser` / `-AdminPassword` | `admin` / `macro123` | 管理员登录账号 |
| `-MemberUser` / `-MemberPassword` | `test` / `test123456` | 会员登录账号（非关键失败） |
| `-ReportPath` | `./runtime-logs/api-smoke-v3.json` | 冒烟报告输出路径 |

**检查项：**
1. `app/admin` 健康检查。
2. `GET /home/content`、`GET /product/search`、`GET /product/detail/{id}`。
3. `POST /admin/login`、`GET /admin/info`。
4. 首页广告图与抽样商品图字段来源合规（`/api/asset/image/` 或内联）。

```powershell
.\smoke-api-v3.ps1
.\smoke-api-v3.ps1 -AppBase http://localhost:18080 -AdminBase http://localhost:18081
```

---

### `perf-check-v3.ps1` — 核心链路性能采样

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-Mode` | `after` | `baseline` / `after` |
| `-WebBase` | `http://localhost:8091` | 用户端页面入口 |
| `-AppBase` | `http://localhost:18080` | App API 地址 |
| `-Samples` | `30` | 每个用例采样次数 |
| `-TimeoutSec` | `15` | 单次请求超时秒数 |

**输出文件：**
1. `runtime-logs/perf-baseline.json`
2. `runtime-logs/perf-after.json`
3. `runtime-logs/perf-diff.md`

```powershell
.\perf-check-v3.ps1 -Mode baseline
.\perf-check-v3.ps1 -Mode after
```

---

### `diagnose-home-slow.ps1` — 首页慢加载诊断

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-WebBase` | `http://localhost:8091` | 前端页面地址 |
| `-ApiBase` | `http://localhost:18080` | App API 地址 |
| `-LogTailLines` | `200` | 扫描前端日志尾部行数 |
| `-ReportPath` | `./runtime-logs/home-slow-diagnosis.json` | JSON 报告路径 |
| `-MarkdownPath` | `./runtime-logs/home-slow-diagnosis.md` | Markdown 报告路径 |

**诊断项：**
1. 首页访问耗时与状态码。
2. `/home/content` 与 `/product/search` 的耗时与响应体大小。
3. `runtime-logs/mall-app-web.log` 关键错误词命中。
4. 路由与 Vite 配置同名覆盖风险（`index.js`/`index.ts`、`vite.config.js`/`vite.config.ts`）。

```powershell
.\diagnose-home-slow.ps1
.\diagnose-home-slow.ps1 -LogTailLines 400
```

---

### `audit-home-network-hosts.ps1` — 首页网络主机审计

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-WebBase` | `http://localhost:8091` | 首页入口 |
| `-Samples` | `30` | 采样次数 |
| `-TimeoutSec` | `15` | 单次请求超时 |
| `-ReportPath` | `./runtime-logs/home-network-host-audit.json` | 报告路径 |
| `-FailOnViolation` | 否 | 命中外域 host 时返回退出码 1 |

```powershell
.\audit-home-network-hosts.ps1
.\audit-home-network-hosts.ps1 -FailOnViolation
```

---

### `migrate-external-assets.ps1` — 外链资源迁移（品牌图/头像/商品兜底）

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-Apply` | 否 | 执行真实改写（默认 dry-run） |
| `-RollbackBatch` | 空 | 按批次回滚 |
| `-BatchNo` | 自动生成 | 迁移批次号 |
| `-Output` | `runtime-logs/external-asset-migration-report.json` | 迁移报告 |
| `-BaselineOutput` | `runtime-logs/external-url-baseline.json` | 外链基线快照 |

```powershell
.\migrate-external-assets.ps1
.\migrate-external-assets.ps1 -Apply
.\migrate-external-assets.ps1 -RollbackBatch EXT20260227153000
```

---

### `check-shadow-js.ps1` — 影子 JS 回归检查

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-ReportPath` | `<project_root>/runtime-logs/shadow-js-check.json` | 检查报告输出路径（相对路径会自动锚定到项目根） |
| `-Fix` | 否 | 自动删除命中的影子 `.js` 文件 |
| `-NoFail` | 否 | 命中影子 `.js` 时仅告警，不返回退出码 1 |

**输出语义（已统一）：**
1. `OK`：无命中，或 `-Fix` 后清零。
2. `WARN`：有命中，但启用了 `-NoFail`。
3. `FAIL`：有命中且未启用 `-NoFail`。

**检查范围：**
1. `frontend/apps/mall-app-web/src/**/*.js`
2. `frontend/apps/mall-admin-web/src/**/*.js`

**报告字段（关键）：**
1. `before_fix_count`
2. `after_fix_count`
3. `fixed_count`
4. `fixed_paths`

```powershell
.\check-shadow-js.ps1
.\check-shadow-js.ps1 -Fix -NoFail
.\check-shadow-js.ps1 -NoFail
```

**CI 门禁：**
1. 安装依赖后先执行一次 `check-shadow-js.ps1`（pre-build）。
2. `build:admin` 后再执行一次。
3. `build:app` 后再执行一次。

---

### `sync-carousel-overlay.ps1` — 轮播图资产同步与叠加配置

| 参数 | 默认值 | 说明 |
|---|---|---|
| `-DbHost` | `localhost` | MySQL 主机 |
| `-DbPort` | `13306` | MySQL 端口 |
| `-DbUser` / `-DbPassword` | `root` / `root` | MySQL 账号 |
| `-DbName` | `mall` | 数据库名 |
| `-Output` | `runtime-logs/carousel-overlay-map.json` | 结果报告路径 |
| `-SkipAdvertiseUpdate` | 否 | 仅同步 `pms_asset`，不改 `sms_home_advertise` |

**行为：**
1. 扫描 `assets/淘宝爬取商品数据/轮播图/1..5`。
2. 自动识别背景/前景（透明通道判定）。
3. Upsert 到 `pms_asset`（按 `image_hash` 去重）。
4. 标准化 `sms_home_advertise` 为 5 条：`1/4/5` 叠加，`2/3` 仅背景，并把元数据写入 `note`。

```powershell
.\sync-carousel-overlay.ps1
.\sync-carousel-overlay.ps1 -SkipAdvertiseUpdate
```

---

## 4. 原有脚本（`start-v3.ps1` / `stop-v3.ps1` 等）

| 脚本 | 作用 | 常用参数 |
|---|---|---|
| `preflight-v3.ps1` | 环境预检：Java/Maven/Docker/Node/pnpm + 关键端口占用扫描 | 无 |
| `start-v3.ps1` | 完整启动（基础设施+后端+可选前端） | `-SkipBuild`, `-SkipInfra`, `-Frontend`, `-BackendHealthTimeoutSec` |
| `stop-v3.ps1` | 停止后端/前端进程，并可选停止基础设施 | `-KeepInfra`, `-ForcePortKill` |
| `dev-start-v3.ps1` | Dev 模式启动（spring-boot:run，支持 Spring DevTools） | `-Service`, `-Profile`, `-StopFirst` |
| `init-db.ps1` | 初始化数据库：执行 migration 与 seed，支持重建库 | `-Reset`, `-SeedOnly` |
| `run-tests.ps1` | 历史脚本：运行 `tests/` 下的 Java 入口类（保留并行） | 无 |
| `smoke-api-v3.ps1` | 当前推荐冒烟脚本：按现行 API 契约验证关键链路 | `-AppBase`, `-AdminBase`, `-AdminUser`, `-AdminPassword`, `-MemberUser`, `-MemberPassword`, `-ReportPath` |
| `audit-resource-origin.ps1` | 当前推荐资源来源审计：SQL + 源码扫描 + JSON 报告 | `-DbContainer`, `-DbUser`, `-DbPassword`, `-DbName`, `-ReportPath`, `-FailOnViolation` |
| `audit-home-network-hosts.ps1` | 首页网络主机审计：采样首页与 `/api/home/content`，输出外域 host 清单 | `-WebBase`, `-Samples`, `-TimeoutSec`, `-ReportPath`, `-FailOnViolation` |
| `migrate-external-assets.ps1` | 外链媒体迁移：dry-run/apply/rollback + 批次报告 | `-Apply`, `-RollbackBatch`, `-BatchNo`, `-Output`, `-BaselineOutput` |
| `check-product-detail-data.ps1` | 商品详情数据完整性检查与回填（简介图/详情图/规格核心项） | `-DbContainer`, `-DbUser`, `-DbPassword`, `-DbName`, `-ReportPath`, `-AutoFix`, `-FailOnViolation` |
| `diagnose-home-slow.ps1` | 首页慢加载诊断：性能采样 + 风险评分 + 报告落盘 | `-WebBase`, `-ApiBase`, `-LogTailLines`, `-ReportPath`, `-MarkdownPath` |
| `sync-carousel-overlay.ps1` | 同步轮播图 1~5 资产并标准化首页广告元数据 | `-DbHost`, `-DbPort`, `-DbUser`, `-DbPassword`, `-DbName`, `-Output`, `-SkipAdvertiseUpdate` |
| `check-docs.ps1` | 校验文档 frontmatter、事实快照、Markdown 清单漂移 | `-VerboseList`, `-RefreshFacts`, `-SkipCatalog` |
| `doc-catalog.ps1` | 生成/校验 Markdown 清单快照与报告 | `-Refresh`, `-CheckOnly`, `-Watch`, `-IntervalSec` |

---

## 5. 推荐工作流

### 首次拉起

```powershell
.\preflight-v3.ps1
.\start-v3.ps1 -Frontend
.\init-db.ps1
```

### 日常开发（修改代码后）

```powershell
# 只改了后端
.\restart-be.ps1

# 只改了前端
.\restart-fe.ps1

# 都改了
.\restart.ps1 -SkipBuild   # 若已构建过

# 不确定，全量重启
.\restart.ps1

# 接口冒烟（当前推荐）
.\smoke-api-v3.ps1

# 资源来源审计（违规即返回 1）
.\audit-resource-origin.ps1 -FailOnViolation

# 首页慢加载诊断（输出 JSON + Markdown 报告）
.\diagnose-home-slow.ps1
```

### 停止

```powershell
.\stop-v3.ps1                     # 停止全部（含 Docker）
.\stop-v3.ps1 -KeepInfra          # 只停后端/前端
.\stop-v3.ps1 -ForcePortKill      # 强制清理端口（有冲突时用）
```

---

## 6. 日志文件位置（`runtime-logs/`）

| 文件 | 内容 |
|---|---|
| `mall-app-api.log` / `-error.log` | App API 输出 |
| `mall-admin-api.log` / `-error.log` | Admin API 输出 |
| `mall-job.log` | Job 服务输出 |
| `mall-app-web.log` | app-web 前端输出 |
| `mall-admin-web.log` | admin-web 前端输出 |
| `build-output.log` | Maven 构建完整输出 |
| `backend-pids.txt` | 后端进程 PID 记录 |
| `frontend-pids.txt` | 前端进程 PID 记录 |
| `backend-build.fingerprint` | 构建指纹缓存（避免重复构建）|

---

## 7. 访问地址速查

| 服务 | 地址 |
|---|---|
| Admin Web | http://localhost:8090 |
| App Web | http://localhost:8091 |
| Admin API | http://localhost:18081 |
| App API | http://localhost:18080 |
| MySQL | localhost:13306 |
| Redis | localhost:16379 |
| Elasticsearch | localhost:9201 |
| RabbitMQ 控制台 | http://localhost:15673 |
| MinIO 控制台 | http://localhost:19001 |

---

## 8. 常见问题

1. **后端启动失败** → `.\logs.ps1 -Service app -IncludeErrors`
2. **端口冲突** → `.\stop-v3.ps1 -ForcePortKill`
3. **前端端口变了** → `.\status.ps1` 查看实际端口
4. **Docker 未启动** → 启动 Docker Desktop，再 `.\start-v3.ps1`
5. **数据异常** → `.\init-db.ps1 -Reset`
6. **文档检查失败** → `.\check-docs.ps1 -VerboseList`（若是事实变更导致漂移，执行 `.\check-docs.ps1 -RefreshFacts`）
7. **Markdown 清单漂移** → `.\doc-catalog.ps1 -Refresh`（需要实时更新可用 `.\doc-catalog.ps1 -Watch`）
8. **脚本报"字符串缺少终止符"** → 编码问题，见下方 §10 注意事项

---

## 9. 维护规则

1. 新增脚本时同步更新本文件与 `docs/04_release_runbook.md`。
2. 涉及破坏性操作（重置库、强制杀进程）必须在文档中明确风险。
3. 新脚本需补齐：用途、参数、示例命令、失败排查四类信息。


---

## 10. 注意事项 & 历史 Bug 修复

### 执行环境要求

所有脚本必须使用 **PowerShell 7 (`pwsh`)** 执行，不要用旧版 `powershell.exe`（PS5）：

```powershell
# ✅ 正确
pwsh -ExecutionPolicy Bypass -File .\menu.ps1

# ⚠️ 可能出现中文乱码或语法错误（Windows PowerShell 5 不支持无 BOM 的 UTF-8）
powershell -ExecutionPolicy Bypass -File .\menu.ps1
```

通过菜单操作时，只需确保用 `pwsh` 打开终端再运行 `menu.ps1`，子脚本调用均在同一 pwsh 进程内执行，无需额外设置。

### 已修复的 Bug

| 修复日期 | 脚本 | 问题描述 | 修复方式 |
|---|---|---|---|
| 2026-02-24 | `menu.ps1` | 菜单参数通过字符串数组转发，`-Frontend` 被当作位置参数，触发 `BackendHealthTimeoutSec` 类型转换错误 | `Run-Script` 改为 hashtable splat：`Run-Script "start-v3.ps1" @{ Frontend = $true }` |
| 2026-02-24 | `status.ps1` | `"..."` 中的弯引号导致 PS5 解析报错 | 改为单引号 `'...'` |
| 2026-02-24 | 全部 14 个脚本 | 控制台输出为英文 | 全部翻译为中文 |
