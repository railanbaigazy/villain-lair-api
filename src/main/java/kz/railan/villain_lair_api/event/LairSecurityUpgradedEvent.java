package kz.railan.villain_lair_api.event;

import java.time.Instant;
import java.util.UUID;

public record LairSecurityUpgradedEvent(
        String eventId,
        Long villainUserId,
        Long lairId,
        int oldSecurityLevel,
        int newSecurityLevel,
        int coinsSpent,
        Instant occurredAt
) {
    public static LairSecurityUpgradedEvent of(Long villainUserId, Long lairId, int oldSecurityLevel, int newSecurityLevel, int coinsSpent) {
        return new LairSecurityUpgradedEvent(UUID.randomUUID().toString(), villainUserId, lairId,
                oldSecurityLevel, newSecurityLevel, coinsSpent, Instant.now());
    }
}
