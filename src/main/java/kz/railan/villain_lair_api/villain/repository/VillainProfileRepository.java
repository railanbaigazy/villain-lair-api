package kz.railan.villain_lair_api.villain.repository;

import java.util.Optional;
import kz.railan.villain_lair_api.villain.entity.VillainProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VillainProfileRepository extends JpaRepository<VillainProfile, Long> {
    Optional<VillainProfile> findByUserEmail(String email);
}
