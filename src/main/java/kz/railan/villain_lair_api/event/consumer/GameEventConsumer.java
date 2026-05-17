package kz.railan.villain_lair_api.event.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.railan.villain_lair_api.common.config.KafkaTopicConfig;
import kz.railan.villain_lair_api.event.AttackCompletedEvent;
import kz.railan.villain_lair_api.event.ItemPurchasedEvent;
import kz.railan.villain_lair_api.event.LairRepairedEvent;
import kz.railan.villain_lair_api.event.LairSecurityUpgradedEvent;
import kz.railan.villain_lair_api.event.UserRegisteredEvent;
import kz.railan.villain_lair_api.event.WeaponEquippedEvent;
import kz.railan.villain_lair_api.event.entity.GameEventLog;
import kz.railan.villain_lair_api.event.entity.UserStats;
import kz.railan.villain_lair_api.event.repository.GameEventLogRepository;
import kz.railan.villain_lair_api.event.repository.UserStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class GameEventConsumer {

    @Autowired
    private GameEventLogRepository eventLogRepository;
    @Autowired
    private UserStatsRepository userStatsRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicConfig.USER_REGISTERED, groupId = "${spring.kafka.consumer.group-id}")
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Consumed UserRegisteredEvent for user={}", event.userId());
        storeLog(KafkaTopicConfig.USER_REGISTERED, "UserRegisteredEvent", event.eventId(),
                String.valueOf(event.userId()), event);
    }

    @KafkaListener(topics = KafkaTopicConfig.ITEM_PURCHASED, groupId = "${spring.kafka.consumer.group-id}")
    public void onItemPurchased(ItemPurchasedEvent event) {
        log.info("Consumed ItemPurchasedEvent for user={} item={}", event.userId(), event.itemId());
        storeLog(KafkaTopicConfig.ITEM_PURCHASED, "ItemPurchasedEvent", event.eventId(),
                String.valueOf(event.itemId()), event);
    }

    @KafkaListener(topics = KafkaTopicConfig.WEAPON_EQUIPPED, groupId = "${spring.kafka.consumer.group-id}")
    public void onWeaponEquipped(WeaponEquippedEvent event) {
        log.info("Consumed WeaponEquippedEvent for user={} item={}", event.userId(), event.inventoryItemId());
        storeLog(KafkaTopicConfig.WEAPON_EQUIPPED, "WeaponEquippedEvent", event.eventId(),
                String.valueOf(event.inventoryItemId()), event);
    }

    @KafkaListener(topics = KafkaTopicConfig.LAIR_SECURITY_UPGRADED, groupId = "${spring.kafka.consumer.group-id}")
    public void onLairSecurityUpgraded(LairSecurityUpgradedEvent event) {
        log.info("Consumed LairSecurityUpgradedEvent for lair={}", event.lairId());
        storeLog(KafkaTopicConfig.LAIR_SECURITY_UPGRADED, "LairSecurityUpgradedEvent", event.eventId(),
                String.valueOf(event.lairId()), event);
    }

    @KafkaListener(topics = KafkaTopicConfig.LAIR_REPAIRED, groupId = "${spring.kafka.consumer.group-id}")
    public void onLairRepaired(LairRepairedEvent event) {
        log.info("Consumed LairRepairedEvent for lair={}", event.lairId());
        storeLog(KafkaTopicConfig.LAIR_REPAIRED, "LairRepairedEvent", event.eventId(),
                String.valueOf(event.lairId()), event);
    }

    @KafkaListener(topics = KafkaTopicConfig.ATTACK_COMPLETED, groupId = "${spring.kafka.consumer.group-id}")
    public void onAttackCompleted(AttackCompletedEvent event) {
        log.info("Consumed AttackCompletedEvent battle={} result={}", event.battleId(), event.result());
        storeLog(KafkaTopicConfig.ATTACK_COMPLETED, "AttackCompletedEvent", event.eventId(),
                String.valueOf(event.battleId()), event);
        updateUserStats(event);
    }

    private void updateUserStats(AttackCompletedEvent event) {
        boolean heroWon = "HERO_WIN".equals(event.result());

        UserStats heroStats = userStatsRepository.findByUserId(event.heroUserId())
                .orElseGet(() -> newStats(event.heroUserId()));
        if (heroWon) {
            heroStats.setAttacksWon(heroStats.getAttacksWon() + 1);
        } else {
            heroStats.setAttacksLost(heroStats.getAttacksLost() + 1);
        }
        heroStats.setUpdatedAt(Instant.now());
        userStatsRepository.save(heroStats);

        UserStats villainStats = userStatsRepository.findByUserId(event.villainUserId())
                .orElseGet(() -> newStats(event.villainUserId()));
        if (heroWon) {
            villainStats.setDefensesLost(villainStats.getDefensesLost() + 1);
        } else {
            villainStats.setDefensesWon(villainStats.getDefensesWon() + 1);
        }
        villainStats.setUpdatedAt(Instant.now());
        userStatsRepository.save(villainStats);
    }

    private UserStats newStats(Long userId) {
        UserStats stats = new UserStats();
        stats.setUserId(userId);
        return stats;
    }

    private void storeLog(String topic, String eventType, String eventId, String aggregateId, Object event) {
        GameEventLog log = new GameEventLog();
        log.setEventId(eventId);
        log.setTopic(topic);
        log.setEventType(eventType);
        log.setAggregateId(aggregateId);
        log.setConsumedAt(Instant.now());
        try {
            log.setPayload(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.setPayload("{}");
        }
        eventLogRepository.save(log);
    }
}
