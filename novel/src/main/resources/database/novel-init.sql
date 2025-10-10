-- Novel 模块专属表结构

create table if not exists novel_sessions (
  id           bigserial primary key,
  title        varchar(200),
  model        varchar(100),
  temperature  double precision,
  max_tokens   integer,
  top_p        double precision,
  created_at   timestamp default CURRENT_TIMESTAMP,
  updated_at   timestamp default CURRENT_TIMESTAMP
);

create table if not exists novel_messages (
  id           bigserial primary key,
  session_id   bigint not null,
  role         varchar(20) not null,
  content      text not null,
  created_at   timestamp default CURRENT_TIMESTAMP
);

create index if not exists idx_novel_messages_session on novel_messages (session_id);

-- RAG 引用
create table if not exists novel_references (
  id           bigserial primary key,
  session_id   bigint,
  message_id   bigint,
  source       text,
  title        text,
  excerpt      text,
  similarity   double precision,
  url          text,
  created_at   timestamp default CURRENT_TIMESTAMP
);

create index if not exists idx_novel_references_session on novel_references (session_id);
create index if not exists idx_novel_references_message on novel_references (message_id);

-- MCP 工具调用
create table if not exists novel_tool_calls (
  id           bigserial primary key,
  session_id   bigint,
  message_id   bigint,
  tool_name    varchar(100) not null,
  input_json   text,
  result_json  text,
  status       varchar(20) not null default 'IN_PROGRESS',
  error_message text,
  created_at   timestamp default CURRENT_TIMESTAMP,
  updated_at   timestamp default CURRENT_TIMESTAMP
);

create index if not exists idx_novel_tool_calls_session on novel_tool_calls (session_id);
create index if not exists idx_novel_tool_calls_message on novel_tool_calls (message_id);
