package kz.railan.villain_lair_api.villain.dto;

import kz.railan.villain_lair_api.lair.dto.LairSummaryResponse;

public record VillainProfileResponse(
        Long id,
        String username,
        Integer coins,
        Integer reputation,
        LairSummaryResponse lair
) {
}
