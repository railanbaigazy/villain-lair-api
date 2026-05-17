package kz.railan.villain_lair_api.lair.repository;

import java.util.List;
import kz.railan.villain_lair_api.lair.entity.Lair;
import kz.railan.villain_lair_api.lair.entity.Trap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrapRepository extends JpaRepository<Trap, Long> {
    List<Trap> findByLairOrderByIdAsc(Lair lair);

    List<Trap> findByLairAndActiveTrue(Lair lair);
}
