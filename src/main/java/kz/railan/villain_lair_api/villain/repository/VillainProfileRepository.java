package kz.railan.villain_lair_api.villain.repository;

import kz.railan.villain_lair_api.villain.entity.VillainProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VillainProfileRepository extends JpaRepository<VillainProfile, Long> {
}
