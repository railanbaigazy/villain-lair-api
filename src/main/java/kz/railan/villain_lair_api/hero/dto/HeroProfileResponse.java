package kz.railan.villain_lair_api.hero.dto;

public record HeroProfileResponse(
        Long id,
        String username,
        Integer coins,
        Integer baseAttack,
        Integer health,
        EquippedWeaponResponse equippedWeapon
) {
    public record EquippedWeaponResponse(
            Long id,
            String name,
            Integer attackBonus,
            Integer durability
    ) {
    }
}
