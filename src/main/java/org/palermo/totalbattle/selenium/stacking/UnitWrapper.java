package org.palermo.totalbattle.selenium.stacking;

import lombok.Builder;
import lombok.Getter;

@Builder
public class UnitWrapper {
    
    @Getter
    private Unit unit;
    @Getter
    private Integer limit;
    private Integer health;    
    private Integer strength;
    
    public Integer getStrength() {
        if (this.strength == null) {
            return unit.getStrength();   
        }
        return this.strength;
    }

    public Integer getHealth() {
        if (this.health == null) {
            return unit.getHealth();
        }
        return this.health;
    }
    
    public int getHeadCount() {
        return unit.getHeadCount();
    }
    
    public String getName() {
        return unit.name();
    }
}
