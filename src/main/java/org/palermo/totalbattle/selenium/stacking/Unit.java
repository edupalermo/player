package org.palermo.totalbattle.selenium.stacking;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import org.palermo.totalbattle.selenium.leadership.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.Set;

@Getter
public enum Unit {
    
    S1_SWORDSMAN(50, 150, UnitType.MELEE, 1, Attribute.GUARDSMAN_SPEARMAN, "leadership/swordsman_i.png", Pool.LEADERSHIP, 1),
    S2_SWORDSMAN(90, 270, UnitType.MELEE, 2, Attribute.GUARDSMAN_SPEARMAN, "leadership/swordsman_ii.png", Pool.LEADERSHIP, 1),
    S3_SWORDSMAN(160, 480, UnitType.MELEE, 3, Attribute.GUARDSMAN_SPEARMAN, "leadership/swordsman_iii.png", Pool.LEADERSHIP, 1),
    S4_SWORDSMAN(290, 870, UnitType.MELEE, 4, Attribute.GUARDSMAN_SPEARMAN, "leadership/swordsman_iv.png", Pool.LEADERSHIP, 1),
    G1_MELEE(50, 150, UnitType.MELEE, 1, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_i.png", Pool.LEADERSHIP, 1),
    G1_RANGED(50, 150, UnitType.ARCHER, 1, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_i.png", Pool.LEADERSHIP, 1),
    G1_MOUNTED(100, 300, UnitType.RIDER, 1, Attribute.GUARDSMAN_RIDER, "leadership/mounted_i.png", Pool.LEADERSHIP, 2),
    G1_ENGINEER(250, 1500, UnitType.CATAPULT, 1, Pool.LEADERSHIP, 10),
    G2_MELEE(90, 270, UnitType.MELEE, 2, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_ii.png", Pool.LEADERSHIP, 1),
    G2_RANGED(90, 270, UnitType.ARCHER, 2, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_ii.png", Pool.LEADERSHIP, 1),
    G2_MOUNTED(180, 540, UnitType.RIDER, 2, Attribute.GUARDSMAN_RIDER, "leadership/mounted_ii.png", Pool.LEADERSHIP, 2),
    G2_ENGINEER(450, 2700, UnitType.CATAPULT, 2, Pool.LEADERSHIP, 10),
    G3_MELEE(160, 480, UnitType.MELEE, 3, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_iii.png", Pool.LEADERSHIP, 1),
    G3_RANGED(160, 480, UnitType.ARCHER, 3, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_iii.png", Pool.LEADERSHIP, 1),
    G3_MOUNTED(320, 960, UnitType.RIDER, 3, Attribute.GUARDSMAN_RIDER, "leadership/mounted_iii.png", Pool.LEADERSHIP, 2),
    G3_ENGINEER(810, 4860, UnitType.CATAPULT, 3, Pool.LEADERSHIP, 10),
    G4_MELEE(290, 870, UnitType.MELEE, 4, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_iv.png", Pool.LEADERSHIP, 1),
    G4_RANGED(290, 870, UnitType.ARCHER, 4, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_iv.png", Pool.LEADERSHIP, 1),
    G4_MOUNTED(580, 1740, UnitType.RIDER, 4, Attribute.GUARDSMAN_RIDER, "leadership/mounted_iv.png", Pool.LEADERSHIP, 2),
    G4_ENGINEER(1460, 8750, UnitType.CATAPULT, 4, Pool.LEADERSHIP, 10),
    G5_RANGED(520, 1560, UnitType.ARCHER, 5, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_v.png", Pool.LEADERSHIP, 1),
    G5_MELEE(520, 1560, UnitType.MELEE, 5, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_v.png", Pool.LEADERSHIP, 1),
    G5_MOUNTED(1050, 3150, UnitType.RIDER, 5, Attribute.GUARDSMAN_RIDER, "leadership/mounted_v.png", Pool.LEADERSHIP, 2),
    G5_GRIFFIN(10000, 30000, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.BEAST, Attribute.FLYING, Attribute.GUARDSMAN), "leadership/griffin_v.png", Pool.LEADERSHIP, 20),

    SWIFT_MARKSMAN(1050, 3150, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.RANGED), "leadership/swift_marksman.png", Pool.AUTHORITY, 1),
    EPIC_MONSTER_HUNTER_V(1050, 3150, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.EPIC_MONSTER_HUNTER), "leadership/epic_monster_hunter_v.png", Pool.AUTHORITY, 1),

    EPIC_MONSTER_HUNTER_VI(2030, 6090, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.EPIC_MONSTER_HUNTER), "leadership/epic_monster_hunter_vi.png", Pool.AUTHORITY, 1),
    ARBALESTER_VI(2030, 6090, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.RANGED), "leadership/arbalester_vi.png", Pool.AUTHORITY, 1),
    LEGIONARY_VI(1900, 5700, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.RANGED), "leadership/legionary_vi.png", Pool.AUTHORITY, 1),
    CHARIOT_VI(3800, 11400, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.MOUNTED), "leadership/chariot_vi.png", Pool.AUTHORITY, 2),
    SPHYNX_VI(18900, 56700, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.BEAST, Attribute.FLYING), "leadership/sphynx.png", Pool.AUTHORITY, 10),

    EPIC_MONSTER_HUNTER_VII(3740, 11220, UnitType.UNKNOWN, 7, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.EPIC_MONSTER_HUNTER), "leadership/epic_monster_hunter_vii.png", Pool.AUTHORITY, 1),


    EMERALD_DRAGON(4500, 13500, UnitType.UNKNOWN, 3, ImmutableSet.of(Attribute.DRAGON, Attribute.FLYING), "leadership/emerald_dragon.png", Pool.DOMINANCE, 7),
    STONE_GARGOYLE(5200, 15600, UnitType.UNKNOWN, 3, ImmutableSet.of(Attribute.FLYING, Attribute.GIANT), "leadership/stone_gargoyle.png", Pool.DOMINANCE, 8),
    WATER_ELEMENTAL(1900, 5700, UnitType.UNKNOWN, 3, ImmutableSet.of(Attribute.ELEMENTAL, Attribute.RANGED), "leadership/water_elemental.png", Pool.DOMINANCE, 3),
    BATTLE_BOAR(3900, 11700, UnitType.UNKNOWN, 3, ImmutableSet.of(Attribute.BEAST, Attribute.MOUNTED), "leadership/battle_boar.png", Pool.DOMINANCE, 6),

    MAGIC_DRAGON(15000, 45000, UnitType.UNKNOWN, 4, ImmutableSet.of(Attribute.DRAGON, Attribute.RANGED), "leadership/magic_dragon.png", Pool.DOMINANCE, 13),
    ICE_PHOENIX(17000, 51000, UnitType.UNKNOWN, 4, ImmutableSet.of(Attribute.ELEMENTAL, Attribute.FLYING), "leadership/ice_phoenix.png", Pool.DOMINANCE, 15),
    MANY_ARMED_GUARDIAN(13000, 39000, UnitType.UNKNOWN, 4, ImmutableSet.of(Attribute.GIANT, Attribute.MELEE), "leadership/many_armed_guardian.png", Pool.DOMINANCE, 11),
    GORGON_MEDUSA(12000, 36000, UnitType.UNKNOWN, 4, ImmutableSet.of(Attribute.BEAST, Attribute.RANGED), "leadership/gorgon_medusa.png", Pool.DOMINANCE, 10),

    DESERT_VANQUISER(42000, 126000, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.DRAGON, Attribute.MOUNTED), "leadership/desert_vanquisher.png", Pool.DOMINANCE, 20),
    FLAMING_CENTAUR(44000, 132000, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.ELEMENTAL, Attribute.MOUNTED), "leadership/flaming_centaur.png", Pool.DOMINANCE, 21),
    ETTIN(48000, 144000, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.GIANT, Attribute.MELEE), "leadership/ettin.png", Pool.DOMINANCE, 23),
    FEARSOME_MANTICORE(46000, 138000, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.BEAST, Attribute.FLYING), "leadership/fearsome_manticore.png", Pool.DOMINANCE, 22);

    private int strength;
    private int health;
    private UnitType type;
    private int tier;
    private Set<Attribute> attributes;
    private BufferedImage icon;
    private int headCount;
    private Pool pool;

    Unit(int strength, int health, UnitType type, int tier, Pool pool, int headCount) {
        this.strength = strength;
        this.health = health;
        this.type = type;
        this.tier = tier;
        this.pool = pool;
        this.headCount = headCount;
    }
    
    Unit(int strength, int health, UnitType type, int tier, Set<Attribute> attributes, String iconPath, Pool pool, int headCount) {
        this.strength = strength;
        this.health = health;
        this.type = type;
        this.tier = tier;
        this.attributes = attributes;
        this.icon = ImageUtil.loadResource(iconPath);
        this.pool = pool;
        this.headCount = headCount;
    }
    
    public boolean wasExcluded(Set<Attribute> exclusions) {
        if (this.attributes == null) {
            return false;
        }
        return exclusions.stream()
                .filter(attribute -> this.attributes.contains(attribute))
                .findAny()
                .isPresent();
    }
}
