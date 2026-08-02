package org.palermo.totalbattle.selenium.stacking;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import org.palermo.totalbattle.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.Set;

@Getter
public enum Unit {

    S1_SPY(25, 75, UnitType.SPY, 1, ImmutableSet.of(Attribute.HUMAN, Attribute.SCOUT, Attribute.SPECIALIST), "leadership/swordsman_i.png", Pool.LEADERSHIP, 5),
    S2_SPY(45, 135, UnitType.SPY, 2, ImmutableSet.of(Attribute.HUMAN, Attribute.SCOUT, Attribute.SPECIALIST), "leadership/swordsman_i.png", Pool.LEADERSHIP, 5),
    S3_SPY(80, 240, UnitType.SPY, 3, ImmutableSet.of(Attribute.HUMAN, Attribute.SCOUT, Attribute.SPECIALIST), "leadership/swordsman_i.png", Pool.LEADERSHIP, 5),
    S4_SPY(150, 450, UnitType.SPY, 4, ImmutableSet.of(Attribute.HUMAN, Attribute.SCOUT, Attribute.SPECIALIST), "leadership/swordsman_i.png", Pool.LEADERSHIP, 5),
    S5_SPY(260, 780, UnitType.SPY, 5, ImmutableSet.of(Attribute.HUMAN, Attribute.SCOUT, Attribute.SPECIALIST), "leadership/swordsman_i.png", Pool.LEADERSHIP, 5),
    S6_SPY(470, 1410, UnitType.SPY, 6, ImmutableSet.of(Attribute.HUMAN, Attribute.SCOUT, Attribute.SPECIALIST), "leadership/swordsman_i.png", Pool.LEADERSHIP, 5),
    
    S1_SWORDSMAN(50, 150, UnitType.MELEE, 1, Attribute.GUARDSMAN_SPEARMAN, "leadership/swordsman_i.png", Pool.LEADERSHIP, 1),
    S2_SWORDSMAN(90, 270, UnitType.MELEE, 2, Attribute.GUARDSMAN_SPEARMAN, "leadership/swordsman_ii.png", Pool.LEADERSHIP, 1),
    S3_SWORDSMAN(160, 480, UnitType.MELEE, 3, Attribute.GUARDSMAN_SPEARMAN, "leadership/swordsman_iii.png", Pool.LEADERSHIP, 1),
    S4_SWORDSMAN(290, 870, UnitType.MELEE, 4, Attribute.GUARDSMAN_SPEARMAN, "leadership/swordsman_iv.png", Pool.LEADERSHIP, 1),
    
    S5_SWORDSMAN(520, 1560, UnitType.MELEE, 5, ImmutableSet.of(Attribute.HUMAN, Attribute.MELEE, Attribute.SPECIALIST), "leadership/swordsman_v.png", Pool.LEADERSHIP, 1),
    S5_VULTURE(520, 1560, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.HUMAN, Attribute.FLYING, Attribute.SPECIALIST), "leadership/vulture_v.png", Pool.LEADERSHIP, 1),
    S5_DEADSHOT(520, 1560, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.HUMAN, Attribute.RANGED, Attribute.SPECIALIST), "leadership/deadshot_v.png", Pool.LEADERSHIP, 1),
    S5_LION_RIDER(1050, 3150, UnitType.RIDER, 5, ImmutableSet.of(Attribute.HUMAN, Attribute.MOUNTED, Attribute.SPECIALIST), "leadership/lion_rider_v.png", Pool.LEADERSHIP, 2),

    S6_MELEE(940, 2820, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.HUMAN, Attribute.MELEE, Attribute.SPECIALIST), "leadership/s_melee_vi.png", Pool.LEADERSHIP, 1),
    S6_RANGED(940, 2820, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.HUMAN, Attribute.RANGED, Attribute.SPECIALIST), "leadership/deadshot_vi.png", Pool.LEADERSHIP, 1),
    S6_FLYING(940, 2820, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.HUMAN, Attribute.FLYING, Attribute.SPECIALIST), "leadership/vulture_vi.png", Pool.LEADERSHIP, 1),
    S6_MOUNTED(1900, 5700, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.HUMAN, Attribute.MOUNTED, Attribute.SPECIALIST), "leadership/s_mounted_vi.png", Pool.LEADERSHIP, 2),

    G1_MELEE(50, 150, UnitType.MELEE, 1, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_i.png", Pool.LEADERSHIP, 1),
    G1_RANGED(50, 150, UnitType.ARCHER, 1, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_i.png", Pool.LEADERSHIP, 1),
    G1_MOUNTED(100, 300, UnitType.RIDER, 1, Attribute.GUARDSMAN_RIDER, "leadership/mounted_i.png", Pool.LEADERSHIP, 2),
    G2_MELEE(90, 270, UnitType.MELEE, 2, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_ii.png", Pool.LEADERSHIP, 1),
    G2_RANGED(90, 270, UnitType.ARCHER, 2, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_ii.png", Pool.LEADERSHIP, 1),
    G2_MOUNTED(180, 540, UnitType.RIDER, 2, Attribute.GUARDSMAN_RIDER, "leadership/mounted_ii.png", Pool.LEADERSHIP, 2),
    G3_MELEE(160, 480, UnitType.MELEE, 3, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_iii.png", Pool.LEADERSHIP, 1),
    G3_RANGED(160, 480, UnitType.ARCHER, 3, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_iii.png", Pool.LEADERSHIP, 1),
    G3_MOUNTED(320, 960, UnitType.RIDER, 3, Attribute.GUARDSMAN_RIDER, "leadership/mounted_iii.png", Pool.LEADERSHIP, 2),
    G4_MELEE(290, 870, UnitType.MELEE, 4, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_iv.png", Pool.LEADERSHIP, 1),
    G4_RANGED(290, 870, UnitType.ARCHER, 4, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_iv.png", Pool.LEADERSHIP, 1),
    G4_MOUNTED(580, 1740, UnitType.RIDER, 4, Attribute.GUARDSMAN_RIDER, "leadership/mounted_iv.png", Pool.LEADERSHIP, 2),

    G5_RANGED(520, 1560, UnitType.ARCHER, 5, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_v.png", Pool.LEADERSHIP, 1),
    G5_MELEE(520, 1560, UnitType.MELEE, 5, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_v.png", Pool.LEADERSHIP, 1),
    G5_MOUNTED(1050, 3150, UnitType.RIDER, 5, Attribute.GUARDSMAN_RIDER, "leadership/mounted_v.png", Pool.LEADERSHIP, 2),
    G5_GRIFFIN(10000, 30000, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.BEAST, Attribute.FLYING, Attribute.GUARDSMAN), "leadership/griffin_v.png", Pool.LEADERSHIP, 20),

    G6_RANGED(940, 2820, UnitType.ARCHER, 6, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_vi.png", Pool.LEADERSHIP, 1),
    G6_MELEE(940, 2820, UnitType.MELEE, 6, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_vi.png", Pool.LEADERSHIP, 1),
    G6_MOUNTED(1900, 5700, UnitType.RIDER, 6, Attribute.GUARDSMAN_RIDER, "leadership/mounted_vi.png", Pool.LEADERSHIP, 2),
    G6_GRIFFIN(19000, 57000, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.BEAST, Attribute.FLYING, Attribute.GUARDSMAN), "leadership/griffin_vi.png", Pool.LEADERSHIP, 20),

    G7_RANGED(1700, 5100, UnitType.ARCHER, 7, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_vii.png", Pool.LEADERSHIP, 1),
    G7_MELEE(1700, 5100, UnitType.MELEE, 7, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_vii.png", Pool.LEADERSHIP, 1),
    G7_MOUNTED(3400, 10200, UnitType.RIDER, 7, Attribute.GUARDSMAN_RIDER, "leadership/mounted_vii.png", Pool.LEADERSHIP, 2),
    G7_GRIFFIN(34000, 102000, UnitType.UNKNOWN, 7, ImmutableSet.of(Attribute.BEAST, Attribute.FLYING, Attribute.GUARDSMAN), "leadership/griffin_vii.png", Pool.LEADERSHIP, 20),

    G8_RANGED(3060, 9180, UnitType.ARCHER, 8, Attribute.GUARDSMAN_ARCHER, "leadership/ranged_viii.png", Pool.LEADERSHIP, 1),
    G8_MELEE(3060, 9180, UnitType.MELEE, 8, Attribute.GUARDSMAN_SPEARMAN, "leadership/melee_viii.png", Pool.LEADERSHIP, 1),
    G8_MOUNTED(6120, 18360, UnitType.RIDER, 8, Attribute.GUARDSMAN_RIDER, "leadership/mounted_viii.png", Pool.LEADERSHIP, 2),
    G8_COURAX(61200, 183600, UnitType.UNKNOWN, 8, ImmutableSet.of(Attribute.FLYING, Attribute.GUARDSMAN, Attribute.HUMAN), "leadership/courax_viii.png", Pool.LEADERSHIP, 20),
    
    EC1_ENGINEER(250, 1500, UnitType.CATAPULT, 1, ImmutableSet.of(Attribute.ENGINEER_CORPS, Attribute.HUMAN, Attribute.SIEGE_ENGINE), "leadership/engineer_i.png", Pool.LEADERSHIP, 10),
    EC2_ENGINEER(450, 2700, UnitType.CATAPULT, 2, ImmutableSet.of(Attribute.ENGINEER_CORPS, Attribute.HUMAN, Attribute.SIEGE_ENGINE), "leadership/engineer_ii.png", Pool.LEADERSHIP, 10),
    EC3_ENGINEER(810, 4860, UnitType.CATAPULT, 3, ImmutableSet.of(Attribute.ENGINEER_CORPS, Attribute.HUMAN, Attribute.SIEGE_ENGINE), "leadership/engineer_iii.png", Pool.LEADERSHIP, 10),
    EC4_ENGINEER(1460, 8750, UnitType.CATAPULT, 4, ImmutableSet.of(Attribute.ENGINEER_CORPS, Attribute.HUMAN, Attribute.SIEGE_ENGINE), "leadership/engineer_iv.png", Pool.LEADERSHIP, 10),
    EC5_ENGINEER(2630, 15800, UnitType.CATAPULT, 5, ImmutableSet.of(Attribute.ENGINEER_CORPS, Attribute.HUMAN, Attribute.SIEGE_ENGINE), "leadership/engineer_v.png", Pool.LEADERSHIP, 10),
    EC6_ENGINEER(4730, 28400, UnitType.CATAPULT, 6, ImmutableSet.of(Attribute.ENGINEER_CORPS, Attribute.HUMAN, Attribute.SIEGE_ENGINE), "leadership/engineer_vi.png", Pool.LEADERSHIP, 10),
    EC7_ENGINEER(8500, 51000, UnitType.CATAPULT, 7, ImmutableSet.of(Attribute.ENGINEER_CORPS, Attribute.HUMAN, Attribute.SIEGE_ENGINE), "leadership/engineer_vii.png", Pool.LEADERSHIP, 10),

    SWIFT_MARKSMAN(1050, 3150, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.RANGED), "leadership/swift_marksman.png", Pool.AUTHORITY, 1),
    
    EPIC_MONSTER_HUNTER_V(1050, 3150, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.EPIC_MONSTER_HUNTER), "leadership/epic_monster_hunter_v.png", Pool.AUTHORITY, 1),
    EPIC_MONSTER_HUNTER_VI(2030, 6090, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.EPIC_MONSTER_HUNTER), "leadership/epic_monster_hunter_vi.png", Pool.AUTHORITY, 1),
    EPIC_MONSTER_HUNTER_VII(3740, 11220, UnitType.UNKNOWN, 7, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.EPIC_MONSTER_HUNTER), "leadership/epic_monster_hunter_vii.png", Pool.AUTHORITY, 1),
    EPIC_MONSTER_HUNTER_IX(25000, 75000, UnitType.UNKNOWN, 9, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.EPIC_MONSTER_HUNTER), "leadership/epic_monster_hunter_ix.png", Pool.AUTHORITY, 1),
    
    ARBALESTER_VI(2030, 6090, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.RANGED), "leadership/arbalester_vi.png", Pool.AUTHORITY, 1),
    LEGIONARY_VI(1900, 5700, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.RANGED), "leadership/legionary_vi.png", Pool.AUTHORITY, 1),
    CHARIOT_VI(3800, 11400, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.HUMAN, Attribute.MOUNTED), "leadership/chariot_vi.png", Pool.AUTHORITY, 2),
    SPHYNX_VI(18900, 56700, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GUARDSMAN, Attribute.BEAST, Attribute.FLYING), "leadership/sphynx.png", Pool.AUTHORITY, 10),


    DRAGON_III(4500, 13500, UnitType.UNKNOWN, 3, ImmutableSet.of(Attribute.DRAGON, Attribute.FLYING), "leadership/emerald_dragon.png", Pool.DOMINANCE, 7),
    DRAGON_IV(15000, 45000, UnitType.UNKNOWN, 4, ImmutableSet.of(Attribute.DRAGON, Attribute.RANGED), "leadership/magic_dragon.png", Pool.DOMINANCE, 13),
    DRAGON_V(42000, 126000, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.DRAGON, Attribute.MOUNTED), "leadership/desert_vanquisher.png", Pool.DOMINANCE, 20),
    DRAGON_VI(120000, 360000, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.DRAGON, Attribute.MELEE), "leadership/dragon_vi.png", Pool.DOMINANCE, 33),
    DRAGON_VII(300000, 900000, UnitType.UNKNOWN, 7, ImmutableSet.of(Attribute.DRAGON, Attribute.FLYING), "leadership/dragon_vii.png", Pool.DOMINANCE, 44),
    DRAGON_VIII(650_000, 1_950_000, UnitType.UNKNOWN, 8, ImmutableSet.of(Attribute.DRAGON, Attribute.MOUNTED), "leadership/dragon_viii.png", Pool.DOMINANCE, 53),

    ELEMENTAL_III(1900, 5700, UnitType.UNKNOWN, 3, ImmutableSet.of(Attribute.ELEMENTAL, Attribute.RANGED), "leadership/water_elemental.png", Pool.DOMINANCE, 3),
    ELEMENTAL_IV(17000, 51000, UnitType.UNKNOWN, 4, ImmutableSet.of(Attribute.ELEMENTAL, Attribute.FLYING), "leadership/ice_phoenix.png", Pool.DOMINANCE, 15),
    ELEMENTAL_V(44000, 132000, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.ELEMENTAL, Attribute.MOUNTED), "leadership/flaming_centaur.png", Pool.DOMINANCE, 21),
    ELEMENTAL_VI(130000, 390000, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.ELEMENTAL, Attribute.MELEE), "leadership/elemental_vi.png", Pool.DOMINANCE, 35),
    ELEMENTAL_VII(310000, 930000, UnitType.UNKNOWN, 7, ImmutableSet.of(Attribute.ELEMENTAL, Attribute.MELEE), "leadership/elemental_vii.png", Pool.DOMINANCE, 45),
    ELEMENTAL_VIII(660_000, 1_980_000, UnitType.UNKNOWN, 8, ImmutableSet.of(Attribute.ELEMENTAL, Attribute.FLYING), "leadership/elemental_viii.png", Pool.DOMINANCE, 54),
    
    GIANT_III(5200, 15600, UnitType.UNKNOWN, 3, ImmutableSet.of(Attribute.FLYING, Attribute.GIANT), "leadership/stone_gargoyle.png", Pool.DOMINANCE, 8),
    GIANT_IV(13000, 39000, UnitType.UNKNOWN, 4, ImmutableSet.of(Attribute.GIANT, Attribute.MELEE), "leadership/many_armed_guardian.png", Pool.DOMINANCE, 11),
    GIANT_V(48000, 144000, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.GIANT, Attribute.MELEE), "leadership/ettin.png", Pool.DOMINANCE, 23),
    GIANT_VI(110000, 330000, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.GIANT, Attribute.MOUNTED), "leadership/giant_vi.png", Pool.DOMINANCE, 30),
    GIANT_VII(290000, 870000, UnitType.UNKNOWN, 7, ImmutableSet.of(Attribute.GIANT, Attribute.RANGED), "leadership/giant_vii.png", Pool.DOMINANCE, 43),
    GIANT_VIII(670_000, 2_010_000, UnitType.UNKNOWN, 8, ImmutableSet.of(Attribute.GIANT, Attribute.MELEE), "leadership/giant_viii.png", Pool.DOMINANCE, 55),
    
    BEAST_III(3900, 11700, UnitType.UNKNOWN, 3, ImmutableSet.of(Attribute.BEAST, Attribute.MOUNTED), "leadership/battle_boar.png", Pool.DOMINANCE, 6),
    BEAST_IV(12000, 36000, UnitType.UNKNOWN, 4, ImmutableSet.of(Attribute.BEAST, Attribute.RANGED), "leadership/gorgon_medusa.png", Pool.DOMINANCE, 10),
    BEAST_V(46000, 138000, UnitType.UNKNOWN, 5, ImmutableSet.of(Attribute.BEAST, Attribute.FLYING), "leadership/fearsome_manticore.png", Pool.DOMINANCE, 22),
    BEAST_VI(130000, 390000, UnitType.UNKNOWN, 6, ImmutableSet.of(Attribute.BEAST, Attribute.MELEE), "leadership/beast_vi.png", Pool.DOMINANCE, 34),
    BEAST_VII(280000, 840000, UnitType.UNKNOWN, 7, ImmutableSet.of(Attribute.BEAST, Attribute.MOUNTED), "leadership/beast_vii.png", Pool.DOMINANCE, 41),
    BEAST_VIII(640_000, 1_920_000, UnitType.UNKNOWN, 7, ImmutableSet.of(Attribute.BEAST, Attribute.RANGED), "leadership/beast_viii.png", Pool.DOMINANCE, 52);

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
