---
owner: docs
updated: 2026-02-27
scope: mall-v3
audience: all
doc_type: navigation
---

# 业务图谱导航

## 1. 分卷清单

| 卷号 | 名称 | 目标问题 |
|---|---|---|
| 01 | 系统上下文 | 这个系统服务谁、边界在哪里、依赖哪些外部系统？ |
| 02 | 能力地图 | 业务能力有哪些、每块归属谁、依赖什么？ |
| 03 | 领域模型 | 核心实体/聚合/状态如何关联？ |
| 04 | 核心流程 | 用户端和后台端关键业务怎么走？ |
| 05 | 时序图 | 请求在前后端与中间件之间如何流转？ |
| 06 | 数据流 | API 字段如何映射到数据库并回流到页面？ |
| 07 | 后台运营 | 管理端页面到后端落库的全链路是什么？ |
| 08 | 证据库 | 每个结论来自哪些代码/SQL/日志/测试？ |
| 09 | 附录 | 术语、约束、维护规范 |

## 2. 统一章节模板

每个章节建议按以下顺序编写：

1. 章节目标
2. 适用范围
3. 关键图（Mermaid）
4. 事实来源（文件路径）
5. 关键结论
6. 验证点
7. 待补清单

## 3. 命名与维护约定

1. 文件名统一使用 `kebab-case`。
2. 章节编号与目录编号一致，禁止跨卷乱序。
3. 每次新增/重构业务链路，至少更新：
   - `04_core_flows`
   - `06_data_flow`
   - `08_evidence`
4. 每次版本迭代在根 `README` 的 Changelog 记录“变更点/验证点/新增规律/遗留问题”。

## 4. 跨文档跳转锚点

- 架构基线：[`../../00_architecture.md`](../../00_architecture.md)
- API 契约：[`../../02_api_contract.md`](../../02_api_contract.md)
- 前后端调用映射：[`../../05_backend_frontend_api_usage.md`](../../05_backend_frontend_api_usage.md)
- 模块缺口：[`../../07_module_inventory_and_doc_gap.md`](../../07_module_inventory_and_doc_gap.md)
- 文档联动：[`../../14_doc_sync_system.md`](../../14_doc_sync_system.md)