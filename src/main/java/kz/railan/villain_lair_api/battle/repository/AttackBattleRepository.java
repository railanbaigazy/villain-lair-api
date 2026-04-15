package kz.railan.villain_lair_api.battle.repository;

import kz.railan.villain_lair_api.battle.entity.AttackBattle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttackBattleRepository extends JpaRepository<AttackBattle, Long> {
}
