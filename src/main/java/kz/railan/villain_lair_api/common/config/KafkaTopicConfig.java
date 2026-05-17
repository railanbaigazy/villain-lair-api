package kz.railan.villain_lair_api.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public static final String USER_REGISTERED = "game.user.registered";
    public static final String ITEM_PURCHASED = "game.item.purchased";
    public static final String WEAPON_EQUIPPED = "game.weapon.equipped";
    public static final String LAIR_SECURITY_UPGRADED = "game.lair.security-upgraded";
    public static final String LAIR_REPAIRED = "game.lair.repaired";
    public static final String ATTACK_COMPLETED = "game.attack.completed";

    @Bean
    NewTopic userRegisteredTopic() {
        return TopicBuilder.name(USER_REGISTERED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic itemPurchasedTopic() {
        return TopicBuilder.name(ITEM_PURCHASED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic weaponEquippedTopic() {
        return TopicBuilder.name(WEAPON_EQUIPPED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic lairSecurityUpgradedTopic() {
        return TopicBuilder.name(LAIR_SECURITY_UPGRADED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic lairRepairedTopic() {
        return TopicBuilder.name(LAIR_REPAIRED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic attackCompletedTopic() {
        return TopicBuilder.name(ATTACK_COMPLETED).partitions(1).replicas(1).build();
    }
}
