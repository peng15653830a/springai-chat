-- =================================================================
-- Spring AI Chat 项目 数据库初始化脚本（PostgreSQL）
-- 场景：全新初始化（无历史负担）；不使用外键/触发器；应用层维护一致性与时间戳
-- =================================================================

-- 1) 基础表
-- ------------------------------
-- 用户
create table if not exists users (
  id           bigserial primary key,
  username     varchar(50) unique not null,
  nickname     varchar(100),
  created_at   timestamp default CURRENT_TIMESTAMP,
  updated_at   timestamp default CURRENT_TIMESTAMP
);

-- 会话
create table if not exists conversations (
  id           bigserial primary key,
  user_id      bigint not null,
  title        varchar(200),
  created_at   timestamp default CURRENT_TIMESTAMP,
  updated_at   timestamp default CURRENT_TIMESTAMP
);

-- 消息
create table if not exists messages (
  id               bigserial primary key,
  conversation_id  bigint not null,
  role             varchar(20) not null,
  content          text not null,
  thinking         text,
  created_at       timestamp default CURRENT_TIMESTAMP,
  updated_at       timestamp default CURRENT_TIMESTAMP
);

-- 消息的工具调用结果
create table if not exists message_tool_results (
  id            bigserial primary key,
  message_id    bigint not null,
  tool_name     varchar(50) not null,
  call_sequence int not null,
  tool_input    text,
  tool_result   text,
  status        varchar(20) not null default 'IN_PROGRESS',
  error_message text,
  created_at    timestamp default CURRENT_TIMESTAMP,
  updated_at    timestamp default CURRENT_TIMESTAMP
);

-- 2) 索引（为常用查询加速）
-- ------------------------------
create index if not exists idx_conversations_user_id on conversations (user_id);
create index if not exists idx_messages_conversation_id on messages (conversation_id);
create index if not exists idx_message_tool_results_message_id on message_tool_results (message_id);
create index if not exists idx_message_tool_results_message_id_tool_name on message_tool_results (message_id, tool_name);
create index if not exists idx_message_tool_results_message_id_sequence on message_tool_results (message_id, call_sequence);

-- 3) 初始数据（仅开发）
-- ------------------------------
insert into users (username, nickname)
values
  ('admin', '管理员'),
  ('test',  '测试用户'),
  ('demo',  '演示用户')
on conflict (username) do update set
  nickname   = EXCLUDED.nickname,
  updated_at = CURRENT_TIMESTAMP;

-- 提示：
-- - 无外键/触发器，删除级联与 updated_at 请在应用层维护（本项目已实现）。
-- - 如需重置全部数据，可在执行前手动清表（truncate）或加 drop 语句，但默认不包含破坏性操作。

