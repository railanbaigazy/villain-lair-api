package kz.railan.villain_lair_api.lair.repository;

import kz.railan.villain_lair_api.lair.entity.Lair;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LairRepository extends JpaRepository<Lair, Long> {
}
