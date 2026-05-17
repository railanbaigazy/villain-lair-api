package kz.railan.villain_lair_api.event;

import java.time.Instant;
import java.util.UUID;

public record ItemPurchasedEvent(
        String eventId,
        Long userId,
        Long itemId,
        String itemName,
        int price,
        Instant occurredAt
) {
    public static ItemPurchasedEvent of(Long userId, Long itemId, String itemName, int price) {
        return new ItemPurchasedEvent(UUID.randomUUID().toString(), userId, itemId, itemName, price, Instant.now());
    }
}
