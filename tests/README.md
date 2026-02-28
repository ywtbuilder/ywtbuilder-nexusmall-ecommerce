---
owner: qa
updated: 2026-02-23
scope: mall-v3
audience: dev,qa
doc_type: guide
---
# Mall V3 — 跨模块集成测试

> 文档导航：统一入口见 [../docs/README.md](../docs/README.md)。

本目录存放 **独立运行的黑盒/灰盒测试**，直接通过 HTTP 请求验证已启动的服务。  
它们 **不是 Maven 模块**（不在 backend/pom.xml 的 `<modules>` 列表内），不会被 `mvn test` 或 `mvn verify` 自动执行。

## 目录结构

```text
tests/
├─ README.md
├─ contract/
│  └─ AppApiContractTest.java      # API 契约测试
└─ integration/
   └─ OrderFlowIntegrationTest.java  # 端到端集成测试
```

| 目录 | 说明 |
|------|------|
| `contract/` | API 契约测试 — 验证端点可用性和 CommonResult 结构 |
| `integration/` | 端到端集成测试 — 验证完整业务链路（登录→下单→管理） |

> **注意**：编译产生的 `.class` 文件和 `com/` 目录是运行产物，已通过根目录 `.gitignore` 忽略（`*.class`），不应提交到版本控制。

## 前置条件

1. 本地服务已启动（`scripts/start-v3.ps1`）
2. App API（18080）和 Admin API（18081）均已 Healthy
3. 数据库已初始化种子数据（`scripts/init-db.ps1`）
4. Java 17+ 已安装且在 PATH 中可用

## 运行方式

### 推荐方式：通过脚本
```powershell
.\scripts\run-tests.ps1
```

脚本会自动：
1. 检查 18080 和 18081 的 `/actuator/health`
2. 编译 `.java` 文件
3. 依次运行两个测试入口类
4. 任一编译或执行失败返回非 0 退出码

### 手动编译运行
```bash
# 编译
javac tests/contract/AppApiContractTest.java
javac tests/integration/OrderFlowIntegrationTest.java

# 运行
java -cp tests/contract com.mall.tests.contract.AppApiContractTest
java -cp tests/integration com.mall.tests.integration.OrderFlowIntegrationTest
```

## 与 Maven 测试的关系

| 测试类型 | 位置 | 执行方式 | 说明 |
|---|---|---|---|
| 模块内单元测试 | `backend/*/src/test/` | `cd backend && ./mvnw.cmd test` | Maven 生命周期内自动执行 |
| 本目录黑盒测试 | `tests/contract/`、`tests/integration/` | `.\scripts\run-tests.ps1` | 独立于 Maven，需实际运行的服务 |
| 前端 E2E | `frontend/apps/e2e/` | `cd frontend && pnpm test:e2e` | Playwright 浏览器端到端 |

这是 **有意设计**，不是遗漏：这些测试需要实际运行的服务，不适合放入 Maven 测试阶段。

## 维护建议

1. 新增测试文件时，同步更新 `scripts/run-tests.ps1` 中的编译和执行命令。
2. 编译产物（`.class`、`com/`）不要提交版本控制。
3. 测试期望的默认账号/接口行为应与 `data/seed/` 和 `docs/02_api_contract.md` 保持一致。
