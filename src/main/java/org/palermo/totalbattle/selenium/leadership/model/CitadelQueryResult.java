package org.palermo.totalbattle.selenium.leadership.model;

import lombok.Builder;
import lombok.Getter;
import org.palermo.totalbattle.selenium.stacking.Unit;

@Builder
@Getter
public class CitadelQueryResult {
    
    Unit unit;
    private int qtd;
}
