package kz.railan.villain_lair_api.battle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import kz.railan.villain_lair_api.lair.entity.Lair;
import kz.railan.villain_lair_api.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attack_battles")
public class AttackBattle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hero_user_id", nullable = false)
    private User heroUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_lair_id", nullable = false)
    private Lair targetLair;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "villain_user_id", nullable = false)
    private User villainUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BattleResult result;

    @Column(name = "base_hero_power", nullable = false)
    private Integer baseHeroPower;

    @Column(name = "final_hero_power", nullable = false)
    private Integer finalHeroPower;

    @Column(name = "base_lair_defense", nullable = false)
    private Integer baseLairDefense;

    @Column(name = "final_lair_defense", nullable = false)
    private Integer finalLairDefense;

    @Column(name = "damage_dealt", nullable = false)
    private Integer damageDealt;

    @Column(name = "hero_damage_received", nullable = false)
    private Integer heroDamageReceived;

    @Column(name = "coins_rewarded_to_hero", nullable = false)
    private Integer coinsRewardedToHero;

    @Column(name = "coins_rewarded_to_villain", nullable = false)
    private Integer coinsRewardedToVillain;

    @Column(name = "battle_log_summary", nullable = false, length = 500)
    private String battleLogSummary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
