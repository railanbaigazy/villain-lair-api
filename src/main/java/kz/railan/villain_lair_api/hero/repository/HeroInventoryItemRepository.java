package kz.railan.villain_lair_api.hero.repository;

import java.util.Optional;
import kz.railan.villain_lair_api.hero.entity.HeroInventoryItem;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeroInventoryItemRepository extends JpaRepository<HeroInventoryItem, Long> {
    Optional<HeroInventoryItem> findByHeroProfileAndEquippedTrue(HeroProfile heroProfile);
}
