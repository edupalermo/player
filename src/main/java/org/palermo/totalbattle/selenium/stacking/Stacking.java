package org.palermo.totalbattle.selenium.stacking;

import com.google.common.collect.ImmutableSet;
import org.palermo.totalbattle.selenium.leadership.model.TroopQuantity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Stacking {

    public static void main(String[] args) {

        // Doomsday and Hellforge
        //palermo(20684, 3);  // Cleopatra  09.10
        //supporter(12562, 3);  // Cleopatra 10.10
        //mightshaper(11620, 3); // Cleopatra 10.10
        //grirana(3575, 3); // Aydae
        //elanin(2450, 3); // Cleopatra


        // Shadow City
        //palermo(68047, 1);  // Cleopatra Dustan Aydae 
        //supporter(39103, 1);  // Cleopatra Dustan Aydae
        //mightshaper(35961, 1); // Cleopatra Dustan Aydae
        //grirana(1750, 1); // Aydae Dustan Bernard

        // Three Captains
        //palermo(42618, 1);  // Aydae Cleopatra Wu Zetian (Fan of the Five Winds, Griffinwing Mask, Valkyrie Diadem)
        //supporter(26607, 1);  // Aydae Cleopatra Ingrid
        //mightshaper(25474, 1); // Aydae Cleopatra Ingrid        
        //grirana(8475, 1); // Aydae Minamoto Leonidas
        //elanin(7850, 1); // Aydae Minamoto Cleopatra

        // RAID
        //palermo(10920, 3);  //Amanitore  11.10
        //supporter(5000, 3);  // Amanitore 
        //mightshaper(5000, 3); // Amanitore
        //grirana(3875, 3); // Aydae
        //elanin(2950, 3); // Aydae

        // Hero
        //palermo(24805, 2); // 12.10
        // supporter(13868, 2); // 12.10
        //mightshaper(11577, 1); 
        //grirana(5450, 2); // 12.10
        //elanin(4700, 2); // 12.10


        //palermo_citadel_15(34344, 770, 3);
        //supporter_citadel_10(14367, 285, 3);
        //mightshaper_citadel_10(12989, 285, 3);

        
        Set<Attribute> set = new TreeSet<>();
        set.add(Attribute.RANGED);
        set.add(Attribute.MELEE);
        //set.add(Attribute.MOUNTED);
        //set.add(Attribute.ELEMENTAL);
        set.add(Attribute.FLYING);
        //set.add(Attribute.DRAGON);

        //custom(Player.PALERMO, set, 2, 14803);
        //custom(Player.PALERMO, set, 2, 4143); // Leonidas
        //custom(Player.PALERMO, set, 2, 22674);
        
        //custom(Player.PALERMO, set, 4, 43446 - (36 * 10)); // Citadel

        custom(Player.PETER_II, set, 2, 8058);
        //custom(Player.PETER_II, set, 3, 11679);
        
        //custom(Player.PETER_II, set, 2, 7599);
        //custom(Player.PETER_II, set, 3, 11169);

    }


    public static void main2(String args[]) {
        Configuration configuration = Configuration.builder()
                .leadership(6961)
                .dominance(1695)
                .authority(3462)
                //.wave(3)
                /*
                .addUnit(Unit.G1_ENGINEER) // Less Important
                //.addUnit(Unit.S1_SWORDSMAN)
                //.addUnit(Unit.S2_SWORDSMAN)
                 */
                .addUnit(Unit.G1_RANGED)
                .addUnit(Unit.G1_MELEE)
                .addUnit(Unit.G1_MOUNTED)
                .addUnit(Unit.G1_ENGINEER)
                .addUnit(Unit.G2_RANGED)
                .addUnit(Unit.G2_MELEE)
                .addUnit(Unit.G2_MOUNTED)
                .addUnit(Unit.G2_ENGINEER)
                .addUnit(Unit.G3_RANGED)
                .addUnit(Unit.G3_MELEE)
                .addUnit(Unit.G3_MOUNTED)
                .addUnit(Unit.G4_RANGED)
                .addUnit(Unit.G4_MELEE)
                .addUnit(Unit.G4_MOUNTED)
                
                .addUnit(Unit.EMERALD_DRAGON)
                .addUnit(Unit.BATTLE_BOAR)
                .addUnit(Unit.WATER_ELEMENTAL)
                .addUnit(Unit.STONE_GARGOYLE)
                
                .addUnit(Unit.EPIC_MONSTER_HUNTER_VI)
                .build();

        int[] troops = configuration.resolve();
        //System.out.println();
        //System.out.println();
        //System.out.println("Damage: " + configuration.damage(troops));

    }




    private static void palermo_citadel_15(int leadership, int discountCatapults, int wave) {
        palermo_citadel_15(leadership - (discountCatapults * 10), wave);
    }


    private static void palermo_citadel_15(int leadership, int wave) {
        Configuration configuration = Configuration.builder()
                .leadership(leadership)
                /*
                .addUnit(Unit.G1_ENGINEER) // Less Important
                //.addUnit(Unit.S1_SWORDSMAN)
                //.addUnit(Unit.S2_SWORDSMAN)
                 */
                //.addUnit(Unit.G1_RANGED)
                //.addUnit(Unit.G1_MELEE)
                .addUnit(Unit.G1_MOUNTED)
                //.addUnit(Unit.G2_RANGED)
                //.addUnit(Unit.G2_MELEE)
                .addUnit(Unit.G2_MOUNTED)
                //.addUnit(Unit.G3_RANGED)
                //.addUnit(Unit.G3_MELEE)
                .addUnit(Unit.G3_MOUNTED)
                //.addUnit(Unit.G4_RANGED)
                //.addUnit(Unit.G4_MELEE)
                .addUnit(Unit.G4_MOUNTED)
                .build();

        int[] troops = configuration.resolve();
        //System.out.println();
        //System.out.println();
        //System.out.println("Damage: " + configuration.damage(troops));

    }

    public enum Player {
        PALERMO, PETER_II, MIGHTSHAPER, GRIRANA, ELANIN;

        public static Player from(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Input cannot be null");
            }
            String key = name.trim().replaceAll("\\s+", "_");
            if (key.isEmpty()) {
                throw new IllegalArgumentException("Input cannot be blank");
            }
            for (Player e : values()) {
                if (e.name().equalsIgnoreCase(key)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("No enum constant matching: " + name);   
        }
    }
    
    private static void custom(Player player, Set<Attribute> exclusions, int tiers, int leadership) {

        List<Unit> units;
        switch (player) {
            case PALERMO:
                units = getPalermoUnits(tiers);
                break;
            case PETER_II:
                units = getPeterIIUnits(tiers);
                break;
            default:
                throw new RuntimeException("Not implemented");
        }

        ConfigurationBuilder builder = Configuration.builder()
                .leadership(leadership)
                .authority(leadership)
                .dominance(leadership);

        for (Unit unit: units) {
            if (!unit.wasExcluded(exclusions)) {
                builder.addUnit(unit);
            }

        }

        int[] troops = builder.build().resolve();
    }
    
    private static List<Unit> getPalermoUnits(int tiers) {
        List<Unit> units = new ArrayList<>();
        if (tiers >= 4) {
            units.add(Unit.G2_RANGED);
            units.add(Unit.G2_MELEE);
            units.add(Unit.G2_MOUNTED);
        }
        if (tiers >= 3) {
            units.add(Unit.G3_RANGED);
            units.add(Unit.G3_MELEE);
            units.add(Unit.G3_MOUNTED);
        }
        if (tiers >= 2) {
            units.add(Unit.G4_RANGED);
            units.add(Unit.G4_MELEE);
            units.add(Unit.G4_MOUNTED);
        }
        if (tiers >= 1) {
            units.add(Unit.G5_RANGED);
            units.add(Unit.G5_MELEE);
            units.add(Unit.G5_MOUNTED);
            units.add(Unit.G5_GRIFFIN);
        }

        units.add(Unit.EMERALD_DRAGON);
        units.add(Unit.WATER_ELEMENTAL);
        units.add(Unit.STONE_GARGOYLE);
        units.add(Unit.BATTLE_BOAR);

        units.add(Unit.MAGIC_DRAGON);
        units.add(Unit.ICE_PHOENIX);
        units.add(Unit.MANY_ARMED_GUARDIAN);
        units.add(Unit.GORGON_MEDUSA);
        
        return units;
    }

    private static List<Unit> getPeterIIUnits(int tiers) {
        List<Unit> units = new ArrayList<>();
        if (tiers >= 3) {
            units.add(Unit.G2_RANGED);
            units.add(Unit.G2_MELEE);
            units.add(Unit.G2_MOUNTED);
        }
        if (tiers >= 2) {
            units.add(Unit.G3_RANGED);
            units.add(Unit.G3_MELEE);
            units.add(Unit.G3_MOUNTED);
        }
        if (tiers >= 1) {
            units.add(Unit.G4_RANGED);
            units.add(Unit.G4_MELEE);
            units.add(Unit.G4_MOUNTED);
        }

        units.add(Unit.EMERALD_DRAGON);
        units.add(Unit.WATER_ELEMENTAL);
        units.add(Unit.STONE_GARGOYLE);
        units.add(Unit.BATTLE_BOAR);

        units.add(Unit.MAGIC_DRAGON);
        units.add(Unit.ICE_PHOENIX);
        units.add(Unit.MANY_ARMED_GUARDIAN);
        units.add(Unit.GORGON_MEDUSA);

        return units;
    }

    private static void palermo(int leadership, int wave) {
        Configuration configuration = Configuration.builder()
                .leadership(leadership)
                .authority(leadership)
                .dominance(leadership)
                /*
                .addUnit(Unit.G1_ENGINEER) // Less Important
                //.addUnit(Unit.S1_SWORDSMAN)
                //.addUnit(Unit.S2_SWORDSMAN)
                 */
                /*
                .addUnit(Unit.G1_RANGED)
                .addUnit(Unit.G1_MELEE)
                .addUnit(Unit.G1_MOUNTED)
                */

                .addUnit(Unit.S3_SWORDSMAN)
                .addUnit(Unit.G3_RANGED)
                .addUnit(Unit.G3_MELEE)
                .addUnit(Unit.G3_MOUNTED)
                .addUnit(Unit.G4_RANGED)
                .addUnit(Unit.G4_MELEE)
                .addUnit(Unit.G4_MOUNTED)
                .addUnit(Unit.G5_RANGED)
                .addUnit(Unit.G5_MELEE)
                .addUnit(Unit.G5_MOUNTED)
                .addUnit(Unit.G5_GRIFFIN)
                .addUnit(Unit.EMERALD_DRAGON)
                .addUnit(Unit.WATER_ELEMENTAL)
                .addUnit(Unit.STONE_GARGOYLE)
                .addUnit(Unit.BATTLE_BOAR)
                
                .addUnit(Unit.MAGIC_DRAGON)
                .addUnit(Unit.ICE_PHOENIX)
                .addUnit(Unit.MANY_ARMED_GUARDIAN)
                .addUnit(Unit.GORGON_MEDUSA)

                .addUnit(Unit.DESERT_VANQUISER)
                .addUnit(Unit.FLAMING_CENTAUR)
                .addUnit(Unit.ETTIN)
                .addUnit(Unit.FEARSOME_MANTICORE)

                .addUnit(Unit.EPIC_MONSTER_HUNTER_VI)
                .build();

        int[] troops = configuration.resolve();
        //System.out.println();
        //System.out.println();
        //System.out.println("Damage: " + configuration.damage(troops));

    }

    private static void palermo_melee(int leadership, int wave) {
        Configuration configuration = Configuration.builder()
                .leadership(leadership)
                .authority(leadership)
                .dominance(leadership)
                //.wave(3)
                /*
                .addUnit(Unit.G1_ENGINEER) // Less Important
                //.addUnit(Unit.S1_SWORDSMAN)
                //.addUnit(Unit.S2_SWORDSMAN)
                 */
                //.addUnit(Unit.G1_MELEE)
                .addUnit(Unit.G2_MELEE)
                .addUnit(Unit.G3_MELEE)
                .addUnit(Unit.G4_MELEE)
                .build();

        int[] troops = configuration.resolve();
        //System.out.println();
        //System.out.println();
        //System.out.println("Damage: " + configuration.damage(troops));

    }
    
    
    private static void mightshaper_citadel_10(int leadership, int discountCatapults, int wave) {
        mightshaper_citadel_10(leadership - (discountCatapults * 10), wave);
    }

    private static void mightshaper_citadel_10(int leadership, int wave) {
        Configuration configuration = Configuration.builder()
                .leadership(leadership)
                /*
                .addUnit(Unit.G1_ENGINEER) // Less Important
                //.addUnit(Unit.S1_SWORDSMAN)
                //.addUnit(Unit.S2_SWORDSMAN)
                 */
                .addUnit(Unit.G1_RANGED)
                //.addUnit(Unit.G1_MELEE)
                //.addUnit(Unit.G1_MOUNTED)
                .addUnit(Unit.G2_RANGED)
                //.addUnit(Unit.G2_MELEE)
                //.addUnit(Unit.G2_MOUNTED)
                .addUnit(Unit.G3_RANGED)
                //.addUnit(Unit.G3_MELEE)
                //.addUnit(Unit.G3_MOUNTED)
                //.addUnit(Unit.G4_RANGED)
                //.addUnit(Unit.G4_MELEE)
                //.addUnit(Unit.G4_MOUNTED)
                .build();

        int[] troops = configuration.resolve();
    }

    private static void supporter_citadel_10(int leadership, int discountCatapults, int wave) {
        supporter_citadel_10(leadership - (discountCatapults * 10), wave);
    }

    private static void supporter_citadel_10(int leadership, int wave) {
        Configuration configuration = Configuration.builder()
                .leadership(leadership)
                //.wave(3)
                /*
                .addUnit(Unit.G1_ENGINEER) // Less Important
                //.addUnit(Unit.S1_SWORDSMAN)
                //.addUnit(Unit.S2_SWORDSMAN)
                 */
                .addUnit(Unit.G1_RANGED)
                //.addUnit(Unit.G1_MELEE)
                //.addUnit(Unit.G1_MOUNTED)
                .addUnit(Unit.G2_RANGED)
                //.addUnit(Unit.G2_MELEE)
                //.addUnit(Unit.G2_MOUNTED)
                .addUnit(Unit.G3_RANGED)
                //.addUnit(Unit.G3_MELEE)
                //.addUnit(Unit.G3_MOUNTED)
                .addUnit(Unit.G4_RANGED)
                //.addUnit(Unit.G4_MELEE)
                //.addUnit(Unit.G4_MOUNTED)
                .build();

        int[] troops = configuration.resolve();
    }


    private static void supporter(int leadership, int wave) {
        Configuration configuration = Configuration.builder()
                .leadership(leadership)
                .dominance(leadership)
                .authority(leadership)
                /*
                .addUnit(Unit.G1_RANGED)
                .addUnit(Unit.G1_MELEE)
                .addUnit(Unit.G1_MOUNTED)
                 */
                
                
                .addUnit(Unit.S2_SWORDSMAN)
                .addUnit(Unit.G2_RANGED)
                .addUnit(Unit.G2_MELEE)
                .addUnit(Unit.G2_MOUNTED)
                .addUnit(Unit.G3_RANGED)
                .addUnit(Unit.G3_MELEE)
                .addUnit(Unit.G3_MOUNTED)
                .addUnit(Unit.G4_RANGED)
                .addUnit(Unit.G4_MELEE)
                .addUnit(Unit.G4_MOUNTED)

                .addUnit(Unit.EMERALD_DRAGON)
                .addUnit(Unit.WATER_ELEMENTAL)
                .addUnit(Unit.STONE_GARGOYLE)
                .addUnit(Unit.BATTLE_BOAR)

                .addUnit(Unit.MAGIC_DRAGON)
                .addUnit(Unit.ICE_PHOENIX)
                .addUnit(Unit.MANY_ARMED_GUARDIAN)
                .addUnit(Unit.GORGON_MEDUSA)
                
                .addUnit(Unit.EPIC_MONSTER_HUNTER_VI)
                .build();

        int[] troops = configuration.resolve();
    }


    private static void mightshaper(int leadership, int wave) {
        Configuration configuration = Configuration.builder()
                .leadership(leadership)
                .authority(leadership)
                .dominance(leadership)
                /*
                .addUnit(Unit.G1_ENGINEER) // Less Important
                //.addUnit(Unit.S1_SWORDSMAN)
                //.addUnit(Unit.S2_SWORDSMAN)
                 */
                
                
                .addUnit(Unit.S2_SWORDSMAN)
                .addUnit(Unit.G2_RANGED)
                .addUnit(Unit.G2_MELEE)
                .addUnit(Unit.G2_MOUNTED)
                .addUnit(Unit.G3_RANGED)
                .addUnit(Unit.G3_MELEE)
                .addUnit(Unit.G3_MOUNTED)
                .addUnit(Unit.G4_RANGED)
                .addUnit(Unit.G4_MELEE)
                .addUnit(Unit.G4_MOUNTED)

                .addUnit(Unit.EMERALD_DRAGON)
                .addUnit(Unit.WATER_ELEMENTAL)
                .addUnit(Unit.STONE_GARGOYLE)
                .addUnit(Unit.BATTLE_BOAR)

                .addUnit(Unit.MAGIC_DRAGON)
                .addUnit(Unit.ICE_PHOENIX)
                .addUnit(Unit.MANY_ARMED_GUARDIAN)
                .addUnit(Unit.GORGON_MEDUSA)
                
                .addUnit(Unit.EPIC_MONSTER_HUNTER_VI)
                .build();

        int[] troops = configuration.resolve();
        //System.out.println();
        //System.out.println();
        //System.out.println("Damage: " + configuration.damage(troops));

    }

    private static void grirana(int leadership, int wave) {
        Configuration configuration = Configuration.builder()
                .leadership(leadership)
                .authority(leadership)
                .dominance(leadership)
                .addUnit(Unit.S1_SWORDSMAN)
                .addUnit(Unit.G1_RANGED)
                .addUnit(Unit.G1_MELEE)
                .addUnit(Unit.G1_MOUNTED)
                .addUnit(Unit.G2_RANGED)
                .addUnit(Unit.G2_MELEE)
                .addUnit(Unit.G2_MOUNTED)
                .addUnit(Unit.G3_RANGED)
                .addUnit(Unit.G3_MELEE)
                .addUnit(Unit.G3_MOUNTED)
                .addUnit(Unit.SWIFT_MARKSMAN)
                .addUnit(Unit.EPIC_MONSTER_HUNTER_V)
                .build();

        int[] troops = configuration.resolve();
        //System.out.println();
        //System.out.println();
        //System.out.println("Damage: " + configuration.damage(troops));

    }

    private static void elanin(int leadership, int wave) {
        Configuration configuration = Configuration.builder()
                .leadership(leadership)
                .authority(leadership)
                .dominance(leadership)
                .addUnit(Unit.S1_SWORDSMAN)
                .addUnit(Unit.G1_RANGED)
                .addUnit(Unit.G1_MELEE)
                .addUnit(Unit.G1_MOUNTED)
                .addUnit(Unit.G2_RANGED)
                .addUnit(Unit.G2_MELEE)
                .addUnit(Unit.G2_MOUNTED)
                .addUnit(Unit.G3_RANGED)
                .addUnit(Unit.SWIFT_MARKSMAN)
                .addUnit(Unit.EPIC_MONSTER_HUNTER_V)
                .build();

        int[] troops = configuration.resolve();
        //System.out.println();
        //System.out.println();
        //System.out.println("Damage: " + configuration.damage(troops));

    }
    
    
}
