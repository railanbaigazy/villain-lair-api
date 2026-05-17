package kz.railan.villain_lair_api.villain.service;

import java.util.List;
import kz.railan.villain_lair_api.common.config.KafkaTopicConfig;
import kz.railan.villain_lair_api.common.exception.InvalidGameActionException;
import kz.railan.villain_lair_api.common.exception.ResourceNotFoundException;
import kz.railan.villain_lair_api.event.KafkaEventPublisher;
import kz.railan.villain_lair_api.event.LairRepairedEvent;
import kz.railan.villain_lair_api.event.LairSecurityUpgradedEvent;
import kz.railan.villain_lair_api.economy.entity.TransactionType;
import kz.railan.villain_lair_api.economy.service.EconomyService;
import kz.railan.villain_lair_api.lair.dto.LairDefenseUnitResponse;
import kz.railan.villain_lair_api.lair.dto.LairOwnerResponse;
import kz.railan.villain_lair_api.lair.dto.LairSummaryResponse;
import kz.railan.villain_lair_api.lair.dto.UpdateLairRequest;
import kz.railan.villain_lair_api.lair.entity.DefenseWeapon;
import kz.railan.villain_lair_api.lair.entity.Guard;
import kz.railan.villain_lair_api.lair.entity.Lair;
import kz.railan.villain_lair_api.lair.entity.LairStatus;
import kz.railan.villain_lair_api.lair.entity.Trap;
import kz.railan.villain_lair_api.lair.repository.DefenseWeaponRepository;
import kz.railan.villain_lair_api.lair.repository.GuardRepository;
import kz.railan.villain_lair_api.lair.repository.LairRepository;
import kz.railan.villain_lair_api.lair.repository.TrapRepository;
import kz.railan.villain_lair_api.villain.dto.VillainProfileResponse;
import kz.railan.villain_lair_api.villain.entity.VillainProfile;
import kz.railan.villain_lair_api.villain.repository.VillainProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VillainService {
    private static final int MAX_SECURITY_LEVEL = 20;
    private static final int REPAIR_HEALTH_AMOUNT = 25;

    @Autowired
    private VillainProfileRepository villainProfileRepository;
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
    public VillainProfileResponse getMe(String email) {
        VillainProfile profile = getProfile(email);
        Lair lair = getLair(email);
        return new VillainProfileResponse(
                profile.getId(),
                profile.getUser().getUsername(),
                profile.getUser().getCoins(),
                profile.getReputation(),
                toSummary(lair)
        );
    }

    @Transactional(readOnly = true)
    public LairOwnerResponse getOwnLair(String email) {
        return toOwnerResponse(getLair(email));
    }

    @Transactional
    public LairOwnerResponse updateLair(String email, UpdateLairRequest request) {
        Lair lair = getLair(email);
        lair.setName(request.name().trim());
        return toOwnerResponse(lair);
    }

    @Transactional
    public LairOwnerResponse upgradeSecurity(String email) {
        Lair lair = getLair(email);
        if (lair.getSecurityLevel() >= MAX_SECURITY_LEVEL) {
            throw new InvalidGameActionException("Lair security is already at the maximum level");
        }

        int cost = 25 + (lair.getLevel() * 10);
        int oldSecurityLevel = lair.getSecurityLevel();
        economyService.debit(lair.getOwnerUser(), cost, TransactionType.LAIR_UPGRADE, "Upgraded lair security");
        lair.setSecurityLevel(oldSecurityLevel + 1);
        lair.setLevel(lair.getLevel() + 1);

        eventPublisher.publish(
                KafkaTopicConfig.LAIR_SECURITY_UPGRADED,
                String.valueOf(lair.getId()),
                LairSecurityUpgradedEvent.of(lair.getOwnerUser().getId(), lair.getId(),
                        oldSecurityLevel, lair.getSecurityLevel(), cost)
        );

        return toOwnerResponse(lair);
    }

    @Transactional
    public LairOwnerResponse repairLair(String email) {
        Lair lair = getLair(email);
        if (lair.getHealth() >= lair.getMaxHealth()) {
            throw new InvalidGameActionException("Lair health is already full");
        }

        int cost = 15 + (lair.getLevel() * 5);
        int healthBefore = lair.getHealth();
        economyService.debit(lair.getOwnerUser(), cost, TransactionType.LAIR_UPGRADE, "Repaired lair damage");
        lair.setHealth(Math.min(lair.getMaxHealth(), healthBefore + REPAIR_HEALTH_AMOUNT));
        if (lair.getHealth() > 0) {
            lair.setStatus(LairStatus.ACTIVE);
        }

        eventPublisher.publish(
                KafkaTopicConfig.LAIR_REPAIRED,
                String.valueOf(lair.getId()),
                LairRepairedEvent.of(lair.getOwnerUser().getId(), lair.getId(),
                        healthBefore, lair.getHealth(), cost)
        );

        return toOwnerResponse(lair);
    }

    private VillainProfile getProfile(String email) {
        return villainProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Villain profile not found"));
    }

    private Lair getLair(String email) {
        return lairRepository.findByOwnerUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Villain lair not found"));
    }

    private LairSummaryResponse toSummary(Lair lair) {
        return new LairSummaryResponse(
                lair.getId(),
                lair.getName(),
                lair.getLevel(),
                lair.getHealth(),
                lair.getMaxHealth(),
                lair.getSecurityLevel(),
                lair.getStatus().name()
        );
    }

    private LairOwnerResponse toOwnerResponse(Lair lair) {
        return new LairOwnerResponse(
                lair.getId(),
                lair.getName(),
                lair.getLevel(),
                lair.getHealth(),
                lair.getMaxHealth(),
                lair.getSecurityLevel(),
                lair.getStatus().name(),
                guardRepository.findByLairOrderByIdAsc(lair).stream().map(this::toGuardResponse).toList(),
                trapRepository.findByLairOrderByIdAsc(lair).stream().map(this::toTrapResponse).toList(),
                defenseWeaponRepository.findByLairOrderByIdAsc(lair).stream().map(this::toDefenseWeaponResponse).toList()
        );
    }

    private LairDefenseUnitResponse toGuardResponse(Guard guard) {
        return new LairDefenseUnitResponse(guard.getId(), guard.getName(), guard.getPower(), null, guard.getActive());
    }

    private LairDefenseUnitResponse toTrapResponse(Trap trap) {
        return new LairDefenseUnitResponse(trap.getId(), trap.getName(), trap.getPower(), trap.getDurability(), trap.getActive());
    }

    private LairDefenseUnitResponse toDefenseWeaponResponse(DefenseWeapon defenseWeapon) {
        return new LairDefenseUnitResponse(
                defenseWeapon.getId(),
                defenseWeapon.getName(),
                defenseWeapon.getPower(),
                defenseWeapon.getDurability(),
                defenseWeapon.getActive()
        );
    }
}
