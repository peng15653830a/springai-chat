# 仓库指南

## 项目结构与模块组织
- `backend/`：Spring Boot 3（Java 17），集成 Spring AI、MyBatis、REST/SSE。配置位于 `src/main/resources/application.yml`，测试位于 `src/test/java`。
- `frontend/`：Vue 3 + Vite。页面在 `src/views`，组件在 `src/components`，状态在 `src/stores`，API 在 `src/api`。
- `scripts/`：实用脚本（如 `code-review.sh`）。
- `docs/`：设计与运维文档。

## 构建、测试与本地运行
- 一键本地运行：`./start-cross-platform.sh`（后端 `:8080`，前端 `:3000`），停止：`./stop-cross-platform.sh`。
- 后端
  - `cd backend && mvn clean verify`：构建并执行测试（生成 JaCoCo 报告）。
  - `mvn spring-boot:run`：本地启动 API。
  - `mvn pmd:check`：基于 PMD 的阿里巴巴 P3C 规则检查。
  - `mvn spotless:check|apply`：检查/自动格式化 Java 代码。
  - 覆盖率报告：`backend/target/site/jacoco/index.html`。
- 前端
  - `cd frontend && npm install`：安装依赖。
  - `npm run dev`：本地开发（Vite）。
  - `npm run build` / `npm run preview`：生产构建/预览。

## 代码风格与命名约定
- Java：Spotless 强制 Google Java Format；2 空格缩进；禁止通配符导入；按需使用 Lombok；包名 `com.example.*`。
- Vue/JS：ES Modules 与 Composition API。组件/页面使用 `PascalCase.vue`（如 `SearchResults.vue`）；store 与工具采用小写/小驼峰（如 `stores/chat.js`、`api/index.js`）。
- 命名：类 `PascalCase`，方法/变量 `camelCase`，常量 `UPPER_SNAKE_CASE`。

## 测试规范
- 后端：JUnit/Spring Boot Test；测试放在 `backend/src/test/java`，包结构与源码镜像；文件名以 `*Test.java` 结尾。
- 覆盖率：`mvn test` 运行 JaCoCo；覆盖率阈值放宽，强调关键业务与控制器行为断言。
- 前端：暂未配置测试框架，建议保持逻辑可分离、可测试。

## 提交与 PR 规范
- 建议使用约定式提交：`feat:`、`fix:`、`chore:`、`refactor:`、`test:` 等。
- 提交 PR 前：运行 `scripts/code-review.sh`，确保编译、PMD 与格式检查通过。
- PR 必须包含：变更摘要、关联 Issue、测试步骤/计划，UI 变更附截图。

## 安全与配置提示
- 禁止提交真实密钥。通过环境变量覆盖 `application.yml` 引用的配置（如 `OPENAI_API_KEY`、`TAVILY_API_KEY`、数据库凭据）。优先使用环境覆盖而非直接改默认值。
- 后端默认 PostgreSQL；可用环境变量或 Spring Profiles 调整连接。避免在前端代码中嵌入任何敏感信息。
