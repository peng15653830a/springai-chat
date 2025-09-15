# 贡献指南

感谢关注本项目！为了保持代码质量与协作效率，请在贡献前阅读并遵循本指南。更详细的项目约定请参考 AGENTS.md。

## 快速开始
- Fork 仓库并创建特性分支：`feat/xxx`、`fix/xxx`、`chore/xxx`。
- 本地运行：
  - 一键启动：`./start-cross-platform.sh`（前端 3000，后端 8080）。
  - 或分开启动：
    - 后端：`cd backend && mvn spring-boot:run`
    - 前端：`cd frontend && npm install && npm run dev`

## 开发规范
- 代码风格：Java 使用 Spotless（Google Java Format），命令：`mvn spotless:apply`；静态检查：`mvn pmd:check`（阿里巴巴 P3C）。
- 提交信息：建议采用约定式提交，例如：
  - `feat: 新增会话搜索接口`
  - `fix: 修复 ChatController SSE 超时`
  - `refactor: 抽取 SearchService 接口`
  - `test: 补充 GlobalExceptionHandler 测试`
- 提交前自检：`scripts/code-review.sh`（编译、PMD、格式检查）。

## 测试
- 后端：`mvn clean verify` 或 `mvn test`（JaCoCo 覆盖率报告：`backend/target/site/jacoco/index.html`）。
- 前端：尚未集成测试框架，建议将复杂逻辑下沉为函数以便后续补测。

## Pull Request 流程
- 合并前请：
  - 与 `main` 同步并解决冲突；
  - 保证本地构建通过、PMD 无阻塞问题、代码已格式化；
  - 未提交任何密钥或私密配置。
- PR 必须包含：变更摘要、关联 Issue、测试步骤/结果，UI 变更请附截图；如涉及配置或接口，更新相应文档。

## 安全与配置
- 禁止提交真实密钥。使用环境变量覆盖 `backend/src/main/resources/application.yml` 中的占位：如 `OPENAI_API_KEY`、`TAVILY_API_KEY`、数据库凭据等。
- 避免在前端代码中硬编码敏感信息。

## 参考
- 代码与规范总览：`AGENTS.md`
