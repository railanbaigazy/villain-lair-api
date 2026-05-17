package kz.railan.villain_lair_api.hero.repository;

import java.util.Optional;
import kz.railan.villain_lair_api.hero.entity.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeaponRepository extends JpaRepository<Weapon, Long> {
    Optional<Weapon> findByName(String name);

    Optional<Weapon> findByCode(String code);
}
