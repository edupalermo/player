package org.palermo.totalbattle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.palermo.totalbattle.selenium.stacking.Unit;

@Entity
@Table(name = "unit")
@Getter
@NoArgsConstructor
public class UnitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Matches: unit.player_id BIGINT NOT NULL REFERENCES player(id)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Setter @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_unit_player"))
    private PlayerEntity playerEntity;

    @Setter @Column(name = "priority", nullable = false, length = 255)
    private int priority;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 255)
    private Unit unit;
}
