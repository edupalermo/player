package org.palermo.totalbattle.entity;

import jakarta.persistence.*;
import lombok.Setter;

public class PlayerPropertyEntity {

    @EmbeddedId
    private PlayerPropertyId id;

    @MapsId("playerId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_player_property_player"))
    private PlayerEntity player;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "value", nullable = false, length = 255)
    private String value ;
}
