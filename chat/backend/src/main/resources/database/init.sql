-- =================================================================
-- Spring AI 项目数据库初始化脚本（清理版）
-- 作者: xupeng
-- 创建日期: 2024-12-01
-- 修改日期: 2025-09-07
-- 说明: 移除AI模型数据库依赖，简化为配置文件驱动
-- =================================================================

-- =================================================================
-- 1. 基础表结构（保留用户和对话功能）
-- =================================================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    nickname VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 对话会话表（简化，移除模型ID引用）
CREATE TABLE IF NOT EXISTS conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200),
    -- 使用字符串存储提供者和模型名称，而不是外键
    provider_name VARCHAR(50),
    model_name VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 消息表（简化，移除模型ID引用）
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    thinking TEXT,
    thinking_content TEXT,
    -- 使用字符串存储提供者和模型名称，而不是外键
    provider_name VARCHAR(50),
    model_name VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 消息工具调用结果表
CREATE TABLE IF NOT EXISTS message_tool_results (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    tool_name VARCHAR(50) NOT NULL,
    call_sequence INT NOT NULL,
    tool_input TEXT,
    tool_result TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =================================================================
-- 2. 索引
-- =================================================================

-- 用户相关索引
CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations (user_id);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages (conversation_id);

-- 新增索引
CREATE INDEX IF NOT EXISTS idx_conversations_provider_model ON conversations (provider_name, model_name);
CREATE INDEX IF NOT EXISTS idx_messages_provider_model ON messages (provider_name, model_name);

-- 消息工具调用结果表索引
CREATE INDEX IF NOT EXISTS idx_message_tool_results_message_id ON message_tool_results (message_id);
CREATE INDEX IF NOT EXISTS idx_message_tool_results_message_id_tool_name ON message_tool_results (message_id, tool_name);
CREATE INDEX IF NOT EXISTS idx_message_tool_results_message_id_sequence ON message_tool_results (message_id, call_sequence);

-- =================================================================
-- 3. 外键约束（简化）
-- =================================================================

DO $$
BEGIN
    -- 基础表外键
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_conversations_user') THEN
        ALTER TABLE conversations ADD CONSTRAINT fk_conversations_user
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_messages_conversation') THEN
        ALTER TABLE messages ADD CONSTRAINT fk_messages_conversation
            FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE;
    END IF;

    -- 消息工具调用结果表外键
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_message_tool_results_message') THEN
        ALTER TABLE message_tool_results ADD CONSTRAINT fk_message_tool_results_message
            FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE;
    END IF;
END $$;

-- =================================================================
-- 4. 触发器函数
-- =================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 触发器
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_conversations_updated_at ON conversations;
CREATE TRIGGER update_conversations_updated_at 
    BEFORE UPDATE ON conversations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_messages_updated_at ON messages;
CREATE TRIGGER update_messages_updated_at
    BEFORE UPDATE ON messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_message_tool_results_updated_at ON message_tool_results;
CREATE TRIGGER update_message_tool_results_updated_at
    BEFORE UPDATE ON message_tool_results
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =================================================================
-- 5. 初始数据 - 测试用户（仅开发环境）
-- =================================================================

-- 插入测试用户
INSERT INTO users (username, nickname) VALUES
('admin', '管理员'),
('test', '测试用户'),
('demo', '演示用户')
ON CONFLICT (username) DO UPDATE SET
    nickname = EXCLUDED.nickname,
    updated_at = CURRENT_TIMESTAMP;

-- =================================================================
-- 结束
-- =================================================================

-- 注意: AI模型配置现在完全由 application.yml 和 ModelProvider 类管理
-- 不再需要数据库表存储模型信息，简化了架构并避免了数据不一致问题