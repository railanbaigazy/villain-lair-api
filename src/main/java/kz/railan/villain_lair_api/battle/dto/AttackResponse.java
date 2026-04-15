package kz.railan.villain_lair_api.battle.dto;

import kz.railan.villain_lair_api.battle.entity.BattleResult;

public record AttackResponse(
        BattleResult result,
        Integer heroPower,
        Integer lairDefense,
        Integer heroCoinsRewarded,
        Integer villainCoinsRewarded,
        String message
) {
}
