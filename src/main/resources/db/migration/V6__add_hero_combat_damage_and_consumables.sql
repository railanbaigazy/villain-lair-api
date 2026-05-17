alter table attack_battles
    add column if not exists hero_damage_received integer not null default 0 check (hero_damage_received >= 0);

alter table shop_items
    drop constraint if exists shop_items_category_check;
alter table shop_items
    add constraint shop_items_category_check
    check (category in ('WEAPON', 'GUARD', 'TRAP', 'DEFENSE_WEAPON', 'POTION'));

create table if not exists hero_consumable_items (
    id          bigserial primary key,
    hero_profile_id bigint not null references hero_profiles(id) on delete cascade,
    shop_item_id    bigint not null references shop_items(id),
    quantity    integer not null default 1 check (quantity > 0),
    acquired_at timestamptz not null default now()
);

create index if not exists ix_hero_consumable_items_hero_profile_id on hero_consumable_items(hero_profile_id);

insert into shop_items (code, name, role, category, price, power, durability, description, sort_order)
values ('HEALTH_POTION', 'Health Potion', 'HERO', 'POTION', 40, 30, null, 'Restores 30 HP to the hero. Consumed on use.', 40)
on conflict (code) do update
    set name        = excluded.name,
        price       = excluded.price,
        power       = excluded.power,
        description = excluded.description,
        sort_order  = excluded.sort_order;
