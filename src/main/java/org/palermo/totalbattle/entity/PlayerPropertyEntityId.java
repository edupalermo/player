package org.palermo.totalbattle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PlayerPropertyEntityId implements Serializable {

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    protected PlayerPropertyEntityId() {}

    @Builder
    public PlayerPropertyEntityId(Long playerId, String name) {
        this.playerId = playerId;
        this.name = name;
    }

    public Long getPlayerId() { return playerId; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerPropertyEntityId that)) return false;
        return Objects.equals(playerId, that.playerId)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, name);
    }
}