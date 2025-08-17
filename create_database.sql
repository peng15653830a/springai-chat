-- 创建AI聊天应用数据库
-- 请在MySQL中执行此脚本以创建数据库

CREATE DATABASE IF NOT EXISTS ai_chat 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用创建的数据库
USE ai_chat;

-- 创建用户（可选，如果需要专门的数据库用户）
-- CREATE USER IF NOT EXISTS 'ai_chat_user'@'localhost' IDENTIFIED BY 'ai_chat_password';
-- GRANT ALL PRIVILEGES ON ai_chat.* TO 'ai_chat_user'@'localhost';
-- FLUSH PRIVILEGES;

SELECT 'Database ai_chat created successfully!' as message;