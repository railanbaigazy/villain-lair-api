alter table lairs
    add column if not exists max_health integer not null default 100 check (max_health > 0),
    add column if not exists status varchar(20) not null default 'ACTIVE' check (status in ('ACTIVE', 'BREACHED'));

alter table weapons
    add column if not exists code varchar(50);

update weapons
set code = case
    when name = 'Rusty Blaster' then 'LEGACY_RUSTY_BLASTER'
    when name = 'Steel Sword' then 'LEGACY_STEEL_SWORD'
    else upper(replace(name, ' ', '_'))
end
where code is null;

alter table weapons
    alter column code set not null;

alter table weapons
    add constraint ux_weapons_code unique (code);

alter table hero_inventory_items
    add column if not exists current_durability integer not null default 0 check (current_durability >= 0);

update hero_inventory_items hii
set current_durability = w.durability
from weapons w
where hii.weapon_id = w.id
  and hii.current_durability = 0;

create table if not exists defense_weapons (
    id bigserial primary key,
    lair_id bigint not null references lairs(id) on delete cascade,
    name varchar(80) not null,
    power integer not null check (power >= 0),
    durability integer not null check (durability >= 0),
    active boolean not null default true
);

create table if not exists transactions (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    amount integer not null,
    type varchar(30) not null check (type in (
        'STARTER_BONUS',
        'ITEM_PURCHASE',
        'ATTACK_WIN_REWARD',
        'DEFENSE_WIN_REWARD',
        'LAIR_UPGRADE',
        'DAILY_REWARD'
    )),
    description varchar(255) not null,
    created_at timestamptz not null default now()
);

create table if not exists shop_items (
    id bigserial primary key,
    code varchar(50) not null unique,
    name varchar(80) not null,
    role varchar(20) not null check (role in ('HERO', 'VILLAIN')),
    category varchar(30) not null check (category in ('WEAPON', 'GUARD', 'TRAP', 'DEFENSE_WEAPON')),
    price integer not null check (price >= 0),
    power integer not null check (power >= 0),
    durability integer check (durability >= 0),
    description varchar(255) not null,
    active boolean not null default true,
    sort_order integer not null default 0
);

alter table attack_battles
    add column if not exists villain_user_id bigint references users(id) on delete cascade,
    add column if not exists base_hero_power integer not null default 0 check (base_hero_power >= 0),
    add column if not exists final_hero_power integer not null default 0 check (final_hero_power >= 0),
    add column if not exists base_lair_defense integer not null default 0 check (base_lair_defense >= 0),
    add column if not exists final_lair_defense integer not null default 0 check (final_lair_defense >= 0),
    add column if not exists damage_dealt integer not null default 0 check (damage_dealt >= 0),
    add column if not exists battle_log_summary varchar(500) not null default '';

update attack_battles
set villain_user_id = l.owner_user_id
from lairs l
where attack_battles.target_lair_id = l.id
  and attack_battles.villain_user_id is null;

alter table attack_battles
    alter column villain_user_id set not null;

update attack_battles
set base_hero_power = hero_power_snapshot,
    final_hero_power = hero_power_snapshot,
    base_lair_defense = lair_defense_snapshot,
    final_lair_defense = lair_defense_snapshot,
    damage_dealt = case when result = 'WIN' then 10 else 0 end,
    battle_log_summary = case
        when result = 'WIN' then 'Legacy battle: hero won.'
        else 'Legacy battle: villain defended successfully.'
    end;

alter table attack_battles
    drop constraint if exists attack_battles_result_check;

update attack_battles
set result = case
    when result = 'WIN' then 'HERO_WIN'
    when result = 'LOSE' then 'VILLAIN_WIN'
    else result
end;

alter table attack_battles
    add constraint attack_battles_result_check
    check (result in ('HERO_WIN', 'VILLAIN_WIN'));

create index if not exists ix_lairs_status on lairs(status);
create index if not exists ix_transactions_user_id on transactions(user_id);
create index if not exists ix_shop_items_role on shop_items(role);
create index if not exists ix_defense_weapons_lair_id on defense_weapons(lair_id);
create index if not exists ix_attack_battles_villain_user_id on attack_battles(villain_user_id);

insert into weapons (code, name, attack_bonus, durability, price)
values
    ('BASIC_SWORD', 'Basic Sword', 10, 25, 0),
    ('IRON_SWORD', 'Iron Sword', 18, 40, 60),
    ('PLASMA_BLADE', 'Plasma Blade', 30, 55, 140)
on conflict (code) do update
set name = excluded.name,
    attack_bonus = excluded.attack_bonus,
    durability = excluded.durability,
    price = excluded.price;

insert into shop_items (code, name, role, category, price, power, durability, description, sort_order)
values
    ('BASIC_SWORD', 'Basic Sword', 'HERO', 'WEAPON', 0, 10, 25, 'Starter blade for heroes.', 10),
    ('IRON_SWORD', 'Iron Sword', 'HERO', 'WEAPON', 60, 18, 40, 'Reliable forged sword with better damage.', 20),
    ('PLASMA_BLADE', 'Plasma Blade', 'HERO', 'WEAPON', 140, 30, 55, 'High-end weapon with strong attack bonus.', 30),
    ('GUARD', 'Guard', 'VILLAIN', 'GUARD', 50, 8, null, 'Adds a trained guard to the lair.', 10),
    ('TRAP', 'Trap', 'VILLAIN', 'TRAP', 45, 7, 4, 'Adds a reusable trap to the lair.', 20),
    ('DEFENSE_TURRET', 'Defense Turret', 'VILLAIN', 'DEFENSE_WEAPON', 90, 14, 6, 'Automated defense weapon for lairs.', 30)
on conflict (code) do update
set name = excluded.name,
    role = excluded.role,
    category = excluded.category,
    price = excluded.price,
    power = excluded.power,
    durability = excluded.durability,
    description = excluded.description,
    sort_order = excluded.sort_order;
