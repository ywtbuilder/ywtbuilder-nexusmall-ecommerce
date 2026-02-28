-- Flyway V1: Baseline schema
-- ============================================================
-- V3 使用 V1 的完整 DDL 作为基线。
-- 新环境部署流程：
--   1. Docker init-db.d/ 自动导入 V1 基础 schema（来自 project_mall_v1 的 mall.sql）
--   2. Flyway baseline-on-migrate=true 会标记 V1 已应用，从 V2 开始增量迁移
--
-- 如果 Flyway 在全新空库上运行（无 init-db），此脚本确保不会报错。
-- V2 及后续迁移均使用 IF NOT EXISTS / IF EXISTS 保护。
-- ============================================================

-- 确认数据库连接正常
SELECT 1;
