---
owner: docs
updated: 2026-02-26
scope: mall-v3
audience: dev,qa,ops
doc_type: standard
---

# 06 — Markdown 文档统一格式规范

> 文档导航：返回 [docs/README.md](README.md)。

本规范适用于 `project_mall_v3` 内所有 Markdown 文档。目标是让文档风格像代码风格一样**统一、可检索、可维护**。

---

## 1. 必填头部元信息（Frontmatter）

每份 Markdown 文档开头必须包含 YAML frontmatter：

```yaml
---
owner: <module-or-role>
updated: YYYY-MM-DD
scope: mall-v3
audience: dev,qa,ops
doc_type: overview|architecture|contract|migration|runbook|guide|analysis|index|standard|reference|agent-policy
---
```

| 字段 | 说明 | 示例 |
|---|---|---|
| `owner` | 负责维护该文档的模块或角色 | `backend`, `frontend`, `api`, `ops`, `docs`, `project-core` |
| `updated` | 最后一次实质性更新日期（非格式修正） | `2026-02-23` |
| `scope` | 固定为 `mall-v3` | `mall-v3` |
| `audience` | 目标读者，逗号分隔 | `dev,qa`, `dev,ops`, `all` |
| `doc_type` | 文档类型，便于分类检索 | 见下方类型清单 |

### doc_type 类型清单

| 类型 | 说明 | 典型文档 |
|---|---|---|
| `overview` | 项目/模块总览 | `README.md` |
| `architecture` | 架构设计 | `00_architecture.md` |
| `contract` | 接口契约/协议 | `02_api_contract.md` |
| `migration` | 数据迁移策略 | `03_data_migration.md` |
| `runbook` | 操作手册（启停/发布/排障） | `04_release_runbook.md` |
| `guide` | 使用指南/开发指南 | `11_contributing.md` |
| `analysis` | 分析报告 | `05_backend_frontend_api_usage.md` |
| `index` | 导航索引 | `docs/README.md` |
| `standard` | 规范标准 | `06_doc_style.md` |
| `reference` | 参考手册（术语表/ADR） | `08_glossary.md`, `12_adr_log.md` |
| `agent-policy` | AI 代理策略 | `AGENTS.md` |

---

## 2. 文档结构规范

### 2.1 推荐章节顺序

```text
# 标题

> 导航行（可选）

## 1. 背景与目标
## 2. 适用范围
## 3. 关键内容（核心章节）
## 4. 验证步骤
## 5. 维护规则
## 附录 / 变更记录（可选）
```

### 2.2 标题层级规则

| 层级 | 用途 | 规则 |
|---|---|---|
| `#` (H1) | 文档标题 | 每份文档**有且仅有一个** |
| `##` (H2) | 一级章节 | 用编号前缀（`## 1.`、`## 2.`） |
| `###` (H3) | 二级章节 | 用编号前缀（`### 2.1`、`### 2.2`） |
| `####` (H4) | 三级章节 | 尽量避免超过 H4 深度 |

### 2.3 导航行

每份文档（除根 `README.md`）建议在标题下方添加导航：

```markdown
> 文档导航：返回 [docs/README.md](README.md)。
```

---

## 3. 命名与放置规范

### 3.1 全局文档命名

`docs/` 下的全局文档采用编号前缀：

```text
docs/
├── README.md                    # 文档导航中心
├── 00_architecture.md           # 编号从 00 开始
├── 01_roadmap.md
├── 02_api_contract.md
├── ...
└── 12_adr_log.md
```

**编号分区**：
- `00–05`：核心文档（架构/路线/契约/迁移/运行/映射）
- `06–07`：治理文档（格式规范/模块盘点）
- `08–12`：参考文档（术语/错误处理/安全/贡献/ADR）
- 新增文档取当前最大编号 + 1

### 3.2 模块文档命名

模块级文档优先就近放置，常用文件名：

| 文件名 | 用途 |
|---|---|
| `README.md` | 模块总览（必备） |
| `CONTRACT.md` | 接口契约/稳定性承诺 |
| `RUNBOOK.md` | 操作手册/排障指南 |
| `CHANGELOG.md` | 变更日志 |

### 3.3 放置判定规则

> 如果只改一个模块时需要查该文档 → 放模块目录。
> 如果改两个以上模块时需要查 → 放 `docs/`。

---

## 4. 内容书写规范

### 4.1 路径引用

- 使用**仓库相对路径**，如 `backend/mall-app-api/src/main/resources/application.yml`
- 不使用绝对路径（除非是命令示例中必须使用）
- 目录路径以 `/` 结尾：`backend/mall-modules/`
- 对“调用映射类文档”，优先记录到文件级路径，不强依赖行号（行号随格式调整容易失真）

### 4.2 代码块

- 所有命令和代码片段必须放在代码块中
- 必须标注语言标识：`powershell`、`java`、`sql`、`json`、`yaml`、`text` 等
- `project_mall_v3/scripts/*.ps1` 命令统一建议格式：`pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File <script.ps1> [args]`

### 4.3 表格

- 接口、字段、配置项、状态列表等**优先使用表格**而非列表
- 表格对齐符 `|---|` 不要求列宽对齐（可读性优先）

### 4.4 Mermaid 图

- 流程图用 `flowchart TD` 或 `flowchart LR`
- 时序图用 `sequenceDiagram`
- ER 图用 `erDiagram`
- 节点文本使用中文（面向中文读者）

### 4.5 关键信息高亮

| 标记 | 用途 | 语法 |
|---|---|---|
| 加粗 | 关键词、要点 | `**关键词**` |
| 行内代码 | 类名、方法名、配置项、路径 | 反引号包裹 |
| 引用块 | 注意事项、补充说明 | `> 注意：...` |
| 警告 | 破坏性操作提醒 | `> ⚠️ 警告：...` |

### 4.6 术语一致性

- 项目术语以 [08_glossary.md](08_glossary.md) 为准
- 同一概念只使用一个名称，不要交替使用同义词
- 首次出现的缩写必须给出全称

---

## 5. 文档验证规范

### 5.1 命令可执行性

- 文档中的命令必须可直接复制粘贴执行
- 路径必须与仓库实际结构一致
- 指定工作目录时使用 `cd` 命令明确

### 5.2 关键流程验收

- 避免"只描述不验证"
- 关键流程必须附**最小验收步骤**（命令 + 预期结果）

---

## 6. 新文档模板

```text
---
owner: <owner>
updated: YYYY-MM-DD
scope: mall-v3
audience: dev,qa
doc_type: guide
---

# {编号} — {标题}

> 文档导航：返回 [docs/README.md](README.md)。

---

## 1. 背景与目标
{为什么需要这份文档？}

---

## 2. 核心内容
{文档主体}

---

## 3. 验证步骤
{最小验收命令}

---

## 4. 维护规则
1. 何时更新本文档
2. 更新时需同步更新哪些其他文档
```

---

## 7. 变更触发矩阵

| 变更类型 | 需更新的文档 |
|---|---|
| 接口签名/行为变更 | `02_api_contract.md` + API SDK + `05` |
| 跨模块流程变更 | `00_architecture.md` |
| 数据库结构变更 | `data/migration/*.sql` + `03_data_migration.md` |
| 错误码新增 | `09_error_handling.md` |
| 安全策略变更 | `10_security_guide.md` |
| 启动/部署/排障 | `04_release_runbook.md` + `scripts/README.md` |
| 架构决策 | `12_adr_log.md` |
| 新术语/缩写 | `08_glossary.md` |
| 协作流程变更 | `11_contributing.md` |

---

## 8. 文档治理检查

### 8.1 自动化校验

```powershell
cd d:/Desktop/work/mall/project_mall_v3
.\scripts\check-docs.ps1          # 检查 frontmatter + 事实快照漂移
.\scripts\check-docs.ps1 -VerboseList  # 详细列表
.\scripts\check-docs.ps1 -RefreshFacts # 代码事实变更后刷新快照
```

当前脚本自动校验以下内容：
- Markdown 文件是否包含 frontmatter
- 是否包含 `owner/updated/scope/audience/doc_type` 五个必填键
- `scope` 是否为 `mall-v3`
- 文档事实快照是否与当前代码一致（migration/seed、控制器与端点估算、前端视图与入口文件、脚本清单）

标题层级、代码块语言标识、术语一致性等仍需人工 Review。

### 8.2 时效要求

- 核心文档（00–05）超过 **60 天**未更新需复核
- 辅助文档（06–12）超过 **90 天**未更新需复核
- 模块文档随代码变更同步更新

### 8.3 人工 Review 清单

- [ ] Frontmatter 完整且 `updated` 日期正确
- [ ] 标题层级正确（只有一个 H1）
- [ ] 代码块有语言标识
- [ ] 路径与仓库结构一致
- [ ] 术语使用与 `08_glossary.md` 一致
- [ ] 全局文档索引（`docs/README.md`）已更新
