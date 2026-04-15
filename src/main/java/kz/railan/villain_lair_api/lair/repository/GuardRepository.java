package kz.railan.villain_lair_api.lair.repository;

import java.util.List;
import kz.railan.villain_lair_api.lair.entity.Guard;
import kz.railan.villain_lair_api.lair.entity.Lair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuardRepository extends JpaRepository<Guard, Long> {
    @Query("select g.power from Guard g where g.lair = :lair and g.active = true")
    List<Integer> findActivePowersByLair(@Param("lair") Lair lair);
}
