# Plan — 门户化整合实施计划

状态：Draft（规划阶段）

## Milestones（里程碑）
1. 阶段 0：确认信息（MCP 前端/后端信息、部署同域策略）
2. 阶段 1：搭建 Portal 壳（路由/守卫/Auth/Axios 骨架）
3. 阶段 2：迁移 Chat 前端（原样迁移 + 修正 import/路由）
4. 阶段 3：迁移 MCP 前端（原样迁移 + 代理 /mcp-api）
5. 阶段 4：后端抽取 agent-core（行为等价）并接入 Chat/MCP
6. 阶段 5：联调与验收（认证流、SSE、MCP）
7. 阶段 6：收尾（文档/脚本/回滚点）

## Work Breakdown（任务拆分 — 详情见 tasks.md）
（负责人/估时为示例占位，可在评审时补充）

### Backend
- BE-01 抽取 `agent-core`（接口与默认实现，保留行为等价） — 负责人：TBD — 估时：0.5d
- BE-02 Chat 后端改依赖 `agent-core`（构建一起打包） — 负责人：TBD — 估时：0.5d
- BE-03 MCP 后端改依赖 `agent-core` — 负责人：TBD — 估时：0.5d

### Frontend (Portal)
- FE-01 初始化 Portal（路由/守卫/Auth store/Axios） — 负责人：TBD — 估时：0.5d
- FE-02 迁移 Chat 前端到 `features/chat`（修复 import 与路由） — 负责人：TBD — 估时：0.5d
- FE-03 迁移 MCP 前端到 `features/mcp`（修复 import 与路由） — 负责人：TBD — 估时：0.5–1d
- FE-04 Dev 代理与环境变量配置（/api、/mcp-api） — 负责人：TBD — 估时：0.25d

### Integration
- INT-01 联调认证流（登录/登出/回跳/刷新） — 负责人：TBD — 估时：0.25d
- INT-02 联调 Chat SSE（start/chunk/end/error） — 负责人：TBD — 估时：0.25d
- INT-03 联调 MCP 请求（代理与权限） — 负责人：TBD — 估时：0.25d

### Documentation
- DOC-01 更新 README/部署指南/启动脚本 — 负责人：TBD — 估时：0.25d

## Dependencies（依赖）
- MCP 前端源码位置与打包方式；MCP 后端地址与 API 前缀。
- 运维：同域部署与反向代理配置；如二级域名，CORS 与 Cookie Domain。
- 现有 Chat 登录接口与 Cookie 策略（HTTP-only/路径/过期）。

## Test & Observability（测试与观测）
- 认证流：未登录直达 `/chat` → 跳 `/login`；登录后回跳；已登录访问 `/login` → 跳 `/home`；登出→受保护路由跳登录。
- 功能入口：从 `/home` 进入 Chat、MCP；刷新/回退/深链接可用。
- SSE：多轮对话不异常；错误事件上报清晰；客户端重连策略验证。
- MCP：通过 `/mcp-api` 代理访问后端，权限与跨域策略正确。
- 观测：前端关键事件（登录/跳转/SSE 错误）与后端工具调用/时延/错误均可观测。

## Rollback（回滚）
- 前端：保留原 chat/mcp 前端目录与构建脚本；如出现问题，暂时恢复原路径部署。
- 后端：`agent-core` 替换以行为等价为前提；保留替换前分支，必要时逐模块回退。

## DoR / DoD（就绪/完成）
- DoR：
  - Spec（spec.md）已评审通过；
  - MCP 前端与后端信息齐备；
  - 部署/代理策略明确（同域/二级域名）。
- DoD：
  - 单点登录 + 深链接 + 刷新/回退全部通过；
  - Chat SSE 与 MCP 功能联调通过；
  - 文档/脚本更新；回滚预案可用。

## Sign-off（评审与签署）
- 审阅人：前端负责人 / 后端负责人 / 运维负责人 / Owner
- 通过条件：三方评审通过 + 风险与回滚认可

