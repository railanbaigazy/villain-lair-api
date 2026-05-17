package kz.railan.villain_lair_api.event.dto;

import java.time.Instant;

public record GameEventLogResponse(
        Long id,
        String eventId,
        String topic,
        String eventType,
        String aggregateId,
        String payload,
        Instant consumedAt
) {
}
