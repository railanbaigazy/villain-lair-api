package kz.railan.villain_lair_api.event;

import java.time.Instant;
import java.util.UUID;

public record AttackCompletedEvent(
        String eventId,
        Long battleId,
        Long heroUserId,
        Long villainUserId,
        Long targetLairId,
        String result,
        int heroPower,
        int lairDefense,
        int damageDealt,
        int heroCoinsRewarded,
        int villainCoinsRewarded,
        Instant occurredAt
) {
    public static AttackCompletedEvent of(
            Long battleId, Long heroUserId, Long villainUserId, Long targetLairId,
            String result, int heroPower, int lairDefense, int damageDealt,
            int heroCoinsRewarded, int villainCoinsRewarded
    ) {
        return new AttackCompletedEvent(UUID.randomUUID().toString(), battleId, heroUserId, villainUserId,
                targetLairId, result, heroPower, lairDefense, damageDealt,
                heroCoinsRewarded, villainCoinsRewarded, Instant.now());
    }
}
