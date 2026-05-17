package kz.railan.villain_lair_api.battle.repository;

import java.util.List;
import kz.railan.villain_lair_api.battle.entity.AttackBattle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttackBattleRepository extends JpaRepository<AttackBattle, Long> {
    List<AttackBattle> findByHeroUserEmailOrderByCreatedAtDescIdDesc(String email);

    List<AttackBattle> findByVillainUserEmailOrderByCreatedAtDescIdDesc(String email);
}
