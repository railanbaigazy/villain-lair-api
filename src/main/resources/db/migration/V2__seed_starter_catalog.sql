insert into weapons (name, attack_bonus, durability, price)
values
    ('Rusty Blaster', 12, 100, 0),
    ('Steel Sword', 25, 150, 75)
on conflict (name) do nothing;
