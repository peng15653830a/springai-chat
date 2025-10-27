# Spring AI 多模块智能助手平台 - 部署运维文档

> **版本**: v3.0  
> **更新时间**: 2024-01-27  
> **状态**: ✅ 生产就绪

---

## 📋 目录

- [1. 环境准备](#1-环境准备)
- [2. 快速开始](#2-快速开始)
- [3. 生产部署](#3-生产部署)
- [4. 配置说明](#4-配置说明)
- [5. 运维管理](#5-运维管理)
- [6. 故障排查](#6-故障排查)

---

## 1. 环境准备

### 1.1 系统要求

| 组件 | 最低要求 | 推荐配置 |
|------|---------|---------|
| **操作系统** | Linux / macOS / Windows | Ubuntu 22.04 LTS |
| **Java** | JDK 17+ | JDK 17 |
| **Maven** | 3.9+ | 3.9.6 |
| **Node.js** | 18+ | 20 LTS |
| **PostgreSQL** | 15+ | 15.x |
| **内存** | 4GB | 8GB+ |
| **磁盘** | 10GB | 50GB+ |

### 1.2 依赖服务

#### PostgreSQL
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql-15 postgresql-contrib

# macOS
brew install postgresql@15

# 启动服务
sudo systemctl start postgresql
# 或
brew services start postgresql@15
```

#### PGVector 扩展（Novel 模块需要）
```bash
# 安装 PGVector
git clone https://github.com/pgvector/pgvector.git
cd pgvector
make
sudo make install

# 在数据库中启用
psql -U postgres -d your_database -c "CREATE EXTENSION vector;"
```

### 1.3 API Key 准备

需要申请以下 API Key：

| 服务 | 环境变量 | 说明 | 申请地址 |
|------|---------|------|---------|
| **OpenAI** | `OPENAI_API_KEY` | GPT 模型 | https://platform.openai.com/api-keys |
| **DeepSeek** | `DEEPSEEK_API_KEY` | DeepSeek Chat | https://platform.deepseek.com |
| **Tavily** | `TAVILY_API_KEY` | 搜索功能 | https://tavily.com |
| **GreatWall** | `GREATWALL_API_KEY` | 长城大模型（可选） | 内部申请 |

---

## 2. 快速开始

### 2.1 克隆项目

```bash
git clone <repository-url>
cd springai-multimodule-platform
```

### 2.2 配置数据库

```bash
# 创建数据库
psql -U postgres -c "CREATE DATABASE ai_chat;"
psql -U postgres -c "CREATE DATABASE ai_novel;"

# 启用 PGVector（Novel 模块）
psql -U postgres -d ai_novel -c "CREATE EXTENSION vector;"
```

### 2.3 配置环境变量

```bash
# 复制环境变量模板
cp env.example .env

# 编辑 .env 文件
export OPENAI_API_KEY=sk-xxx
export DEEPSEEK_API_KEY=sk-xxx
export TAVILY_API_KEY=xxx
export DB_URL=jdbc:postgresql://localhost:5432/ai_chat
export DB_USERNAME=postgres
export DB_PASSWORD=your_password

# 加载环境变量
source .env
```

### 2.4 初始化数据库

```bash
# Chat 模块数据库初始化
psql -U postgres -d ai_chat -f chat/src/main/resources/database/init.sql

# Novel 模块数据库初始化
psql -U postgres -d ai_novel -f novel/src/main/resources/database/init.sql
```

### 2.5 构建项目

```bash
# 编译所有模块
./mvnw clean package -DskipTests

# 或者只编译不打包
./mvnw clean compile
```

### 2.6 启动服务

#### 方式一：一键启动（推荐）
```bash
./start-all.sh
```

该脚本会按顺序启动：
1. Chat Module (8080)
2. Novel Module (8081)
3. MCP Server (8082)
4. Portal Frontend (5173)

#### 方式二：手动启动

**启动 Chat 模块**：
```bash
cd chat
../mvnw spring-boot:run
# 或
java -jar target/ai-chat-0.0.1-SNAPSHOT.jar
```

**启动 Novel 模块**：
```bash
cd novel
../mvnw spring-boot:run
# 或
java -jar target/novel-backend-0.0.1-SNAPSHOT.jar
```

**启动前端**：
```bash
cd apps/portal-frontend
npm install
npm run dev
```

### 2.7 访问应用

打开浏览器访问：
- **前端门户**: http://localhost:5173
- **Chat API**: http://localhost:8080
- **Novel API**: http://localhost:8081
- **MCP Server**: http://localhost:8082

---

## 3. 生产部署

### 3.1 配置文件

创建生产环境配置：

**chat/src/main/resources/application-prod.yml**:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
  
  sql:
    init:
      mode: never  # 生产环境不自动初始化

ai:
  models:
    default-provider: deepseek
    defaults:
      temperature: 0.7
      max-tokens: 4096

logging:
  level:
    root: INFO
    com.example: INFO
  file:
    name: logs/chat-app.log
```

### 3.2 打包部署

```bash
# 1. 打包所有模块
./mvnw clean package -DskipTests -Pprod

# 2. 打包前端
cd apps/portal-frontend
npm run build

# 3. 部署后端 JAR
scp chat/target/ai-chat-0.0.1-SNAPSHOT.jar user@server:/opt/springai/
scp novel/target/novel-backend-0.0.1-SNAPSHOT.jar user@server:/opt/springai/

# 4. 部署前端静态文件
rsync -avz apps/portal-frontend/dist/ user@server:/var/www/springai/
```

### 3.3 使用 systemd 管理服务

**创建 systemd 服务文件**：

`/etc/systemd/system/springai-chat.service`:
```ini
[Unit]
Description=Spring AI Chat Module
After=network.target postgresql.service

[Service]
Type=simple
User=springai
WorkingDirectory=/opt/springai
ExecStart=/usr/bin/java -jar \
  -Xms1g -Xmx2g \
  -Dspring.profiles.active=prod \
  /opt/springai/ai-chat-0.0.1-SNAPSHOT.jar

Environment="OPENAI_API_KEY=sk-xxx"
Environment="DEEPSEEK_API_KEY=sk-xxx"
Environment="DB_URL=jdbc:postgresql://localhost:5432/ai_chat"
Environment="DB_USERNAME=springai"
Environment="DB_PASSWORD=xxx"

Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**启动服务**：
```bash
sudo systemctl daemon-reload
sudo systemctl enable springai-chat
sudo systemctl start springai-chat
sudo systemctl status springai-chat
```

### 3.4 Nginx 反向代理

**配置文件 `/etc/nginx/sites-available/springai`**:
```nginx
upstream chat_backend {
    server 127.0.0.1:8080;
}

upstream novel_backend {
    server 127.0.0.1:8081;
}

server {
    listen 80;
    server_name ai.example.com;

    # 前端静态文件
    location / {
        root /var/www/springai;
        try_files $uri $uri/ /index.html;
    }

    # Chat API
    location /api/chat {
        proxy_pass http://chat_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # SSE 配置
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 3600s;
    }

    # Novel API
    location /api/novel {
        proxy_pass http://novel_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        
        # SSE 配置
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 3600s;
    }
}
```

**启用配置**：
```bash
sudo ln -s /etc/nginx/sites-available/springai /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 3.5 Docker 部署（可选）

**Dockerfile (Chat 模块)**:
```dockerfile
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/ai-chat-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-Xms1g", "-Xmx2g", "-jar", "app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_USER: springai
      POSTGRES_PASSWORD: password
      POSTGRES_DB: ai_chat
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  chat:
    build: ./chat
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/ai_chat
      DB_USERNAME: springai
      DB_PASSWORD: password
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY}
    depends_on:
      - postgres

  novel:
    build: ./novel
    ports:
      - "8081:8081"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/ai_novel
      DB_USERNAME: springai
      DB_PASSWORD: password
    depends_on:
      - postgres

  frontend:
    image: nginx:alpine
    volumes:
      - ./apps/portal-frontend/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/conf.d/default.conf
    ports:
      - "80:80"
    depends_on:
      - chat
      - novel

volumes:
  postgres_data:
```

**启动**：
```bash
docker-compose up -d
```

---

## 4. 配置说明

### 4.1 多模型配置

**application.yml**:
```yaml
ai:
  models:
    enabled: true
    default-provider: deepseek
    default-model: deepseek-chat
    
    defaults:
      temperature: 0.7
      max-tokens: 4096
      timeout-ms: 30000
      stream-enabled: true
    
    providers:
      # OpenAI
      openai:
        enabled: ${OPENAI_ENABLED:true}
        display-name: "OpenAI"
        api-key: ${OPENAI_API_KEY}
        base-url: https://api.openai.com
        models:
          - name: "gpt-4"
            display-name: "GPT-4"
            max-tokens: 8192
            supports-tools: true
            supports-streaming: true
      
      # DeepSeek
      deepseek:
        enabled: ${DEEPSEEK_ENABLED:true}
        display-name: "DeepSeek"
        api-key: ${DEEPSEEK_API_KEY}
        base-url: https://api.deepseek.com
        models:
          - name: "deepseek-chat"
            display-name: "DeepSeek Chat"
            max-tokens: 4096
            temperature: 0.7
            supports-tools: true
            supports-streaming: true
      
      # 本地 Ollama
      ollama:
        enabled: ${OLLAMA_ENABLED:false}
        display-name: "Ollama"
        base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
        models:
          - name: "llama2"
            display-name: "Llama 2"
            max-tokens: 4096
```

### 4.2 搜索配置

```yaml
chat:
  search:
    provider: tavily
    api-key: ${TAVILY_API_KEY}
    max-results: 5
    max-tool-calls: 3
    enabled: true
```

### 4.3 RAG 配置（Novel 模块）

```yaml
novel:
  rag:
    enabled: true
    embedding-model: text-embedding-ada-002
    vector-store: pgvector
    top-k: 5
    similarity-threshold: 0.7
    
  material:
    max-file-size: 10485760  # 10MB
    allowed-types:
      - pdf
      - txt
      - markdown
      - docx
```

### 4.4 日志配置

```yaml
logging:
  level:
    root: INFO
    com.example: DEBUG
    org.springframework.ai: DEBUG
  
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  
  file:
    name: logs/application.log
    max-size: 100MB
    max-history: 30
```

---

## 5. 运维管理

### 5.1 服务管理脚本

项目提供了便捷的管理脚本：

```bash
# 启动所有服务
./start-all.sh

# 停止所有服务
./stop-all.sh

# 重启所有服务
./restart-all.sh

# 查看服务状态
./status.sh
```

### 5.2 日志管理

**查看实时日志**：
```bash
# Chat 模块
tail -f chat/logs/application.log

# Novel 模块
tail -f novel/logs/application.log

# 或使用脚本
./logs.sh chat
./logs.sh novel
```

**日志轮转配置** (`/etc/logrotate.d/springai`):
```
/opt/springai/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 springai springai
    sharedscripts
    postrotate
        systemctl reload springai-chat
        systemctl reload springai-novel
    endscript
}
```

### 5.3 数据库备份

**每日备份脚本** (`/opt/scripts/backup-db.sh`):
```bash
#!/bin/bash
BACKUP_DIR=/opt/backups
DATE=$(date +%Y%m%d_%H%M%S)

# 备份 Chat 数据库
pg_dump -U springai ai_chat > $BACKUP_DIR/ai_chat_$DATE.sql

# 备份 Novel 数据库
pg_dump -U springai ai_novel > $BACKUP_DIR/ai_novel_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/ai_chat_$DATE.sql
gzip $BACKUP_DIR/ai_novel_$DATE.sql

# 删除 30 天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

echo "Backup completed: $DATE"
```

**配置 cron 任务**：
```bash
crontab -e

# 每天凌晨 2 点备份
0 2 * * * /opt/scripts/backup-db.sh >> /var/log/backup.log 2>&1
```

### 5.4 监控告警

**健康检查端点**：
```bash
# Chat 模块
curl http://localhost:8080/actuator/health

# Novel 模块
curl http://localhost:8081/actuator/health
```

**Prometheus 监控配置** (`prometheus.yml`):
```yaml
scrape_configs:
  - job_name: 'springai-chat'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
  
  - job_name: 'springai-novel'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8081']
```

---

## 6. 故障排查

### 6.1 常见问题

#### 问题 1: 服务启动失败

**症状**：
```
Error: Failed to start Spring Boot application
```

**排查步骤**：
1. 检查日志：`tail -f logs/application.log`
2. 检查端口占用：`lsof -i :8080`
3. 检查数据库连接：`psql -U postgres -d ai_chat`
4. 检查环境变量：`echo $OPENAI_API_KEY`

**解决方案**：
```bash
# 停止占用端口的进程
kill -9 $(lsof -t -i:8080)

# 或更换端口
export SERVER_PORT=8090
```

#### 问题 2: SSE 连接断开

**症状**：
前端显示 "连接错误"，SSE 流式输出中断

**排查步骤**：
1. 检查 Nginx 配置：`proxy_buffering off`
2. 检查超时配置：`proxy_read_timeout`
3. 检查网络连接：`curl -N http://localhost:8080/api/chat/stream`

**解决方案**：
```nginx
# Nginx 配置
location /api/chat {
    proxy_buffering off;
    proxy_cache off;
    proxy_read_timeout 3600s;
    proxy_connect_timeout 60s;
}
```

#### 问题 3: 数据库连接池耗尽

**症状**：
```
HikariPool - Connection is not available, request timed out after 30000ms
```

**排查步骤**：
1. 检查活动连接：`SELECT count(*) FROM pg_stat_activity;`
2. 检查慢查询：`SELECT * FROM pg_stat_statements ORDER BY mean_time DESC;`

**解决方案**：
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50  # 增加连接池大小
      leak-detection-threshold: 60000  # 启用泄漏检测
```

#### 问题 4: 向量检索慢

**症状**：
Novel 模块 RAG 检索超时

**排查步骤**：
1. 检查索引：`\d+ novel_material_vectors`
2. 检查数据量：`SELECT count(*) FROM novel_material_vectors;`

**解决方案**：
```sql
-- 创建 IVFFlat 索引
CREATE INDEX idx_material_vectors_embedding 
ON novel_material_vectors 
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- 增加探测列表
SET ivfflat.probes = 10;
```

### 6.2 性能优化

#### JVM 调优
```bash
java -jar \
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/opt/springai/logs/heapdump.hprof \
  app.jar
```

#### 数据库优化
```sql
-- 分析查询计划
EXPLAIN ANALYZE 
SELECT * FROM messages 
WHERE conversation_id = 123 
ORDER BY created_at;

-- 更新统计信息
ANALYZE messages;

-- 清理死元组
VACUUM FULL messages;
```

### 6.3 紧急恢复

**从备份恢复数据库**：
```bash
# 停止服务
sudo systemctl stop springai-chat

# 恢复数据库
gunzip < /opt/backups/ai_chat_20240127.sql.gz | psql -U springai ai_chat

# 启动服务
sudo systemctl start springai-chat
```

---

## 7. 安全加固

### 7.1 防火墙配置

```bash
# 只允许 HTTP/HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# 禁止直接访问后端端口
sudo ufw deny 8080/tcp
sudo ufw deny 8081/tcp

# 启用防火墙
sudo ufw enable
```

### 7.2 SSL/TLS 配置

```bash
# 使用 Let's Encrypt
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d ai.example.com
```

### 7.3 API Key 轮换

```bash
# 1. 更新环境变量
export OPENAI_API_KEY=sk-new-key

# 2. 重启服务
sudo systemctl restart springai-chat
```

---

**文档维护者**: 运维团队  
**最后更新**: 2024-01-27  
**状态**: ✅ 当前版本
