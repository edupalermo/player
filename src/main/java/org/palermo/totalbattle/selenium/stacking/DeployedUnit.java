package org.palermo.totalbattle.selenium.stacking;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Builder @Getter
public class DeployedUnit {
    
    private final Unit unit;
    private final int quantity;
    @With
    private final boolean alive;
    @With
    private final int round;
    
}
