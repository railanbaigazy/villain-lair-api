package kz.railan.villain_lair_api.lair.dto;

import java.util.List;

public record LairOwnerResponse(
        Long id,
        String name,
        Integer level,
        Integer health,
        Integer maxHealth,
        Integer securityLevel,
        String status,
        List<LairDefenseUnitResponse> guards,
        List<LairDefenseUnitResponse> traps,
        List<LairDefenseUnitResponse> defenseWeapons
) {
}
