package org.palermo.totalbattle.player.bean;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.util.Navigate;

@Builder
@Getter
public class UnitQuantity {
    
    private final Unit unit;
    @With
    private final long quantity;

}
