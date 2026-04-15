package kz.railan.villain_lair_api.hero.service;

import kz.railan.villain_lair_api.common.exception.ResourceNotFoundException;
import kz.railan.villain_lair_api.hero.dto.HeroProfileResponse;
import kz.railan.villain_lair_api.hero.entity.HeroInventoryItem;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import kz.railan.villain_lair_api.hero.repository.HeroInventoryItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HeroService {
    @Autowired
    private HeroProfileRepository heroProfileRepository;

    @Autowired
    private HeroInventoryItemRepository inventoryItemRepository;

    @Transactional(readOnly = true)
    public HeroProfileResponse getMe(String email) {
        HeroProfile profile = heroProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hero profile not found"));
        HeroInventoryItem equipped = inventoryItemRepository.findByHeroProfileAndEquippedTrue(profile).orElse(null);
        HeroProfileResponse.EquippedWeaponResponse weapon = equipped == null ? null : new HeroProfileResponse.EquippedWeaponResponse(
                equipped.getWeapon().getId(),
                equipped.getWeapon().getName(),
                equipped.getWeapon().getAttackBonus(),
                equipped.getWeapon().getDurability()
        );

        return new HeroProfileResponse(
                profile.getId(),
                profile.getUser().getUsername(),
                profile.getUser().getCoins(),
                profile.getBaseAttack(),
                profile.getHealth(),
                weapon
        );
    }
}
