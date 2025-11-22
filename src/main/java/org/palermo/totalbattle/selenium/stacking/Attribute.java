package org.palermo.totalbattle.selenium.stacking;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public enum Attribute {
    
    GUARDSMAN, SPECIALIST, 
    
    SCOUT, HUMAN, MOUNTED, RANGED, MELEE, SIEGE, ELEMENTAL, FLYING, EPIC_MONSTER_HUNTER, GIANT, DRAGON, BEAST;

    public static Set<Attribute> GUARDSMAN_RIDER = ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.MOUNTED);
    public static Set<Attribute> GUARDSMAN_ARCHER = ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.RANGED);
    public static Set<Attribute> GUARDSMAN_SPEARMAN = ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.MELEE);
    
    public static Set<Attribute> SPECIALIST_SWORDSMAN = ImmutableSet.of(Attribute.MELEE, Attribute.HUMAN, Attribute.SPECIALIST);
}
