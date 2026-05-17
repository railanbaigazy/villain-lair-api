package kz.railan.villain_lair_api.battle.service;

import kz.railan.villain_lair_api.battle.dto.AttackRequest;
import kz.railan.villain_lair_api.battle.dto.AttackResponse;
import kz.railan.villain_lair_api.battle.entity.AttackBattle;
import kz.railan.villain_lair_api.battle.entity.BattleResult;
import kz.railan.villain_lair_api.battle.repository.AttackBattleRepository;
import kz.railan.villain_lair_api.common.exception.InvalidGameActionException;
import kz.railan.villain_lair_api.economy.service.EconomyService;
import kz.railan.villain_lair_api.event.KafkaEventPublisher;
import kz.railan.villain_lair_api.hero.entity.HeroInventoryItem;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import kz.railan.villain_lair_api.hero.entity.Weapon;
import kz.railan.villain_lair_api.hero.repository.HeroInventoryItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroProfileRepository;
import kz.railan.villain_lair_api.lair.entity.Lair;
import kz.railan.villain_lair_api.lair.entity.LairStatus;
import kz.railan.villain_lair_api.lair.repository.DefenseWeaponRepository;
import kz.railan.villain_lair_api.lair.repository.GuardRepository;
import kz.railan.villain_lair_api.lair.repository.LairRepository;
import kz.railan.villain_lair_api.lair.repository.TrapRepository;
import kz.railan.villain_lair_api.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {
    @Mock HeroProfileRepository heroProfileRepository;
    @Mock HeroInventoryItemRepository inventoryItemRepository;
    @Mock LairRepository lairRepository;
    @Mock GuardRepository guardRepository;
    @Mock TrapRepository trapRepository;
    @Mock DefenseWeaponRepository defenseWeaponRepository;
    @Mock AttackBattleRepository attackBattleRepository;
    @Mock EconomyService economyService;
    @Mock KafkaEventPublisher eventPublisher;

    @InjectMocks
    BattleService battleService;

    @Test
    void attack_persistsBattleResult() {
        User heroUser = user(1L, "hero@test.com");
        User villainUser = user(2L, "villain@test.com");

        HeroProfile profile = new HeroProfile();
        profile.setUser(heroUser);
        profile.setBaseAttack(25);

        Weapon weapon = new Weapon();
        weapon.setName("Basic Sword");
        weapon.setAttackBonus(10);
        weapon.setDurability(25);

        HeroInventoryItem item = new HeroInventoryItem();
        item.setWeapon(weapon);
        item.setCurrentDurability(10);
        item.setEquipped(true);

        Lair lair = new Lair();
        lair.setId(10L);
        lair.setName("Dark Lair");
        lair.setOwnerUser(villainUser);
        lair.setStatus(LairStatus.ACTIVE);
        lair.setHealth(100);
        lair.setMaxHealth(100);
        lair.setSecurityLevel(5);
        lair.setLevel(1);

        when(heroProfileRepository.findByUserEmail("hero@test.com")).thenReturn(Optional.of(profile));
        when(inventoryItemRepository.findByHeroProfileAndEquippedTrue(profile)).thenReturn(Optional.of(item));
        when(lairRepository.findById(10L)).thenReturn(Optional.of(lair));
        when(guardRepository.findByLairAndActiveTrue(lair)).thenReturn(List.of());
        when(trapRepository.findByLairAndActiveTrue(lair)).thenReturn(List.of());
        when(defenseWeaponRepository.findByLairAndActiveTrue(lair)).thenReturn(List.of());
        when(attackBattleRepository.save(any(AttackBattle.class))).thenAnswer(inv -> {
            AttackBattle b = inv.getArgument(0);
            b.setId(99L);
            return b;
        });
        when(trapRepository.findByLairAndActiveTrue(lair)).thenReturn(List.of());

        AttackResponse response = battleService.attack("hero@test.com", new AttackRequest(10L));

        assertNotNull(response);
        assertTrue(response.result() == BattleResult.HERO_WIN || response.result() == BattleResult.VILLAIN_WIN);
        verify(attackBattleRepository).save(any(AttackBattle.class));
        verify(eventPublisher).publish(any(), any(), any());
    }

    @Test
    void attack_brokenWeapon_throwsInvalidGameAction() {
        User heroUser = user(1L, "hero@test.com");

        HeroProfile profile = new HeroProfile();
        profile.setUser(heroUser);
        profile.setBaseAttack(25);

        Weapon weapon = new Weapon();
        weapon.setName("Rusty Sword");
        weapon.setAttackBonus(5);
        weapon.setDurability(0);

        HeroInventoryItem item = new HeroInventoryItem();
        item.setWeapon(weapon);
        item.setCurrentDurability(0);
        item.setEquipped(true);

        when(heroProfileRepository.findByUserEmail("hero@test.com")).thenReturn(Optional.of(profile));
        when(inventoryItemRepository.findByHeroProfileAndEquippedTrue(profile)).thenReturn(Optional.of(item));

        assertThrows(InvalidGameActionException.class,
                () -> battleService.attack("hero@test.com", new AttackRequest(10L)));
    }

    private User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setUsername(email.split("@")[0]);
        u.setCoins(500);
        return u;
    }
}
