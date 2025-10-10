-- 测试数据初始化脚本

-- 插入测试用户
INSERT INTO users (id, username, email) VALUES 
(1, 'testuser1', 'test1@example.com'),
(2, 'testuser2', 'test2@example.com');

-- 插入测试产品
INSERT INTO products (id, name, price, stock) VALUES 
(1, '苹果', 5.00, 100),
(2, '香蕉', 3.00, 50),
(3, '橙子', 4.00, 30),
(4, '牛奶', 6.00, 20),
(5, '面包', 8.00, 15);