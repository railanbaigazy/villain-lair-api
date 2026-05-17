package kz.railan.villain_lair_api.battle.dto;

import java.time.Instant;
import kz.railan.villain_lair_api.battle.entity.BattleResult;

public record BattleDetailResponse(
        Long id,
        Long heroUserId,
        String heroUsername,
        Long villainUserId,
        String villainUsername,
        Long targetLairId,
        String targetLairName,
        BattleResult result,
        Integer baseHeroPower,
        Integer finalHeroPower,
        Integer baseLairDefense,
        Integer finalLairDefense,
        Integer damageDealt,
        Integer heroDamageReceived,
        Integer heroCoinsRewarded,
        Integer villainCoinsRewarded,
        String battleLogSummary,
        Instant createdAt
) {
}
