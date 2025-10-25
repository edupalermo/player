package org.palermo.totalbattle.selenium.leadership.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.palermo.totalbattle.selenium.stacking.Unit;

@Getter
@Builder
public class TroopQuantity {

    @With
    private final int quantity;
    
    private final Unit unit;
}
