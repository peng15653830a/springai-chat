# Tasks — 可执行任务清单（spec‑kit）

状态：Draft（规划阶段）  
说明：每个任务具备明确依赖与验收标准（DoD），PR 请引用任务编号。

—

## Backend

### BE-01 抽取 agent-core（行为等价）
- 描述：抽取 Chat/MCP 公共能力（ChatClientManager、ModelProviderFactory、MultiModelProperties、WebSearchTool、MarkdownNormalizer、记忆装配接口等）到内部模块，不改变现有行为与对外接口。
- 依赖：无（可并行准备）。
- 验收（DoD）：
  - [ ] Chat/MCP 后端在切换到 core 前均可独立编译通过；
  - [ ] core 提供的 bean/配置不与现有自动装配冲突（OpenAI 默认装配继续禁用）；
  - [ ] 核心单元测试/最小回归通过（如 DeepSeek/GreatWall 配置装配）。

### BE-02 Chat 后端依赖 agent-core
- 描述：Chat 后端移除重复实现，改为依赖 core；构建产物将 core 一并打入。
- 依赖：BE-01。
- 验收（DoD）：
  - [ ] Chat 编译/启动通过；
  - [ ] 聊天流式（SSE）行为与旧版一致；
  - [ ] 关键日志与错误处理路径保持一致。

### BE-03 MCP 后端依赖 agent-core
- 描述：MCP 后端移除重复实现，改为依赖 core；构建产物将 core 一并打入。
- 依赖：BE-01。
- 验收（DoD）：
  - [ ] MCP 编译/启动通过；
  - [ ] 既有接口行为不变；
  - [ ] 与 Chat 共用配置不冲突。

—

## Frontend (Portal)

### FE-01 初始化 Portal 骨架
- 描述：建立 Vite+Vue3 门户骨架；新增 `/login`、`/home`；Pinia `auth`；Axios 客户端；全局路由守卫（未登录跳转登录；已登录访问登录页跳首页）。
- 依赖：无。
- 验收（DoD）：
  - [ ] `/login`、`/home` 路由可访问；
  - [ ] 未登录访问受保护路由会重定向至 `/login`；
  - [ ] .env 与 dev 代理文件具备基础配置。

### FE-02 迁移 Chat 前端至 features/chat
- 描述：将 Chat 页面/组件迁至 `features/chat`，修正 import/路径与路由；去除 Chat 自带登录守卫；SSE 工具改用 `shared/utils/useEventSource`（如已抽出）。
- 依赖：FE-01。
- 验收（DoD）：
  - [ ] `/chat/**` 原有页面功能正常；
  - [ ] SSE 正常输出 `start/chunk/end/error`；
  - [ ] Chat 内不再包含独立登录页/守卫。

### FE-03 迁移 MCP 前端至 features/mcp
- 描述：将 MCP 页面/组件迁至 `features/mcp`；配置 MCP 专属 API 基址或使用 `/mcp-api` 代理；共用 `auth` store。
- 依赖：FE-01。
- 验收（DoD）：
  - [ ] `/mcp/**` 页面功能正常；
  - [ ] 通过代理访问 MCP 后端成功；
  - [ ] MCP 不再呈现独立登录页。

### FE-04 Dev 代理与环境变量
- 描述：配置开发代理：`/api`→Chat 后端，`/mcp-api`→MCP 后端；`.env.*` 输出 `VITE_API_BASE`、`VITE_MCP_API_BASE`。
- 依赖：FE-01。
- 验收（DoD）：
  - [ ] 本地联调时 Chat/MCP 调用均成功；
  - [ ] Cookie 策略与代理兼容（同域优先）。

—

## Integration

### INT-01 联调认证流
- 描述：未登录访问受保护路由→跳登录；登录成功回跳目标；已登录访问 `/login`→跳 `/home`；登出后再访问受保护路由→跳登录。
- 依赖：FE-01、FE-02、FE-03。
- 验收（DoD）：
  - [ ] 全路径验证通过；
  - [ ] 刷新/回退不丢登录态。

### INT-02 联调 Chat SSE
- 描述：验证 Chat 流式：`start/chunk/end/error` 事件链路；网络异常/中断重连表现。
- 依赖：FE-02、BE-02。
- 验收（DoD）：
  - [ ] 多轮对话稳定；
  - [ ] 错误事件与用户提示一致；
  - [ ] 守卫与鉴权不中断 SSE。

### INT-03 联调 MCP 请求
- 描述：验证 MCP 前端通过 `/mcp-api` 代理访问后端；权限与跨域策略正确。
- 依赖：FE-03、BE-03。
- 验收（DoD）：
  - [ ] 关键 API 调用成功；
  - [ ] Cookie/CORS 策略验证通过。

—

## Documentation

### DOC-01 更新文档与脚本
- 描述：更新 README/部署指南/启动脚本；标记旧前端目录为 deprecated（短期保留）。
- 依赖：所有任务完成后。
- 验收（DoD）：
  - [ ] 文档完整、可按步骤复现；
  - [ ] 回滚步骤清晰可按需执行。

