package kz.railan.villain_lair_api.lair.service;

import java.util.List;
import kz.railan.villain_lair_api.common.exception.ResourceNotFoundException;
import kz.railan.villain_lair_api.lair.dto.LairDefenseUnitResponse;
import kz.railan.villain_lair_api.lair.dto.LairPublicDetailResponse;
import kz.railan.villain_lair_api.lair.dto.LairPublicResponse;
import kz.railan.villain_lair_api.lair.entity.DefenseWeapon;
import kz.railan.villain_lair_api.lair.entity.Guard;
import kz.railan.villain_lair_api.lair.entity.Lair;
import kz.railan.villain_lair_api.lair.entity.Trap;
import kz.railan.villain_lair_api.lair.repository.DefenseWeaponRepository;
import kz.railan.villain_lair_api.lair.repository.GuardRepository;
import kz.railan.villain_lair_api.lair.repository.LairRepository;
import kz.railan.villain_lair_api.lair.repository.TrapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LairService {
    @Autowired
    private LairRepository lairRepository;
    @Autowired
    private GuardRepository guardRepository;
    @Autowired
    private TrapRepository trapRepository;
    @Autowired
    private DefenseWeaponRepository defenseWeaponRepository;

    @Transactional(readOnly = true)
    public List<LairPublicResponse> listPublicLairs() {
        return lairRepository.findAll().stream().map(this::toPublicResponse).toList();
    }

    @Transactional(readOnly = true)
    public LairPublicDetailResponse getLairDetail(String currentUserEmail, Long lairId) {
        Lair lair = lairRepository.findById(lairId)
                .orElseThrow(() -> new ResourceNotFoundException("Lair not found"));
        boolean ownerView = lair.getOwnerUser().getEmail().equals(currentUserEmail);

        return new LairPublicDetailResponse(
                lair.getId(),
                lair.getName(),
                lair.getLevel(),
                lair.getHealth(),
                lair.getMaxHealth(),
                lair.getSecurityLevel(),
                lair.getStatus().name(),
                lair.getOwnerUser().getUsername(),
                ownerView ? guardRepository.findByLairOrderByIdAsc(lair).stream().map(this::toGuardResponse).toList() : List.of(),
                ownerView ? trapRepository.findByLairOrderByIdAsc(lair).stream().map(this::toTrapResponse).toList() : List.of(),
                ownerView ? defenseWeaponRepository.findByLairOrderByIdAsc(lair).stream().map(this::toDefenseWeaponResponse).toList() : List.of()
        );
    }

    private LairPublicResponse toPublicResponse(Lair lair) {
        return new LairPublicResponse(
                lair.getId(),
                lair.getName(),
                lair.getLevel(),
                lair.getHealth(),
                lair.getMaxHealth(),
                lair.getSecurityLevel(),
                lair.getOwnerUser().getUsername(),
                lair.getStatus().name()
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
