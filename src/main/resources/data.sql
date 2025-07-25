-- AI聊天应用测试数据

-- 插入测试用户
INSERT INTO users (username, nickname) VALUES 
('testuser', '测试用户'),
('admin', '管理员用户');

-- 插入测试对话
INSERT INTO conversations (user_id, title) VALUES 
(1, '第一次对话'),
(1, '关于Spring AI的讨论'),
(2, '系统管理对话');

-- 插入测试消息
INSERT INTO messages (conversation_id, role, content) VALUES 
(1, 'user', '你好，我想了解一下AI聊天应用'),
(1, 'assistant', '你好！我是AI助手，很高兴为您介绍AI聊天应用。这个应用具有智能对话和联网搜索功能，可以帮助您获取最新信息并进行有意义的对话。'),
(2, 'user', 'Spring AI框架有哪些特点？'),
(2, 'assistant', 'Spring AI是Spring生态系统中用于构建AI应用的框架，主要特点包括：1. 与Spring Boot无缝集成 2. 支持多种AI模型 3. 提供统一的API接口 4. 支持流式响应等功能。'),
(3, 'user', '系统运行状态如何？'),
(3, 'assistant', '系统运行良好，所有核心功能正常工作，数据库连接正常，AI服务响应及时。');