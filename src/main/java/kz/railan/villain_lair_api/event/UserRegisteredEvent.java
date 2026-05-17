package kz.railan.villain_lair_api.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        String eventId,
        Long userId,
        String username,
        String role,
        Instant occurredAt
) {
    public static UserRegisteredEvent of(Long userId, String username, String role) {
        return new UserRegisteredEvent(UUID.randomUUID().toString(), userId, username, role, Instant.now());
    }
}
