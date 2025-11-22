package org.palermo.totalbattle.player.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.palermo.totalbattle.selenium.stacking.Unit;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TroopQuantity {

    private int current;
    private int target;
    private Unit unit;

    public static class TroopQuantityBuilder {
        public TroopQuantity.TroopQuantityBuilder target(long value) {
            this.target = (int) value;
            return this;
        }
    }    
}
