package org.palermo.totalbattle.selenium.stacking;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationBuilder {

    private int leadership;
    private int dominance;
    private int authority;
    private List<UnitWrapper> units = new ArrayList<>();
    private int wave = 1;

    public Configuration build() {
        return new Configuration(leadership, dominance, authority, units, wave);
    }

    public ConfigurationBuilder leadership(int leadership) {
        this.leadership = leadership;
        return this;
    }

    public ConfigurationBuilder dominance(int dominance) {
        this.dominance = dominance;
        return this;
    }

    public ConfigurationBuilder authority(int authority) {
        this.authority = authority;
        return this;
    }

    public ConfigurationBuilder addUnit(Unit unit) {
        units.add(UnitWrapper.builder().unit(unit).build());
        return this;
    }
    
    public ConfigurationBuilder wave(int wave) {
        this.wave = wave;
        return this;
    }
}
