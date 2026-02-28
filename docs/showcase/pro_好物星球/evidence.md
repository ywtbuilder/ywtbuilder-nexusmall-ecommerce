# pro_好物星球 展示证据页

## 一句话价值

面向真实业务链路的全栈电商平台，覆盖买家端、管理端与七大业务域，支持本地一键复现。

## 1 分钟演示视频

- 文件：`docs/showcase/pro_好物星球/demo.mp4`
- 建议镜头：首页聚合 -> 搜索结果 -> 商品详情 -> 加购 -> 管理端商品管理

## 3 张关键截图

1. `shot-01.png`：买家端首页（轮播 + 推荐商品）
2. `shot-02.png`：搜索结果（ES 命中）或降级日志
3. `shot-03.png`：管理端商品/订单管理页面

## 一键运行命令（PowerShell 7）

```powershell
cd ywtbuilder-nexusmall-ecommerce
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\preflight-v3.ps1
pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-v3.ps1 -Frontend
```

## 核心技术决策

1. 双端 BFF：买家端与管理端独立聚合层，降低前端耦合。
2. 搜索降级：ES 不可用时自动回退 MySQL，保证可用性。
3. 图片服务：图片 BLOB 入库，离线环境仍可完整展示。

## 性能/稳定性证据

| 指标 | 目标 | 当前结果 | 说明 |
|---|---:|---:|---|
| 首页首屏加载 | <= 5s | 待填充 | 建议填入生产前端模式数据 |
| `GET /home/content` 响应 | <= 300ms | 待填充 | 在本机环境测量 |
| 搜索降级可用性 | 100% | 待填充 | 关闭 ES 后验证 |

## 面试可提问点

1. 为什么采用 BFF 而不是单网关直连业务服务？
2. ES 降级触发条件是什么，怎么避免误降级？
3. 订单超时取消为什么选 MQ 而不是轮询任务？
4. 图片入 BLOB 的收益与代价分别是什么？
5. 多模块 Maven 项目如何做依赖边界治理？


