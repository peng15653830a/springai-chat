-- AI聊天应用测试数据

-- 插入测试用户（使用ON CONFLICT DO NOTHING避免重复插入）
INSERT INTO users (id, username, nickname) VALUES 
(1, 'testuser', '测试用户'),
(2, 'admin', '管理员用户')
ON CONFLICT (id) DO NOTHING;