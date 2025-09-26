标题：门户化前端整合与后端复用（Chat/MCP 一体化）

状态：Draft（规划阶段）  
Owner：项目负责人  
参与：前端负责人、后端负责人、运维负责人

—

一、Summary（一句话）
将 Chat 与 MCP 分散的前端整合为统一门户（Portal），后端抽取可复用的 agent-core 内核，实现一次登录、统一入口、最小改动复用通用能力，并为后续新增模块（如写小说）铺路。

二、Context（背景）
- 现状：Chat 与 MCP 两套前端各自维护，登录与守卫重复、入口分散；后端存在模型接入/工具调用等重复实现。
- 机会：现有功能不复杂、页面不多，主要是“挪文件 + 少量引用与路由修正”，可一次性完成整合。

三、Goals / Non-goals（目标/非目标）
Goals
- 统一登录：沿用 Chat 登录作为唯一认证源，一次登录，访问 Chat/MCP 均无需重复登录。
- 统一入口：新增门户首页（Home）集中展示可用功能卡片（Chat、MCP），支持深链接与回跳。
- 统一守卫：在 Portal 层做路由守卫与登录态管理，模块页不再自带登录页与守卫。
- 后端复用：抽取 agent-core（模型选路、Client 管理、工具与记忆装配接口），Chat/MCP 共享。
- 同域部署优先：简化 Cookie/CORS/SSE 复杂度。
- 快速落地：尽量“原样迁移页面”，改动集中在路由与 import；2–3 天完成。

Non-goals
- 本期不统一 UI 主题/布局样式（先通路，后打磨）。
- 不引入重量级跨语言编排框架；不重写现有业务逻辑。
- 不对外发布独立 jar 包（core 为内部模块，随业务模块打包）。

四、Success Metrics（成功口径）
- 单点登录：在 Portal 登录一次，即可访问 Chat/MCP；登出后访问受保护路由会跳转登录。
- 深链接：未登录直达 `/chat/...` 会先登录，完成后回跳目标；刷新不丢登录态。
- SSE：聊天流式稳定（start/chunk/end/error 等事件类型可见）。
- 构建产物：产出单 SPA 门户（含 Chat/MCP 功能路由）；性能不劣化（可按需懒加载）。
- 复用效果：去除模块内重复登录/守卫与 axios 配置，后端抽取共性后行为等价。

五、Scope & Stakeholders（范围与干系人）
- 范围：Chat/MCP 前端与后端、门户前端、反向代理/网关、认证与会话。
- 干系人：终端用户（登录→选择功能→使用）、开发（前/后端）、运维。

六、Constraints & Assumptions（约束与假设）
- 同域优先部署，以便共享 HTTP-only Cookie（SameSite=Lax，Path=/）。
- Portal 负责登录与守卫；Chat/MCP 不再呈现独立登录页。
- 后端抽取 core 时接口保持兼容、行为等价；先抽取后替换。

七、High-level Approach（高层方案概述，仅 What/Why）
- 前端：建立单一门户 SPA（Portal），统一登录 `/login`、首页 `/home`，再挂载 `/chat/**`、`/mcp/**` 路由；在门户层做路由守卫与登录态管理（Pinia）。
- 后端：抽取 `agent-core`（模型选路、Client 管理、工具注册、记忆装配、编排接口）供 Chat/MCP 共享，构建时一并打包。
- 部署：同域优先；SSE 直接与各自后端通信；反代统一 `/api/*`、`/mcp-api/*` 等路径。

八、Acceptance Checklist（验收清单 — 针对规格本身，而非实现）
- [ ] 目标/非目标清晰，且均可被测试用例验证。
- [ ] 成功指标包含登录一次可用、深链接回跳、刷新不丢态、SSE 事件可见性。
- [ ] 边界场景已在计划的测试中覆盖（未登录深链接、SSE 中断、登录态过期）。
- [ ] 安全策略已声明（Cookie 策略、同域优先；如跨域需 CORS+凭证与 Cookie Domain）。
- [ ] 风险与回滚策略在 Plan 中有明确项，能够快速恢复。
- [ ] 相关干系人与约束已列明。

—

关联文档（spec‑kit 三件套）
- Plan：docs/specs/portal-integration/plan.md
- Tasks：docs/specs/portal-integration/tasks.md

十二、回滚预案
- 前端：保留原 chat/mcp 前端目录与构建脚本，必要时恢复原路径部署
- 后端：`agent-core` 替换前的分支随时可回退；替换时逐模块回滚

十三、开放问题
- MCP 前端源码与后端地址（端口、上下文路径）确认
- 部署是否同域？若二级域名，Cookie Domain/CORS 策略由谁配置
- 是否需要统一“事件模型”（AgentEvent）作为前后端标准（建议未来推进）

—

附录：spec-kit 风格 TODO（仅计划，不执行）

- [ ] BE-01 抽取 `agent-core`（接口与默认实现，保留行为等价）
- [ ] BE-02 Chat 后端改依赖 `agent-core`（构建一起打包）
- [ ] BE-03 MCP 后端改依赖 `agent-core`
- [ ] FE-01 初始化 Portal（路由/守卫/Auth store/Axios）
- [ ] FE-02 迁移 Chat 前端到 `features/chat`（修复 import 与路由）
- [ ] FE-03 迁移 MCP 前端到 `features/mcp`（修复 import 与路由）
- [ ] FE-04 Dev 代理与环境变量配置（/api、/mcp-api）
- [ ] INT-01 联调认证流（登录/登出/回跳/刷新）
- [ ] INT-02 联调 Chat SSE
- [ ] INT-03 联调 MCP 请求
- [ ] DOC-01 更新 README/启动脚本/部署指南
