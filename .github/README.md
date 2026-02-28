---
owner: ci
updated: 2026-02-23
scope: mall-v3
audience: dev,qa,ops
doc_type: guide
---

# CI 模块说明（GitHub Actions）

> 文档导航：统一入口见 [../docs/README.md](../docs/README.md)。

## 1. 入口

CI 配置文件：`.github/workflows/ci.yml`

## 2. 触发策略

| 事件 | 目标分支 |
|---|---|
| `push` | `main`, `develop` |
| `pull_request` | `main` |

### 2.1 忽略路径（`paths-ignore`）

```yaml
paths-ignore:
  - '*.md'      # 仅匹配仓库根目录的 Markdown 文件
  - 'docs/**'   # 匹配 docs/ 目录下的所有文件
```

**注意**：`*.md` 仅匹配仓库根目录的 `.md` 文件（如 `README.md`、`CONTRIBUTING.md`），**不**匹配子目录下的 Markdown（如 `tests/README.md`、`infra/README.md`、`.github/README.md`）。  
如需真正做到"仅改任意 Markdown 不触发 CI"，应将 `*.md` 改为 `**/*.md`。

### 2.2 并发控制（`concurrency`）

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

同一分支的重复推送会自动取消旧的 CI 运行，减少资源浪费。

## 3. 全局环境变量

```yaml
env:
  JAVA_VERSION: '17'
  NODE_VERSION: '20'
  PNPM_VERSION: '9'
```

## 4. 流水线结构

### 4.1 Backend Job

| 维度 | 配置 |
|---|---|
| 运行环境 | `ubuntu-latest` |
| JDK | 17（Temurin 发行版，通过 `actions/setup-java@v4` 配置，走 Maven 缓存） |
| 工作目录 | `backend/` |
| CI 依赖服务 | MySQL 8.0（端口 `13306`）、Redis 7-alpine（端口 `16379`） |

执行步骤：

1. `./mvnw compile -q -B` — 编译
2. `./mvnw test -q -B -pl mall-shared/shared-common,mall-shared/shared-security` — 单元测试（仅 shared 模块）
3. `./mvnw package -DskipTests -q -B` — 打包
4. **仅 `main` 分支**：上传 `backend-jars` 产物（`mall-app-api`、`mall-admin-api`、`mall-job` 的 jar），保留 7 天

### 4.2 Frontend Job

| 维度 | 配置 |
|---|---|
| 运行环境 | `ubuntu-latest` |
| Node.js | 20（通过 `actions/setup-node@v4`，走 pnpm 缓存） |
| pnpm | 9（通过 `pnpm/action-setup@v4`） |
| 工作目录 | `frontend/` |

执行步骤：

1. `pnpm install --frozen-lockfile` — 安装依赖
2. `pnpm type-check` — 类型检查
3. `pnpm build:admin` — 构建管理后台
4. `pnpm build:app` — 构建 C 端
5. **仅 `main` 分支**：上传 `frontend-dist` 产物（`mall-admin-web/dist`、`mall-app-web/dist`），保留 7 天

## 5. Actions 版本清单

| Action | 版本 |
|---|---|
| `actions/checkout` | v4 |
| `actions/setup-java` | v4 |
| `actions/setup-node` | v4 |
| `pnpm/action-setup` | v4 |
| `actions/upload-artifact` | v4 |

## 6. 维护注意事项

1. 如果新增 Maven 模块或修改构建流程，需同步调整 backend job 的 `test -pl` 清单与缓存路径。
2. 如果调整前端 workspace 结构，需同步调整 frontend job 的 `working-directory` 与构建脚本。
3. 如果希望文档改动也触发 CI，需要移除 `paths-ignore` 中的 `*.md` / `docs/**`。
4. 修改 MySQL/Redis 版本时，需同步调整 `infra/docker-compose.local.yml` 与 `backend/*/application.yml` 中的连接配置。
5. CI 中 MySQL 的 `MYSQL_ROOT_PASSWORD` 和 `MYSQL_DATABASE` 必须与后端测试配置一致。

