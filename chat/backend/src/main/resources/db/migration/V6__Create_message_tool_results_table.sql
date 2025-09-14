-- 创建消息工具调用结果表
CREATE TABLE IF NOT EXISTS message_tool_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    tool_name VARCHAR(50) NOT NULL,
    call_sequence INT NOT NULL,
    tool_input TEXT,
    tool_result LONGTEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_message_tool_results_message_id ON message_tool_results (message_id);
CREATE INDEX IF NOT EXISTS idx_message_tool_results_message_id_tool_name ON message_tool_results (message_id, tool_name);
CREATE INDEX IF NOT EXISTS idx_message_tool_results_message_id_sequence ON message_tool_results (message_id, call_sequence);