INSERT INTO users (id, username, nickname) VALUES (1, 'testuser', '测试用户'), (2, 'admin', '管理员用户') ON CONFLICT (id) DO NOTHING;
