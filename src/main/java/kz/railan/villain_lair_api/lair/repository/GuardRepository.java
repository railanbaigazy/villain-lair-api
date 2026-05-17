package kz.railan.villain_lair_api.lair.repository;

import java.util.List;
import kz.railan.villain_lair_api.lair.entity.Guard;
import kz.railan.villain_lair_api.lair.entity.Lair;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardRepository extends JpaRepository<Guard, Long> {
    List<Guard> findByLairOrderByIdAsc(Lair lair);

    List<Guard> findByLairAndActiveTrue(Lair lair);
}
