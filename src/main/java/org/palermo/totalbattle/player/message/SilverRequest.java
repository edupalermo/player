package org.palermo.totalbattle.player.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.palermo.totalbattle.player.Player;

@Getter
@SuperBuilder
@NoArgsConstructor
public class SilverRequest extends Message {
    
    public Player target;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SilverRequest that = (SilverRequest) o;

        return target == that.target;
    }

    @Override
    public int hashCode() {
        return target != null ? target.hashCode() : 0;
    }
}
