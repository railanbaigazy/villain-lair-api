package kz.railan.villain_lair_api.battle.service;

import kz.railan.villain_lair_api.battle.dto.AttackRequest;
import kz.railan.villain_lair_api.battle.dto.AttackResponse;
import kz.railan.villain_lair_api.battle.entity.AttackBattle;
import kz.railan.villain_lair_api.battle.entity.BattleResult;
import kz.railan.villain_lair_api.battle.repository.AttackBattleRepository;
import kz.railan.villain_lair_api.common.exception.ForbiddenActionException;
import kz.railan.villain_lair_api.common.exception.ResourceNotFoundException;
import kz.railan.villain_lair_api.hero.entity.HeroInventoryItem;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import kz.railan.villain_lair_api.hero.repository.HeroInventoryItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroProfileRepository;
import kz.railan.villain_lair_api.lair.entity.Lair;
import kz.railan.villain_lair_api.lair.repository.GuardRepository;
import kz.railan.villain_lair_api.lair.repository.LairRepository;
import kz.railan.villain_lair_api.lair.repository.TrapRepository;
import kz.railan.villain_lair_api.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BattleService {
    private static final int HERO_WIN_REWARD = 25;
    private static final int VILLAIN_DEFENSE_REWARD = 10;
    private static final int LAIR_DAMAGE_ON_WIN = 10;

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
    private AttackBattleRepository attackBattleRepository;

    @Transactional
    public AttackResponse attack(String heroEmail, AttackRequest request) {
        HeroProfile heroProfile = heroProfileRepository.findByUserEmail(heroEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Hero profile not found"));
        Lair lair = lairRepository.findById(request.targetLairId())
                .orElseThrow(() -> new ResourceNotFoundException("Target lair not found"));

        User heroUser = heroProfile.getUser();
        User villainUser = lair.getOwnerUser();
        if (heroUser.getId().equals(villainUser.getId())) {
            throw new ForbiddenActionException("Heroes cannot attack their own lair");
        }

        int weaponBonus = inventoryItemRepository.findByHeroProfileAndEquippedTrue(heroProfile)
                .map(HeroInventoryItem::getWeapon)
                .map(weapon -> weapon.getAttackBonus())
                .orElse(0);
        int heroPower = heroProfile.getBaseAttack() + weaponBonus;
        int lairDefense = lair.getSecurityLevel()
                + guardRepository.findActivePowersByLair(lair).stream().mapToInt(Integer::intValue).sum()
                + trapRepository.findActivePowersByLair(lair).stream().mapToInt(Integer::intValue).sum();

        boolean heroWon = heroPower >= lairDefense;
        int heroReward = heroWon ? HERO_WIN_REWARD : 0;
        int villainReward = heroWon ? 0 : VILLAIN_DEFENSE_REWARD;

        if (heroWon) {
            heroUser.setCoins(heroUser.getCoins() + heroReward);
            lair.setHealth(Math.max(0, lair.getHealth() - LAIR_DAMAGE_ON_WIN));
        } else {
            villainUser.setCoins(villainUser.getCoins() + villainReward);
        }

        AttackBattle battle = new AttackBattle();
        battle.setHeroUser(heroUser);
        battle.setTargetLair(lair);
        battle.setResult(heroWon ? BattleResult.WIN : BattleResult.LOSE);
        battle.setHeroPowerSnapshot(heroPower);
        battle.setLairDefenseSnapshot(lairDefense);
        battle.setCoinsRewardedToHero(heroReward);
        battle.setCoinsRewardedToVillain(villainReward);
        attackBattleRepository.save(battle);

        String message = heroWon
                ? "Attack succeeded. The hero looted coins and damaged the lair."
                : "Attack failed. The villain earned a defense reward.";
        return new AttackResponse(battle.getResult(), heroPower, lairDefense, heroReward, villainReward, message);
    }
}
