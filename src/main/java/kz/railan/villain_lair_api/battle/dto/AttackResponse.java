package kz.railan.villain_lair_api.battle.dto;

import kz.railan.villain_lair_api.battle.entity.BattleResult;

public record AttackResponse(
        Long battleId,
        BattleResult result,
        Integer heroPower,
        Integer lairDefense,
        Integer damageDealt,
        Integer heroCoinsRewarded,
        Integer villainCoinsRewarded,
        Integer lairHealthAfter,
        Integer heroDamageReceived,
        Integer heroHealthAfter,
        String message
) {
}
