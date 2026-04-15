package kz.railan.villain_lair_api.hero.repository;

import java.util.Optional;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeroProfileRepository extends JpaRepository<HeroProfile, Long> {
    Optional<HeroProfile> findByUserEmail(String email);
}
