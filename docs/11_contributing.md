---
owner: project-core
updated: 2026-02-26
scope: mall-v3
audience: dev,qa
doc_type: guide
---
# 11 - 贡献指南

> 文档导航：返回 [docs/README.md](README.md)。

## 1. 基本原则

1. 代码改动与文档改动同 PR 提交。
2. 接口改动必须同步 SDK 与 `docs/02_api_contract.md`。
3. 数据结构改动必须新增迁移脚本并同步 `docs/03_data_migration.md`。

## 2. 环境要求

| 工具 | 建议版本 |
|---|---|
| Java | 17 |
| Maven | 3.9+（或 `mvnw`） |
| Node.js | 20+ |
| pnpm | 9+ |
| Docker | 最新稳定版 |
| PowerShell | 7+（`pwsh`，推荐） |

## 3. 推荐开发流程

### 3.1 拉起环境

```powershell
cd d:/Desktop/work/mall/project_mall_v3
./scripts/preflight-v3.ps1
./scripts/start-v3.ps1 -Frontend
./scripts/init-db.ps1
```

### 3.2 开发改动

1. 后端逻辑放 `module-*`，BFF Controller 保持薄层。
2. 前端请求统一走 `@mall/api-sdk`，页面不写裸 `axios`。
3. 优先改源文件（`.vue`、`.ts`、`.java`），不要优先改生成产物。

### 3.3 提交前校验

```powershell
# 后端
cd d:/Desktop/work/mall/project_mall_v3/backend
./mvnw.cmd compile
./mvnw.cmd test -pl mall-shared/shared-common,mall-shared/shared-security
./mvnw.cmd package -DskipTests

# 前端
cd d:/Desktop/work/mall/project_mall_v3/frontend
pnpm type-check
pnpm build:admin
pnpm build:app

# 文档
cd d:/Desktop/work/mall/project_mall_v3
./scripts/check-docs.ps1
```

可选：

```powershell
./scripts/run-tests.ps1
```

## 4. 提交规范

建议采用 Conventional Commits：

```text
<type>(<scope>): <description>
```

常用 `type`：`feat`、`fix`、`refactor`、`docs`、`test`、`chore`。

## 5. 分支建议

建议命名：

1. `feature/<name>`
2. `fix/<name>`
3. `refactor/<name>`
4. `docs/<name>`

## 6. Code Review 检查点

1. 是否破坏模块边界（BFF/Domain/Shared）。
2. 是否有越权风险（用户资源归属校验）。
3. 是否有契约漂移（Controller 与 SDK 不一致）。
4. 是否补齐必要文档。

## 7. CI 事实说明（当前）

`.github/workflows/ci.yml` 当前执行：

1. 后端：`compile` + shared 单测 + `package -DskipTests`
2. 前端：`pnpm type-check` + `build:admin` + `build:app`

触发规则（当前）：

1. `push`：`main`、`develop`
2. `pull_request`：目标分支 `main`
3. `paths-ignore`：`*.md`、`docs/**`（纯文档改动默认不触发 CI）

当前不包含：

1. 前端 lint 门禁
2. E2E 自动门禁
3. 全量后端模块单测门禁

## 8. 常见任务落点

| 任务 | 主要目录 |
|---|---|
| 新增 App 接口 | `backend/mall-app-api` + `backend/mall-modules/module-*` |
| 新增 Admin 接口 | `backend/mall-admin-api` + `backend/mall-modules/module-*` |
| 改鉴权 | `backend/mall-shared/shared-security` + 两端 `SecurityConfig` |
| 改全局错误处理 | `backend/mall-shared/shared-web` |
| 改前端页面 | `frontend/apps/mall-*-web/src/views` |
| 改 SDK | `frontend/packages/api-sdk/src` |
| 改迁移 | `data/migration` / `data/seed` |

## 9. 文档同步清单

- 接口变更：`docs/02_api_contract.md`
- 迁移变更：`docs/03_data_migration.md`
- 启停流程变更：`docs/04_release_runbook.md`
- 错误策略变更：`docs/09_error_handling.md`
- 安全策略变更：`docs/10_security_guide.md`
