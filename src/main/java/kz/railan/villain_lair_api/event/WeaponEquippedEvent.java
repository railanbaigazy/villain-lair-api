package kz.railan.villain_lair_api.event;

import java.time.Instant;
import java.util.UUID;

public record WeaponEquippedEvent(
        String eventId,
        Long userId,
        Long inventoryItemId,
        String weaponName,
        Instant occurredAt
) {
    public static WeaponEquippedEvent of(Long userId, Long inventoryItemId, String weaponName) {
        return new WeaponEquippedEvent(UUID.randomUUID().toString(), userId, inventoryItemId, weaponName, Instant.now());
    }
}
