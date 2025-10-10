-- =================================================================
-- Novel 模块数据库初始化脚本（PostgreSQL）
-- 说明：与 Chat 模块 schema 对齐，便于独立启动时一键初始化
-- =================================================================

-- 1) 基础表
-- ------------------------------
create table if not exists users (
  id           bigserial primary key,
  username     varchar(50) unique not null,
  nickname     varchar(100),
  created_at   timestamp default CURRENT_TIMESTAMP,
  updated_at   timestamp default CURRENT_TIMESTAMP
);

create table if not exists conversations (
  id           bigserial primary key,
  user_id      bigint not null,
  title        varchar(200),
  created_at   timestamp default CURRENT_TIMESTAMP,
  updated_at   timestamp default CURRENT_TIMESTAMP
);

create table if not exists messages (
  id               bigserial primary key,
  conversation_id  bigint not null,
  role             varchar(20) not null,
  content          text not null,
  thinking         text,
  created_at       timestamp default CURRENT_TIMESTAMP,
  updated_at       timestamp default CURRENT_TIMESTAMP
);

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

-- 2) 索引
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

