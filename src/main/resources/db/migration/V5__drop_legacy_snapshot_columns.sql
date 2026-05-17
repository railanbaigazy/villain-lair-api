alter table attack_battles
    drop column if exists hero_power_snapshot,
    drop column if exists lair_defense_snapshot;
