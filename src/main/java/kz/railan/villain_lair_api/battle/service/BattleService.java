package kz.railan.villain_lair_api.battle.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import kz.railan.villain_lair_api.battle.dto.AttackRequest;
import kz.railan.villain_lair_api.battle.dto.AttackResponse;
import kz.railan.villain_lair_api.battle.dto.BattleDetailResponse;
import kz.railan.villain_lair_api.battle.dto.BattleHistoryResponse;
import kz.railan.villain_lair_api.battle.entity.AttackBattle;
import kz.railan.villain_lair_api.battle.entity.BattleResult;
import kz.railan.villain_lair_api.battle.repository.AttackBattleRepository;
import kz.railan.villain_lair_api.common.config.KafkaTopicConfig;
import kz.railan.villain_lair_api.common.exception.ForbiddenActionException;
import kz.railan.villain_lair_api.common.exception.InvalidGameActionException;
import kz.railan.villain_lair_api.common.exception.ResourceNotFoundException;
import kz.railan.villain_lair_api.event.AttackCompletedEvent;
import kz.railan.villain_lair_api.event.KafkaEventPublisher;
import kz.railan.villain_lair_api.economy.entity.TransactionType;
import kz.railan.villain_lair_api.economy.service.EconomyService;
import kz.railan.villain_lair_api.hero.entity.HeroInventoryItem;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import kz.railan.villain_lair_api.hero.repository.HeroInventoryItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroProfileRepository;
import kz.railan.villain_lair_api.lair.entity.DefenseWeapon;
import kz.railan.villain_lair_api.lair.entity.Lair;
import kz.railan.villain_lair_api.lair.entity.LairStatus;
import kz.railan.villain_lair_api.lair.entity.Trap;
import kz.railan.villain_lair_api.lair.repository.DefenseWeaponRepository;
import kz.railan.villain_lair_api.lair.repository.GuardRepository;
import kz.railan.villain_lair_api.lair.repository.LairRepository;
import kz.railan.villain_lair_api.lair.repository.TrapRepository;
import kz.railan.villain_lair_api.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BattleService {
    @Autowired
    private HeroProfileRepository heroProfileRepository;
    @Autowired
    private HeroInventoryItemRepository inventoryItemRepository;
    @Autowired
    private LairRepository lairRepository;
    @Autowired
    private GuardRepository guardRepository;
    @Autowired
    private TrapRepository trapRepository;
    @Autowired
    private DefenseWeaponRepository defenseWeaponRepository;
    @Autowired
    private AttackBattleRepository attackBattleRepository;
    @Autowired
    private EconomyService economyService;
    @Autowired
    private KafkaEventPublisher eventPublisher;

    @Transactional
    public AttackResponse attack(String heroEmail, AttackRequest request) {
        HeroProfile heroProfile = heroProfileRepository.findByUserEmail(heroEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Hero profile not found"));
        HeroInventoryItem equippedWeapon = inventoryItemRepository.findByHeroProfileAndEquippedTrue(heroProfile)
                .orElseThrow(() -> new InvalidGameActionException("Hero must equip a weapon before attacking"));
        if (equippedWeapon.getCurrentDurability() <= 0) {
            equippedWeapon.setEquipped(false);
            throw new InvalidGameActionException("Equipped weapon is broken");
        }

        Lair lair = lairRepository.findById(request.targetLairId())
                .orElseThrow(() -> new ResourceNotFoundException("Target lair not found"));

        User heroUser = heroProfile.getUser();
        User villainUser = lair.getOwnerUser();
        if (heroUser.getId().equals(villainUser.getId())) {
            throw new ForbiddenActionException("Heroes cannot attack their own lair");
        }
        if (lair.getStatus() != LairStatus.ACTIVE || lair.getHealth() <= 0) {
            throw new InvalidGameActionException("Target lair is not active");
        }

        int baseHeroPower = heroProfile.getBaseAttack() + equippedWeapon.getWeapon().getAttackBonus();
        int baseLairDefense = lair.getSecurityLevel()
                + guardRepository.findByLairAndActiveTrue(lair).stream().mapToInt(guard -> guard.getPower()).sum()
                + trapRepository.findByLairAndActiveTrue(lair).stream().mapToInt(trap -> trap.getPower()).sum()
                + defenseWeaponRepository.findByLairAndActiveTrue(lair).stream().mapToInt(weapon -> weapon.getPower()).sum();

        int finalHeroPower = Math.max(0, baseHeroPower + randomModifier());
        int finalLairDefense = Math.max(0, baseLairDefense + randomModifier());
        boolean heroWon = finalHeroPower >= finalLairDefense;

        int heroReward = heroWon ? 40 + (lair.getLevel() * 10) : 0;
        int villainReward = heroWon ? 0 : 20 + (lair.getLevel() * 5);
        int damageDealt = heroWon ? 10 + Math.max(0, finalHeroPower - finalLairDefense) : 0;
        int heroDamage = heroWon ? 0 : 5 + Math.max(0, finalLairDefense - finalHeroPower);

        if (heroWon) {
            economyService.credit(heroUser, heroReward, TransactionType.ATTACK_WIN_REWARD, "Won attack on lair " + lair.getName());
            lair.setHealth(Math.max(0, lair.getHealth() - damageDealt));
            if (lair.getHealth() == 0) {
                lair.setStatus(LairStatus.BREACHED);
            }
        } else {
            economyService.credit(villainUser, villainReward, TransactionType.DEFENSE_WIN_REWARD, "Defended lair " + lair.getName());
            heroProfile.setHealth(Math.max(0, heroProfile.getHealth() - heroDamage));
        }

        decreaseWeaponDurability(equippedWeapon);
        trapRepository.findByLairAndActiveTrue(lair).forEach(this::decreaseTrapDurability);

        AttackBattle battle = new AttackBattle();
        battle.setHeroUser(heroUser);
        battle.setVillainUser(villainUser);
        battle.setTargetLair(lair);
        battle.setResult(heroWon ? BattleResult.HERO_WIN : BattleResult.VILLAIN_WIN);
        battle.setBaseHeroPower(baseHeroPower);
        battle.setFinalHeroPower(finalHeroPower);
        battle.setBaseLairDefense(baseLairDefense);
        battle.setFinalLairDefense(finalLairDefense);
        battle.setDamageDealt(damageDealt);
        battle.setHeroDamageReceived(heroDamage);
        battle.setCoinsRewardedToHero(heroReward);
        battle.setCoinsRewardedToVillain(villainReward);
        battle.setBattleLogSummary(buildBattleSummary(heroWon, heroUser, villainUser, lair, damageDealt, heroDamage, heroReward, villainReward));
        attackBattleRepository.save(battle);

        eventPublisher.publish(
                KafkaTopicConfig.ATTACK_COMPLETED,
                String.valueOf(battle.getId()),
                AttackCompletedEvent.of(battle.getId(), heroUser.getId(), villainUser.getId(),
                        lair.getId(), battle.getResult().name(), finalHeroPower, finalLairDefense,
                        damageDealt, heroReward, villainReward)
        );

        String message = heroWon
                ? "Attack succeeded and the lair took damage."
                : "Attack failed and the villain held the line.";

        return new AttackResponse(
                battle.getId(),
                battle.getResult(),
                finalHeroPower,
                finalLairDefense,
                damageDealt,
                heroReward,
                villainReward,
                lair.getHealth(),
                heroDamage,
                heroProfile.getHealth(),
                message
        );
    }

    @Transactional(readOnly = true)
    public List<BattleHistoryResponse> getHeroBattleHistory(String email) {
        return attackBattleRepository.findByHeroUserEmailOrderByCreatedAtDescIdDesc(email).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BattleHistoryResponse> getVillainBattleHistory(String email) {
        return attackBattleRepository.findByVillainUserEmailOrderByCreatedAtDescIdDesc(email).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BattleDetailResponse getBattleForParticipant(String email, Long battleId) {
        AttackBattle battle = attackBattleRepository.findById(battleId)
                .orElseThrow(() -> new ResourceNotFoundException("Battle not found"));
        boolean involved = battle.getHeroUser().getEmail().equals(email) || battle.getVillainUser().getEmail().equals(email);
        if (!involved) {
            throw new ForbiddenActionException("You are not allowed to view this battle");
        }
        return toDetailResponse(battle);
    }

    private void decreaseWeaponDurability(HeroInventoryItem equippedWeapon) {
        equippedWeapon.setCurrentDurability(Math.max(0, equippedWeapon.getCurrentDurability() - 1));
        if (equippedWeapon.getCurrentDurability() == 0) {
            equippedWeapon.setEquipped(false);
        }
    }

    private void decreaseTrapDurability(Trap trap) {
        trap.setDurability(Math.max(0, trap.getDurability() - 1));
        if (trap.getDurability() == 0) {
            trap.setActive(false);
        }
    }

    private int randomModifier() {
        return ThreadLocalRandom.current().nextInt(-3, 4);
    }

    private String buildBattleSummary(
            boolean heroWon,
            User heroUser,
            User villainUser,
            Lair lair,
            int damageDealt,
            int heroDamage,
            int heroReward,
            int villainReward
    ) {
        if (heroWon) {
            return heroUser.getUsername() + " breached " + villainUser.getUsername() + "'s lair for "
                    + damageDealt + " damage and earned " + heroReward + " coins.";
        }
        return villainUser.getUsername() + " defended " + lair.getName() + " and earned " + villainReward
                + " coins. " + heroUser.getUsername() + " took " + heroDamage + " damage.";
    }

    private BattleHistoryResponse toHistoryResponse(AttackBattle battle) {
        return new BattleHistoryResponse(
                battle.getId(),
                battle.getTargetLair().getId(),
                battle.getTargetLair().getName(),
                battle.getHeroUser().getUsername(),
                battle.getVillainUser().getUsername(),
                battle.getResult(),
                battle.getBaseHeroPower(),
                battle.getFinalHeroPower(),
                battle.getBaseLairDefense(),
                battle.getFinalLairDefense(),
                battle.getDamageDealt(),
                battle.getHeroDamageReceived(),
                battle.getCoinsRewardedToHero(),
                battle.getCoinsRewardedToVillain(),
                battle.getBattleLogSummary(),
                battle.getCreatedAt()
        );
    }

    private BattleDetailResponse toDetailResponse(AttackBattle battle) {
        return new BattleDetailResponse(
                battle.getId(),
                battle.getHeroUser().getId(),
                battle.getHeroUser().getUsername(),
                battle.getVillainUser().getId(),
                battle.getVillainUser().getUsername(),
                battle.getTargetLair().getId(),
                battle.getTargetLair().getName(),
                battle.getResult(),
                battle.getBaseHeroPower(),
                battle.getFinalHeroPower(),
                battle.getBaseLairDefense(),
                battle.getFinalLairDefense(),
                battle.getDamageDealt(),
                battle.getHeroDamageReceived(),
                battle.getCoinsRewardedToHero(),
                battle.getCoinsRewardedToVillain(),
                battle.getBattleLogSummary(),
                battle.getCreatedAt()
        );
    }
}
