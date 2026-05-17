package kz.railan.villain_lair_api.lair.repository;

import java.util.List;
import kz.railan.villain_lair_api.lair.entity.DefenseWeapon;
import kz.railan.villain_lair_api.lair.entity.Lair;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefenseWeaponRepository extends JpaRepository<DefenseWeapon, Long> {
    List<DefenseWeapon> findByLairOrderByIdAsc(Lair lair);

    List<DefenseWeapon> findByLairAndActiveTrue(Lair lair);
}
