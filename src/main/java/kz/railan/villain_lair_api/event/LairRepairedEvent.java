package kz.railan.villain_lair_api.event;

import java.time.Instant;
import java.util.UUID;

public record LairRepairedEvent(
        String eventId,
        Long villainUserId,
        Long lairId,
        int healthBefore,
        int healthAfter,
        int coinsSpent,
        Instant occurredAt
) {
    public static LairRepairedEvent of(Long villainUserId, Long lairId, int healthBefore, int healthAfter, int coinsSpent) {
        return new LairRepairedEvent(UUID.randomUUID().toString(), villainUserId, lairId,
                healthBefore, healthAfter, coinsSpent, Instant.now());
    }
}
