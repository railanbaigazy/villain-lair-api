package kz.railan.villain_lair_api.shop.service;

import kz.railan.villain_lair_api.common.exception.InsufficientCoinsException;
import kz.railan.villain_lair_api.common.exception.ResourceNotFoundException;
import kz.railan.villain_lair_api.economy.service.EconomyService;
import kz.railan.villain_lair_api.event.KafkaEventPublisher;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import kz.railan.villain_lair_api.hero.entity.Weapon;
import kz.railan.villain_lair_api.hero.repository.HeroInventoryItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroProfileRepository;
import kz.railan.villain_lair_api.hero.repository.WeaponRepository;
import kz.railan.villain_lair_api.lair.repository.DefenseWeaponRepository;
import kz.railan.villain_lair_api.lair.repository.GuardRepository;
import kz.railan.villain_lair_api.lair.repository.LairRepository;
import kz.railan.villain_lair_api.lair.repository.TrapRepository;
import kz.railan.villain_lair_api.shop.entity.ShopItem;
import kz.railan.villain_lair_api.shop.entity.ShopItemCategory;
import kz.railan.villain_lair_api.shop.entity.ShopItemRole;
import kz.railan.villain_lair_api.shop.repository.ShopItemRepository;
import kz.railan.villain_lair_api.user.entity.Role;
import kz.railan.villain_lair_api.user.entity.User;
import kz.railan.villain_lair_api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {
    @Mock ShopItemRepository shopItemRepository;
    @Mock UserRepository userRepository;
    @Mock HeroProfileRepository heroProfileRepository;
    @Mock HeroInventoryItemRepository heroInventoryItemRepository;
    @Mock WeaponRepository weaponRepository;
    @Mock LairRepository lairRepository;
    @Mock GuardRepository guardRepository;
    @Mock TrapRepository trapRepository;
    @Mock DefenseWeaponRepository defenseWeaponRepository;
    @Mock EconomyService economyService;
    @Mock KafkaEventPublisher eventPublisher;

    @InjectMocks
    ShopService shopService;

    @Test
    void buyItem_insufficientCoins_throwsException() {
        User hero = hero(1L, 10);
        ShopItem item = weaponShopItem(99L, 200);

        when(userRepository.findByEmail("hero@test.com")).thenReturn(Optional.of(hero));
        when(shopItemRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(item));
        doThrow(new InsufficientCoinsException("Not enough coins"))
                .when(economyService).debit(any(), anyInt(), any(), anyString());

        assertThrows(InsufficientCoinsException.class, () -> shopService.buyItem("hero@test.com", 99L));
    }

    @Test
    void buyItem_heroWeapon_addsToInventory() {
        User hero = hero(1L, 500);
        ShopItem item = weaponShopItem(1L, 60);

        Weapon weapon = new Weapon();
        weapon.setDurability(40);

        HeroProfile profile = new HeroProfile();
        profile.setUser(hero);

        when(userRepository.findByEmail("hero@test.com")).thenReturn(Optional.of(hero));
        when(shopItemRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(item));
        when(heroProfileRepository.findByUserEmail("hero@test.com")).thenReturn(Optional.of(profile));
        when(weaponRepository.findByCode(item.getCode())).thenReturn(Optional.of(weapon));
        when(heroInventoryItemRepository.findByHeroProfileAndEquippedTrue(profile)).thenReturn(Optional.empty());

        shopService.buyItem("hero@test.com", 1L);

        verify(heroInventoryItemRepository).save(any());
        verify(eventPublisher).publish(any(), any(), any());
    }

    @Test
    void buyItem_itemNotFound_throwsResourceNotFound() {
        User hero = hero(1L, 500);
        when(userRepository.findByEmail("hero@test.com")).thenReturn(Optional.of(hero));
        when(shopItemRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shopService.buyItem("hero@test.com", 999L));
    }

    private User hero(Long id, int coins) {
        User u = new User();
        u.setId(id);
        u.setEmail("hero@test.com");
        u.setUsername("hero");
        u.setRole(Role.HERO);
        u.setCoins(coins);
        return u;
    }

    private ShopItem weaponShopItem(Long id, int price) {
        ShopItem item = new ShopItem();
        item.setId(id);
        item.setCode("IRON_SWORD");
        item.setName("Iron Sword");
        item.setRole(ShopItemRole.HERO);
        item.setCategory(ShopItemCategory.WEAPON);
        item.setPrice(price);
        item.setPower(18);
        item.setDurability(40);
        return item;
    }
}
