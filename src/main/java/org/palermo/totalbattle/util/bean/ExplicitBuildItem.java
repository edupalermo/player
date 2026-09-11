package org.palermo.totalbattle.util.bean;

import lombok.Builder;
import lombok.Getter;
import org.palermo.totalbattle.selenium.stacking.Unit;

@Builder
@Getter
public class ExplicitBuildItem {
    
    public enum Type {
        TOP_UP, INCREASE
    }
    
    private Unit unit;
    private Type type;
    private int amount; 
}
