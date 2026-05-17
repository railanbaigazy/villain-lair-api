package kz.railan.villain_lair_api.hero.repository;

import java.util.List;
import java.util.Optional;
import kz.railan.villain_lair_api.hero.entity.HeroConsumableItem;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import kz.railan.villain_lair_api.shop.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeroConsumableItemRepository extends JpaRepository<HeroConsumableItem, Long> {
    List<HeroConsumableItem> findByHeroProfileOrderByAcquiredAtAsc(HeroProfile profile);

    Optional<HeroConsumableItem> findByHeroProfileAndShopItem(HeroProfile profile, ShopItem shopItem);

    Optional<HeroConsumableItem> findByIdAndHeroProfile(Long id, HeroProfile profile);
}
