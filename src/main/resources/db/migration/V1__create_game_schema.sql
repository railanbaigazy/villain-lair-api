create table users (
    id bigserial primary key,
    username varchar(50) not null unique,
    email varchar(120) not null unique,
    password_hash varchar(255) not null,
    role varchar(20) not null check (role in ('HERO', 'VILLAIN')),
    coins integer not null default 0 check (coins >= 0),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table hero_profiles (
    id bigserial primary key,
    user_id bigint not null unique references users(id) on delete cascade,
    base_attack integer not null check (base_attack >= 0),
    health integer not null check (health >= 0),
    created_at timestamptz not null default now()
);

create table villain_profiles (
    id bigserial primary key,
    user_id bigint not null unique references users(id) on delete cascade,
    reputation integer not null default 0,
    created_at timestamptz not null default now()
);

create table lairs (
    id bigserial primary key,
    owner_user_id bigint not null references users(id) on delete cascade,
    name varchar(100) not null,
    level integer not null check (level > 0),
    health integer not null check (health >= 0),
    security_level integer not null check (security_level >= 0),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table weapons (
    id bigserial primary key,
    name varchar(80) not null unique,
    attack_bonus integer not null check (attack_bonus >= 0),
    durability integer not null check (durability >= 0),
    price integer not null check (price >= 0)
);

create table hero_inventory_items (
    id bigserial primary key,
    hero_profile_id bigint not null references hero_profiles(id) on delete cascade,
    weapon_id bigint not null references weapons(id),
    quantity integer not null check (quantity > 0),
    equipped boolean not null default false
);

create unique index ux_hero_inventory_one_equipped
    on hero_inventory_items(hero_profile_id)
    where equipped = true;

create table guards (
    id bigserial primary key,
    lair_id bigint not null references lairs(id) on delete cascade,
    name varchar(80) not null,
    power integer not null check (power >= 0),
    active boolean not null default true
);

create table traps (
    id bigserial primary key,
    lair_id bigint not null references lairs(id) on delete cascade,
    name varchar(80) not null,
    power integer not null check (power >= 0),
    durability integer not null check (durability >= 0),
    active boolean not null default true
);

create table attack_battles (
    id bigserial primary key,
    hero_user_id bigint not null references users(id) on delete cascade,
    target_lair_id bigint not null references lairs(id) on delete cascade,
    result varchar(20) not null check (result in ('WIN', 'LOSE')),
    hero_power_snapshot integer not null check (hero_power_snapshot >= 0),
    lair_defense_snapshot integer not null check (lair_defense_snapshot >= 0),
    coins_rewarded_to_hero integer not null default 0 check (coins_rewarded_to_hero >= 0),
    coins_rewarded_to_villain integer not null default 0 check (coins_rewarded_to_villain >= 0),
    created_at timestamptz not null default now()
);

create index ix_lairs_owner_user_id on lairs(owner_user_id);
create index ix_guards_lair_id on guards(lair_id);
create index ix_traps_lair_id on traps(lair_id);
create index ix_attack_battles_hero_user_id on attack_battles(hero_user_id);
create index ix_attack_battles_target_lair_id on attack_battles(target_lair_id);
