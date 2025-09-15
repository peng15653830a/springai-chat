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
create table if not exists users (
                                     id BIGSERIAL primary key,
                                     username VARCHAR(50) unique not null,
                                     nickname VARCHAR(100),
                                     created_at TIMESTAMP default CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP default CURRENT_TIMESTAMP
);
-- 对话会话表（简化，移除模型ID引用）
create table if not exists conversations (
                                             id BIGSERIAL primary key,
                                             user_id BIGINT not null,
                                             title VARCHAR(200),
-- 使用字符串存储提供者和模型名称，而不是外键
                                             provider_name VARCHAR(50),
                                             model_name VARCHAR(200),
                                             created_at TIMESTAMP default CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP default CURRENT_TIMESTAMP
);
-- 消息表（简化，移除模型ID引用）
create table if not exists messages (
                                        id BIGSERIAL primary key,
                                        conversation_id BIGINT not null,
                                        role VARCHAR(20) not null,
                                        content TEXT not null,
                                        thinking TEXT,
                                        thinking_content TEXT,
-- 使用字符串存储提供者和模型名称，而不是外键
                                        provider_name VARCHAR(50),
                                        model_name VARCHAR(200),
                                        created_at TIMESTAMP default CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP default CURRENT_TIMESTAMP
);
-- 消息工具调用结果表
create table if not exists message_tool_results (
                                                    id BIGSERIAL primary key,
                                                    message_id BIGINT not null,
                                                    tool_name VARCHAR(50) not null,
                                                    call_sequence INT not null,
                                                    tool_input TEXT,
                                                    tool_result TEXT,
                                                    status VARCHAR(20) not null default 'IN_PROGRESS',
                                                    error_message TEXT,
                                                    created_at TIMESTAMP default CURRENT_TIMESTAMP,
                                                    updated_at TIMESTAMP default CURRENT_TIMESTAMP
);
-- =================================================================
-- 2. 索引
-- =================================================================
-- 用户相关索引
create index if not exists idx_conversations_user_id on
    conversations (user_id);

create index if not exists idx_messages_conversation_id on
    messages (conversation_id);
-- 消息工具调用结果表索引
create index if not exists idx_message_tool_results_message_id on
    message_tool_results (message_id);

create index if not exists idx_message_tool_results_message_id_tool_name on
    message_tool_results (message_id,
                          tool_name);

create index if not exists idx_message_tool_results_message_id_sequence on
    message_tool_results (message_id,
                          call_sequence);
-- =================================================================
-- 3. 外键约束（简化）
-- =================================================================

do $$
    begin
        -- 基础表外键
        if not exists (
            select
                1
            from
                pg_constraint
            where
                conname = 'fk_conversations_user') then
            alter table conversations add constraint fk_conversations_user
                foreign key (user_id) references users(id) on
                    delete
                    cascade;
        end if;

        if not exists (
            select
                1
            from
                pg_constraint
            where
                conname = 'fk_messages_conversation') then
            alter table messages add constraint fk_messages_conversation
                foreign key (conversation_id) references conversations(id) on
                    delete
                    cascade;
        end if;
-- 消息工具调用结果表外键
        if not exists (
            select
                1
            from
                pg_constraint
            where
                conname = 'fk_message_tool_results_message') then
            alter table message_tool_results add constraint fk_message_tool_results_message
                foreign key (message_id) references messages(id) on
                    delete
                    cascade;
        end if;
    end $$;
-- =================================================================
-- 4. 触发器函数
-- =================================================================

create or replace
    function update_updated_at_column()
    returns trigger as $$
begin
    NEW.updated_at = CURRENT_TIMESTAMP;

    return new;
end;

$$ language 'plpgsql';
-- 触发器
drop trigger if exists update_users_updated_at on
    users;

create trigger update_users_updated_at
    before
        update
    on
        users
    for each row execute function update_updated_at_column();

drop trigger if exists update_conversations_updated_at on
    conversations;

create trigger update_conversations_updated_at
    before
        update
    on
        conversations
    for each row execute function update_updated_at_column();

drop trigger if exists update_messages_updated_at on
    messages;

create trigger update_messages_updated_at
    before
        update
    on
        messages
    for each row execute function update_updated_at_column();

drop trigger if exists update_message_tool_results_updated_at on
    message_tool_results;

create trigger update_message_tool_results_updated_at
    before
        update
    on
        message_tool_results
    for each row execute function update_updated_at_column();
-- =================================================================
-- 5. 初始数据 - 测试用户（仅开发环境）
-- =================================================================
-- 插入测试用户
insert
into
    users (username,
           nickname)
values
    ('admin',
     '管理员'),
    ('test',
     '测试用户'),
    ('demo',
     '演示用户')
on
    conflict (username) do
    update
    set
        nickname = EXCLUDED.nickname,
        updated_at = CURRENT_TIMESTAMP;
-- =================================================================
-- 结束
-- =================================================================
-- 注意: AI模型配置现在完全由 application.yml 和 ModelProvider 类管理
-- 不再需要数据库表存储模型信息，简化了架构并避免了数据不一致问题