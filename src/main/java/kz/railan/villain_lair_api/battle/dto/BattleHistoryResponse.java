package kz.railan.villain_lair_api.battle.dto;

import java.time.Instant;
import kz.railan.villain_lair_api.battle.entity.BattleResult;

public record BattleHistoryResponse(
        Long id,
        Long targetLairId,
        String targetLairName,
        String heroUsername,
        String villainUsername,
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
