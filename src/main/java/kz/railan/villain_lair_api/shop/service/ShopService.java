package kz.railan.villain_lair_api.shop.service;

import java.util.List;
import kz.railan.villain_lair_api.common.config.KafkaTopicConfig;
import kz.railan.villain_lair_api.common.exception.ForbiddenActionException;
import kz.railan.villain_lair_api.common.exception.ResourceNotFoundException;
import kz.railan.villain_lair_api.event.ItemPurchasedEvent;
import kz.railan.villain_lair_api.event.KafkaEventPublisher;
import kz.railan.villain_lair_api.economy.entity.TransactionType;
import kz.railan.villain_lair_api.economy.service.EconomyService;
import kz.railan.villain_lair_api.hero.entity.HeroConsumableItem;
import kz.railan.villain_lair_api.hero.entity.HeroInventoryItem;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import kz.railan.villain_lair_api.hero.entity.Weapon;
import kz.railan.villain_lair_api.hero.repository.HeroConsumableItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroInventoryItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroProfileRepository;
import kz.railan.villain_lair_api.hero.repository.WeaponRepository;
import kz.railan.villain_lair_api.lair.entity.DefenseWeapon;
import kz.railan.villain_lair_api.lair.entity.Guard;
import kz.railan.villain_lair_api.lair.entity.Lair;
import kz.railan.villain_lair_api.lair.entity.Trap;
import kz.railan.villain_lair_api.lair.repository.DefenseWeaponRepository;
import kz.railan.villain_lair_api.lair.repository.GuardRepository;
import kz.railan.villain_lair_api.lair.repository.LairRepository;
import kz.railan.villain_lair_api.lair.repository.TrapRepository;
import kz.railan.villain_lair_api.shop.dto.PurchaseResponse;
import kz.railan.villain_lair_api.shop.dto.ShopItemResponse;
import kz.railan.villain_lair_api.shop.entity.ShopItem;
import kz.railan.villain_lair_api.shop.entity.ShopItemCategory;
import kz.railan.villain_lair_api.shop.entity.ShopItemRole;
import kz.railan.villain_lair_api.shop.repository.ShopItemRepository;
import kz.railan.villain_lair_api.user.entity.Role;
import kz.railan.villain_lair_api.user.entity.User;
import kz.railan.villain_lair_api.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopService {
    @Autowired
    private ShopItemRepository shopItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HeroProfileRepository heroProfileRepository;
    @Autowired
    private HeroInventoryItemRepository heroInventoryItemRepository;
    @Autowired
    private HeroConsumableItemRepository heroConsumableItemRepository;
    @Autowired
    private WeaponRepository weaponRepository;
    @Autowired
    private LairRepository lairRepository;
    @Autowired
    private GuardRepository guardRepository;
    @Autowired
    private TrapRepository trapRepository;
    @Autowired
    private DefenseWeaponRepository defenseWeaponRepository;
    @Autowired
    private EconomyService economyService;
    @Autowired
    private KafkaEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<ShopItemResponse> getItemsForCurrentUser(String email) {
        User user = getUserByEmail(email);
        ShopItemRole role = ShopItemRole.valueOf(user.getRole().name());
        return shopItemRepository.findByRoleAndActiveTrueOrderBySortOrderAscIdAsc(role).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PurchaseResponse buyItem(String email, Long itemId) {
        User user = getUserByEmail(email);
        ShopItem item = shopItemRepository.findByIdAndActiveTrue(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop item not found"));

        if (!item.getRole().name().equals(user.getRole().name())) {
            throw new ForbiddenActionException("This item is not available for your role");
        }

        if (item.getPrice() > 0) {
            economyService.debit(user, item.getPrice(), TransactionType.ITEM_PURCHASE, "Purchased " + item.getName());
        }

        if (user.getRole() == Role.HERO) {
            giveHeroItem(email, item);
        } else {
            giveVillainItem(email, item);
        }

        eventPublisher.publish(
                KafkaTopicConfig.ITEM_PURCHASED,
                String.valueOf(user.getId()),
                ItemPurchasedEvent.of(user.getId(), item.getId(), item.getName(), item.getPrice())
        );

        return new PurchaseResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                user.getCoins(),
                "Purchased " + item.getName()
        );
    }

    private void giveHeroItem(String email, ShopItem item) {
        HeroProfile heroProfile = heroProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hero profile not found"));

        switch (item.getCategory()) {
            case WEAPON -> {
                Weapon weapon = weaponRepository.findByCode(item.getCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Weapon template not found"));
                HeroInventoryItem inventoryItem = new HeroInventoryItem();
                inventoryItem.setHeroProfile(heroProfile);
                inventoryItem.setWeapon(weapon);
                inventoryItem.setQuantity(1);
                inventoryItem.setCurrentDurability(weapon.getDurability());
                inventoryItem.setEquipped(false);
                heroInventoryItemRepository.save(inventoryItem);
                boolean hasUsableEquippedWeapon = heroInventoryItemRepository
                        .findByHeroProfileAndEquippedTrue(heroProfile)
                        .filter(current -> current.getCurrentDurability() > 0)
                        .isPresent();
                if (!hasUsableEquippedWeapon) {
                    inventoryItem.setEquipped(true);
                }
            }
            case POTION -> heroConsumableItemRepository
                    .findByHeroProfileAndShopItem(heroProfile, item)
                    .ifPresentOrElse(
                            existing -> existing.setQuantity(existing.getQuantity() + 1),
                            () -> {
                                HeroConsumableItem consumable = new HeroConsumableItem();
                                consumable.setHeroProfile(heroProfile);
                                consumable.setShopItem(item);
                                consumable.setQuantity(1);
                                heroConsumableItemRepository.save(consumable);
                            }
                    );
            default -> throw new ForbiddenActionException("Heroes cannot buy this item type");
        }
    }

    private void giveVillainItem(String email, ShopItem item) {
        Lair lair = lairRepository.findByOwnerUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Villain lair not found"));

        switch (item.getCategory()) {
            case GUARD -> {
                Guard guard = new Guard();
                guard.setLair(lair);
                guard.setName(item.getName());
                guard.setPower(item.getPower());
                guard.setActive(true);
                guardRepository.save(guard);
            }
            case TRAP -> {
                Trap trap = new Trap();
                trap.setLair(lair);
                trap.setName(item.getName());
                trap.setPower(item.getPower());
                trap.setDurability(item.getDurability());
                trap.setActive(true);
                trapRepository.save(trap);
            }
            case DEFENSE_WEAPON -> {
                DefenseWeapon defenseWeapon = new DefenseWeapon();
                defenseWeapon.setLair(lair);
                defenseWeapon.setName(item.getName());
                defenseWeapon.setPower(item.getPower());
                defenseWeapon.setDurability(item.getDurability());
                defenseWeapon.setActive(true);
                defenseWeaponRepository.save(defenseWeapon);
            }
            default -> throw new ForbiddenActionException("Villains cannot buy this item type");
        }
    }

    private ShopItemResponse toResponse(ShopItem item) {
        return new ShopItemResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getCategory(),
                item.getPrice(),
                item.getPower(),
                item.getDurability(),
                item.getDescription()
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
