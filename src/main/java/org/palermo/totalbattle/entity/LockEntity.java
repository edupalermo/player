package org.palermo.totalbattle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.palermo.totalbattle.player.Scenario;

@Entity
@Getter
@Table(name = "lock")
public class LockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lock_player"))
    private PlayerEntity playerEntity;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "scenario", nullable = false, length = 255)
    private Scenario scenario;
}
