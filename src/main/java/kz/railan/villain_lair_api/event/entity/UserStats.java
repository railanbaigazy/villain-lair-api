package kz.railan.villain_lair_api.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "attacks_won", nullable = false)
    private int attacksWon = 0;

    @Column(name = "attacks_lost", nullable = false)
    private int attacksLost = 0;

    @Column(name = "defenses_won", nullable = false)
    private int defensesWon = 0;

    @Column(name = "defenses_lost", nullable = false)
    private int defensesLost = 0;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
