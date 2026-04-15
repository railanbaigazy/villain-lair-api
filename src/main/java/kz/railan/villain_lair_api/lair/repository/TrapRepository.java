package kz.railan.villain_lair_api.lair.repository;

import java.util.List;
import kz.railan.villain_lair_api.lair.entity.Lair;
import kz.railan.villain_lair_api.lair.entity.Trap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrapRepository extends JpaRepository<Trap, Long> {
    @Query("select t.power from Trap t where t.lair = :lair and t.active = true")
    List<Integer> findActivePowersByLair(@Param("lair") Lair lair);
}
