create table if not exists game_event_logs (
    id          bigserial primary key,
    event_id    varchar(36)  not null unique,
    topic       varchar(100) not null,
    event_type  varchar(80)  not null,
    aggregate_id varchar(50),
    payload     text         not null,
    consumed_at timestamptz  not null
);

create index if not exists ix_game_event_logs_topic       on game_event_logs(topic);
create index if not exists ix_game_event_logs_event_type  on game_event_logs(event_type);
create index if not exists ix_game_event_logs_consumed_at on game_event_logs(consumed_at desc);

create table if not exists user_stats (
    id            bigserial primary key,
    user_id       bigint      not null unique references users(id) on delete cascade,
    attacks_won   integer     not null default 0,
    attacks_lost  integer     not null default 0,
    defenses_won  integer     not null default 0,
    defenses_lost integer     not null default 0,
    updated_at    timestamptz not null default now()
);
