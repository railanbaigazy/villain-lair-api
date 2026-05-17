package kz.railan.villain_lair_api.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.railan.villain_lair_api.event.dto.GameEventLogResponse;
import kz.railan.villain_lair_api.event.entity.GameEventLog;
import kz.railan.villain_lair_api.event.repository.GameEventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Events", description = "Game event audit log. Every significant game action is recorded here via Kafka. Returns the most recent 100 events.")
@RestController
@RequestMapping("/api/v1/events")
public class GameEventController {

    @Autowired
    private GameEventLogRepository eventLogRepository;

    @Operation(summary = "Get event log", description = "Returns the last 100 game events ordered by consumed time descending. Filter by event type using the optional 'type' parameter. Valid types: UserRegisteredEvent, ItemPurchasedEvent, WeaponEquippedEvent, LairSecurityUpgradedEvent, LairRepairedEvent, AttackCompletedEvent.")
    @GetMapping
    public List<GameEventLogResponse> getEvents(
            @Parameter(description = "Optional event type filter, e.g. AttackCompletedEvent") @RequestParam(required = false) String type
    ) {
        List<GameEventLog> logs = (type != null && !type.isBlank())
                ? eventLogRepository.findTop100ByEventTypeOrderByConsumedAtDesc(type)
                : eventLogRepository.findTop100ByOrderByConsumedAtDesc();

        return logs.stream().map(this::toResponse).toList();
    }

    private GameEventLogResponse toResponse(GameEventLog log) {
        return new GameEventLogResponse(
                log.getId(),
                log.getEventId(),
                log.getTopic(),
                log.getEventType(),
                log.getAggregateId(),
                log.getPayload(),
                log.getConsumedAt()
        );
    }
}
