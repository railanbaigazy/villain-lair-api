package kz.railan.villain_lair_api.lair.dto;

import java.util.List;

public record LairPublicDetailResponse(
        Long id,
        String name,
        Integer level,
        Integer health,
        Integer maxHealth,
        Integer securityLevel,
        String status,
        String ownerUsername,
        List<LairDefenseUnitResponse> guards,
        List<LairDefenseUnitResponse> traps,
        List<LairDefenseUnitResponse> defenseWeapons
) {
}
