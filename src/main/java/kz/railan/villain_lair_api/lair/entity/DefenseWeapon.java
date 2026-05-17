package kz.railan.villain_lair_api.lair.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "defense_weapons")
public class DefenseWeapon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lair_id", nullable = false)
    private Lair lair;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private Integer power;

    @Column(nullable = false)
    private Integer durability;

    @Column(nullable = false)
    private Boolean active;
}
