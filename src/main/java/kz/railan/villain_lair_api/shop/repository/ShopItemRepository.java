package kz.railan.villain_lair_api.shop.repository;

import java.util.List;
import java.util.Optional;
import kz.railan.villain_lair_api.shop.entity.ShopItem;
import kz.railan.villain_lair_api.shop.entity.ShopItemRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
    List<ShopItem> findByRoleAndActiveTrueOrderBySortOrderAscIdAsc(ShopItemRole role);

    Optional<ShopItem> findByIdAndActiveTrue(Long id);
}
