package kz.railan.villain_lair_api.hero.repository;

import java.util.List;
import java.util.Optional;
import kz.railan.villain_lair_api.hero.entity.HeroInventoryItem;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HeroInventoryItemRepository extends JpaRepository<HeroInventoryItem, Long> {
    Optional<HeroInventoryItem> findByHeroProfileAndEquippedTrue(HeroProfile heroProfile);

    List<HeroInventoryItem> findByHeroProfileOrderByIdAsc(HeroProfile heroProfile);

    Optional<HeroInventoryItem> findByIdAndHeroProfile(Long id, HeroProfile heroProfile);

    @Modifying
    @Query("UPDATE HeroInventoryItem i SET i.equipped = false WHERE i.heroProfile = :profile")
    void unequipAllForProfile(@Param("profile") HeroProfile profile);
}
