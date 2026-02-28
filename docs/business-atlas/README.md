---
owner: docs
updated: 2026-02-27
scope: mall-v3
audience: dev,qa,ops,product,audit,user
doc_type: business-atlas-index
---

# Mall V3 业务图谱（Business Atlas）

> 定位：该目录用于沉淀“全项目业务全景 + 业务逻辑链路 + 证据映射”的长期文档体系，支持研发、产品、运营、审计与用户视角统一理解。

## 1. 阅读路径

1. 先读 [总导航](00_navigation/README.md)
2. 再读 [业务上下文图](01_context/system-context.md)
3. 再读 [能力地图](02_capability_map/capability-map.md)
4. 然后按目标选择：
   - 研发：`03_domain_model` + `04_core_flows` + `06_data_flow`
   - 产品/运营：`02_capability_map` + `04_core_flows`
   - 运维/审计：`01_context` + `08_evidence`
   - 用户体验：`04_core_flows`（用户视角章节）

## 2. 目录结构

- [00_navigation](00_navigation/README.md)：导航、分卷规则、术语入口
- [01_context](01_context/system-context.md)：系统上下文、边界与参与者
- [02_capability_map](02_capability_map/capability-map.md)：业务能力地图
- [03_domain_model](03_domain_model/domain-model.md)：领域模型与核心实体关系
- [04_core_flows](04_core_flows/user-journeys.md)：用户端与管理端核心流程
- [05_sequence](05_sequence/key-sequences.md)：关键时序图
- [06_data_flow](06_data_flow/api-to-db-to-ui.md)：接口到落库再到页面消费映射
- [07_admin_ops](07_admin_ops/admin-linkage.md)：后台管理链路全景
- [08_evidence](08_evidence/evidence-index.md)：证据来源索引与校验方法
- [09_appendix](09_appendix/glossary-and-rules.md)：术语、约束与维护规则

## 3. 写作原则

1. 所有结论必须可追溯到至少一种事实来源（代码、SQL、日志、测试、脚本）。
2. 图统一使用 Mermaid，避免二进制图源难以审查。
3. 每章必须包含“适用范围、事实来源、已验证项、待补项”。
4. 禁止复制粘贴式重复描述，优先“链接 + 精确差异”。

## 4. 与现有文档的关系

- 架构基线：[`../00_architecture.md`](../00_architecture.md)
- 接口契约：[`../02_api_contract.md`](../02_api_contract.md)
- 前后端调用映射：[`../05_backend_frontend_api_usage.md`](../05_backend_frontend_api_usage.md)
- 模块与缺口：[`../07_module_inventory_and_doc_gap.md`](../07_module_inventory_and_doc_gap.md)
- 文档同步机制：[`../14_doc_sync_system.md`](../14_doc_sync_system.md)

## 5. 当前状态

- 本次为 V1 初始落地：已建分卷结构与首批基线内容。
- 下一阶段将按业务域扩充到“可审计级”深度，并持续回写证据索引。