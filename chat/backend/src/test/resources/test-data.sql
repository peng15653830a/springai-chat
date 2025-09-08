-- 测试数据初始化脚本
-- 使用 INSERT IGNORE 或 ON DUPLICATE KEY 避免重复插入

-- 插入测试用户数据，如果已存在则忽略
INSERT INTO users (username, nickname, created_at) 
SELECT 'testuser', '测试用户', NOW() 
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser');

INSERT INTO users (username, nickname, created_at) 
SELECT 'admin', '管理员用户', NOW() 
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');