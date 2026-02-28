---
owner: backend
updated: 2026-02-23
scope: mall-v3
audience: dev,qa,ops
doc_type: guide
---
# 09 - 错误处理指南

> 文档导航：返回 [docs/README.md](README.md)。

## 1. 统一响应模型

后端业务响应统一使用 `CommonResult<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

标准错误码（`ResultCode`）：

| code | 含义 |
|---:|---|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录或 token 失效 |
| 403 | 无权限 |
| 500 | 业务失败/系统异常 |

说明：

1. 业务异常通常返回 HTTP 200 + `code` 字段。
2. 安全异常（未登录/无权限）由 Spring Security 组件直接返回 HTTP 401/403。

## 2. 后端异常处理链路

### 2.1 业务与参数异常（`GlobalExceptionHandler`）

| 异常 | 返回 |
|---|---|
| `ApiException` | `CommonResult.failed(...)` |
| `MethodArgumentNotValidException` / `BindException` | `code=400`，返回 `字段: 错误信息`，多条用 `; ` 拼接 |
| `ConstraintViolationException` | `code=400`，返回约束消息，多条用 `; ` 拼接 |
| `MissingServletRequestParameterException` | `code=400`，提示缺少参数 |
| `MethodArgumentTypeMismatchException` | `code=400`，提示参数类型错误 |
| `HttpMessageNotReadableException` | `code=400`，提示请求体格式错误 |
| `HttpRequestMethodNotSupportedException` | `code=500`，提示不支持的请求方法 |
| `NoResourceFoundException` | `code=500`，提示资源不存在 |
| `IllegalArgumentException` | `code=500`，返回异常消息 |
| 其他异常 | `code=500`，`系统内部错误` |

### 2.2 安全异常（独立处理）

| 组件 | HTTP 状态码 | body |
|---|---:|---|
| `RestAuthenticationEntryPoint` | 401 | `CommonResult.unauthorized(...)` |
| `RestAccessDeniedHandler` | 403 | `CommonResult.forbidden(...)` |

> 结论：大多数业务异常返回 HTTP 200 + `code` 字段；安全异常返回 HTTP 401/403。

## 3. 前端 SDK 行为（`frontend/packages/api-sdk/src/index.ts`）

当前拦截器行为：

1. 请求拦截：若 `localStorage.token` 存在，自动注入 `Authorization: Bearer <token>`。
2. 响应成功分支（仅 HTTP 2xx）：`code === 200` 正常返回；`code === 401` 清理 token 并跳转 `/login`；其他非 200 抛 `Error(message)`。
3. 响应失败分支（如安全组件直接返回 HTTP 401/403）：当前仅 `Promise.reject(error)`，不会在 SDK 内自动跳转登录。
4. SDK 未内置全局 Toast；页面层自行决定提示策略（Vant `showToast` / Element Plus `ElMessage`）。

## 4. Service 层实践

推荐：

```java
if (coupon == null) {
    Asserts.fail("优惠券不存在");
}
```

补充说明：当前代码中仍有部分 Service 直接抛 `RuntimeException`，这类异常会被 `GlobalExceptionHandler` 的兜底分支转换为 `code=500` + `系统内部错误`。

不推荐：

1. 在 Controller 里 `try/catch` 后手工拼错误响应。
2. 在 Service 层吞异常只打印日志。

## 5. 常见问题排查

| 现象 | 排查方向 |
|---|---|
| `code=400` | 参数名、参数类型、请求体 JSON 结构 |
| 401 | token 是否存在/过期/已被拉黑 |
| 403 | Admin 角色是否绑定 `ums_resource` 资源 |
| 500 | 查看 `runtime-logs/*.log` 定位堆栈（含异步 `mall-job`） |

```powershell
Get-Content .\runtime-logs\mall-app-api.log -Tail 200 -Wait
Get-Content .\runtime-logs\mall-admin-api.log -Tail 200 -Wait
Get-Content .\runtime-logs\mall-job.log -Tail 200 -Wait
```

## 6. 文档同步规则

1. 新增错误码或异常映射时，必须更新本文。
2. 修改 SDK 拦截策略时，必须更新本文第 3 节。
3. 新增安全异常处理器时，必须补充第 2.2 节。
