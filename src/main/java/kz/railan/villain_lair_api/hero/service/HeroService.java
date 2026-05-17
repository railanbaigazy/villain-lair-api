package kz.railan.villain_lair_api.hero.service;

import java.util.List;
import kz.railan.villain_lair_api.common.config.KafkaTopicConfig;
import kz.railan.villain_lair_api.common.exception.InvalidGameActionException;
import kz.railan.villain_lair_api.common.exception.ResourceNotFoundException;
import kz.railan.villain_lair_api.event.KafkaEventPublisher;
import kz.railan.villain_lair_api.event.WeaponEquippedEvent;
import kz.railan.villain_lair_api.hero.dto.ConsumableItemResponse;
import kz.railan.villain_lair_api.hero.dto.HeroProfileResponse;
import kz.railan.villain_lair_api.hero.dto.InventoryItemResponse;
import kz.railan.villain_lair_api.hero.dto.UseConsumableResponse;
import kz.railan.villain_lair_api.hero.entity.HeroConsumableItem;
import kz.railan.villain_lair_api.hero.entity.HeroInventoryItem;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import kz.railan.villain_lair_api.hero.repository.HeroConsumableItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroInventoryItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HeroService {
    private static final int MAX_HERO_HEALTH = 100;

    @Autowired
    private HeroProfileRepository heroProfileRepository;
    @Autowired
    private HeroInventoryItemRepository inventoryItemRepository;
    @Autowired
    private HeroConsumableItemRepository heroConsumableItemRepository;
    @Autowired
    private KafkaEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public HeroProfileResponse getMe(String email) {
        HeroProfile profile = getProfile(email);
        InventoryItemResponse weapon = inventoryItemRepository.findByHeroProfileAndEquippedTrue(profile)
                .map(this::toInventoryItemResponse)
                .orElse(null);

        return new HeroProfileResponse(
                profile.getId(),
                profile.getUser().getUsername(),
                profile.getUser().getCoins(),
                profile.getBaseAttack(),
                profile.getHealth(),
                weapon
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getInventory(String email) {
        HeroProfile profile = getProfile(email);
        return inventoryItemRepository.findByHeroProfileOrderByIdAsc(profile).stream()
                .map(this::toInventoryItemResponse)
                .toList();
    }

    @Transactional
    public InventoryItemResponse equipWeapon(String email, Long inventoryItemId) {
        HeroProfile profile = getProfile(email);
        HeroInventoryItem item = inventoryItemRepository.findByIdAndHeroProfile(inventoryItemId, profile)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        if (item.getCurrentDurability() <= 0) {
            throw new InvalidGameActionException("Broken weapons cannot be equipped");
        }

        inventoryItemRepository.unequipAllForProfile(profile);
        item.setEquipped(true);

        eventPublisher.publish(
                KafkaTopicConfig.WEAPON_EQUIPPED,
                String.valueOf(profile.getUser().getId()),
                WeaponEquippedEvent.of(profile.getUser().getId(), item.getId(), item.getWeapon().getName())
        );

        return toInventoryItemResponse(item);
    }

    @Transactional(readOnly = true)
    public List<ConsumableItemResponse> getConsumables(String email) {
        HeroProfile profile = getProfile(email);
        return heroConsumableItemRepository.findByHeroProfileOrderByAcquiredAtAsc(profile).stream()
                .map(c -> new ConsumableItemResponse(c.getId(), c.getShopItem().getCode(), c.getShopItem().getName(), c.getShopItem().getPower(), c.getQuantity()))
                .toList();
    }

    @Transactional
    public UseConsumableResponse useConsumable(String email, Long consumableItemId) {
        HeroProfile profile = getProfile(email);
        HeroConsumableItem consumable = heroConsumableItemRepository.findByIdAndHeroProfile(consumableItemId, profile)
                .orElseThrow(() -> new ResourceNotFoundException("Consumable item not found"));

        if (profile.getHealth() >= MAX_HERO_HEALTH) {
            throw new InvalidGameActionException("Hero health is already full");
        }

        int healAmount = consumable.getShopItem().getPower();
        profile.setHealth(Math.min(MAX_HERO_HEALTH, profile.getHealth() + healAmount));

        if (consumable.getQuantity() > 1) {
            consumable.setQuantity(consumable.getQuantity() - 1);
        } else {
            heroConsumableItemRepository.delete(consumable);
        }

        return new UseConsumableResponse(consumableItemId, consumable.getShopItem().getName(), healAmount, profile.getHealth());
    }

    private HeroProfile getProfile(String email) {
        return heroProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hero profile not found"));
    }

    private InventoryItemResponse toInventoryItemResponse(HeroInventoryItem item) {
        return new InventoryItemResponse(
                item.getId(),
                item.getWeapon().getCode(),
                item.getWeapon().getName(),
                item.getWeapon().getAttackBonus(),
                item.getCurrentDurability(),
                item.getWeapon().getDurability(),
                item.getEquipped()
        );
    }
}
