# Git 仓库清理说明

## 问题描述

在代码审查中发现前端编译产物（`apps/portal-frontend/dist/`）被提交到了版本控制系统。这违反了最佳实践。

## 为什么编译产物不应该提交到 Git？

### 1. **仓库体积膨胀**
- 编译产物通常包含压缩后的 JS/CSS 文件
- 每次编译都会生成新的文件，导致 Git 历史记录快速增长
- 对于大型项目，可能导致仓库大小从 MB 级别增长到 GB 级别

### 2. **合并冲突频繁**
- 不同开发者的编译产物可能不同（即使源代码相同）
- 编译工具版本、操作系统、时间戳等都会影响产物
- 导致频繁的 merge conflict

### 3. **降低代码审查效率**
- PR/MR 中包含大量编译后的代码
- 难以识别真正的代码变更
- 审查者需要浏览大量无意义的编译产物

### 4. **安全风险**
- 源代码和编译产物可能不一致
- 难以验证部署的代码是否由当前源代码编译而来
- 可能包含敏感信息（source maps、环境变量泄露）

### 5. **违反单一来源原则**
- 编译产物应该从源代码生成，而不是独立维护
- 提交编译产物意味着维护两份"真相"

## 已执行的清理操作

### 1. 删除已提交的编译产物

```bash
# 从 Git 索引中删除（但保留本地文件）
git rm -r --cached apps/portal-frontend/dist/
```

已删除的文件：
- `apps/portal-frontend/dist/assets/*.css`
- `apps/portal-frontend/dist/assets/*.js`
- `apps/portal-frontend/dist/index.html`

### 2. 更新 .gitignore

**根目录 `.gitignore`** 添加：
```gitignore
# Portal frontend build output
apps/portal-frontend/dist/
apps/portal-frontend/node_modules/
apps/**/dist/
apps/**/node_modules/
```

**前端目录 `apps/portal-frontend/.gitignore`** 新增：
- 标准的前端项目忽略规则
- 包括 `dist/`, `node_modules/`, 环境变量文件等

### 3. 验证清理效果

```bash
# 检查是否还有 dist 文件被追踪
git ls-files | grep "dist"

# 应该只返回 .gitignore 相关的配置，没有实际的 dist 文件
```

## 正确的前端部署流程

### 开发环境
```bash
cd apps/portal-frontend
npm install
npm run dev  # 开发服务器，不生成 dist
```

### 生产环境
```bash
# 在 CI/CD 中执行
cd apps/portal-frontend
npm ci  # 使用 package-lock.json 安装依赖
npm run build  # 生成 dist/
# 部署 dist/ 到静态服务器或 CDN
```

### CI/CD 配置示例

**GitHub Actions:**
```yaml
name: Build and Deploy

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          
      - name: Install dependencies
        working-directory: apps/portal-frontend
        run: npm ci
        
      - name: Build
        working-directory: apps/portal-frontend
        run: npm run build
        
      - name: Deploy to production
        # 部署 dist/ 到你的服务器
        run: |
          rsync -avz apps/portal-frontend/dist/ user@server:/var/www/html/
```

## 其他最佳实践

### 1. 使用 .gitattributes

为了确保跨平台一致性，建议添加 `.gitattributes`：

```gitattributes
# Auto detect text files and perform LF normalization
* text=auto

# Java files
*.java text eol=lf
*.xml text eol=lf
*.properties text eol=lf
*.yml text eol=lf

# Frontend files
*.js text eol=lf
*.ts text eol=lf
*.json text eol=lf
*.vue text eol=lf
*.css text eol=lf
*.md text eol=lf

# Binary files
*.jar binary
*.png binary
*.jpg binary
*.jpeg binary
*.gif binary
*.ico binary
*.woff binary
*.woff2 binary
*.ttf binary
*.eot binary
```

### 2. 定期审查 Git 仓库大小

```bash
# 查看仓库大小
du -sh .git

# 查看最大的文件
git rev-list --objects --all | \
  git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' | \
  awk '/^blob/ {print substr($0,6)}' | \
  sort -n -k 2 | \
  tail -20
```

### 3. 使用 Git LFS（如有大文件需要）

如果确实需要版本控制一些二进制文件（如设计稿、文档等）：

```bash
git lfs install
git lfs track "*.psd"
git lfs track "*.pdf"
git add .gitattributes
```

## 检查清单

- [x] 删除已提交的编译产物
- [x] 更新 .gitignore 忽略编译产物
- [x] 创建前端专用 .gitignore
- [ ] 配置 CI/CD 自动构建
- [ ] 团队培训：不要提交编译产物
- [ ] 代码审查时检查是否有编译产物被提交

## 参考资源

- [GitHub's .gitignore templates](https://github.com/github/gitignore)
- [Git Best Practices](https://www.git-tower.com/learn/git/ebook/en/command-line/appendix/best-practices)
- [Why you shouldn't commit node_modules](https://www.gitignore.io/docs/faq)

---

**清理日期**: 2024-01-27  
**影响范围**: 前端编译产物  
**风险等级**: 低（只删除可重新生成的文件）
