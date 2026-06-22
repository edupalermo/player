package org.palermo.totalbattle.selenium.leadership;

import lombok.Getter;
import org.palermo.totalbattle.selenium.leadership.model.EnemyRarity;
import org.palermo.totalbattle.selenium.leadership.model.EnemyType;
import org.palermo.totalbattle.selenium.leadership.model.Exclusion;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ExclusionDatabase {

    private static Map<ExclusionKey, Exclusion> exclusions = new HashMap<>();

    static {
        //RARE
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.BARBARIAN, 12), Exclusion.builder().melee(true).elemental(true).ranged(true).siege(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.BARBARIAN, 15), Exclusion.builder().melee(true).elemental(true).flying(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.BARBARIAN, 17), Exclusion.builder().melee(true).elemental(true).flying(true).ranged(true).siege(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.BARBARIAN, 22), Exclusion.builder().melee(true).elemental(true).ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.BARBARIAN, 23), Exclusion.builder().melee(true).elemental(true).flying(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.BARBARIAN, 25), Exclusion.builder().ranged(true).siege(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.BARBARIAN, 26), Exclusion.builder().melee(true).beast(true).flying(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.BARBARIAN, 29), Exclusion.builder().ranged(true).mounted(true).melee(true).flying(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.BARBARIAN, 30), Exclusion.builder().melee(true).beast(true).elemental(true).ranged(true).siege(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.CURSED, 12), Exclusion.builder().melee(true).elemental(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.CURSED, 22), Exclusion.builder().melee(true).elemental(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.CURSED, 23), Exclusion.builder().melee(true).elemental(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.CURSED, 26), Exclusion.builder().ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.CURSED, 29), Exclusion.builder().mounted(true).beast(true).ranged(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.CURSED, 30), Exclusion.builder().ranged(true).melee(true).elemental(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.ELVES, 22), Exclusion.builder().melee(true).dragon(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.ELVES, 23), Exclusion.builder().melee(true).dragon(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.ELVES, 26), Exclusion.builder().mounted(true).elemental(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.ELVES, 27), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.ELVES, 29), Exclusion.builder().ranged(true).siege(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.ELVES, 30), Exclusion.builder().mounted(true).elemental(true).melee(true).dragon(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.INFERNO, 15), Exclusion.builder().ranged(true).elemental(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.INFERNO, 17), Exclusion.builder().ranged(true).elemental(true).mounted(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.INFERNO, 22), Exclusion.builder().ranged(true).elemental(true).melee(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.INFERNO, 23), Exclusion.builder().ranged(true).elemental(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.INFERNO, 26), Exclusion.builder().ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.INFERNO, 29), Exclusion.builder().melee(true).dragon(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.INFERNO, 30), Exclusion.builder().ranged(true).elemental(true).melee(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.UNDEAD, 22), Exclusion.builder().ranged(true).melee(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.UNDEAD, 23), Exclusion.builder().ranged(true).melee(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.UNDEAD, 25), Exclusion.builder().mounted(true).elemental(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.UNDEAD, 26), Exclusion.builder().mounted(true).elemental(true).ranged(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.UNDEAD, 27), Exclusion.builder().mounted(true).elemental(true).ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.UNDEAD, 29), Exclusion.builder().mounted(true).elemental(true).ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.UNDEAD, 30), Exclusion.builder().mounted(true).elemental(true).ranged(true).melee(true).build());
        
        // COMMON
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 13), Exclusion.builder().melee(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 15), Exclusion.builder().mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 19), Exclusion.builder().mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 22), Exclusion.builder().mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 23), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 24), Exclusion.builder().ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 25), Exclusion.builder().melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 26), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 27), Exclusion.builder().melee(true).ranged(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 28), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 29), Exclusion.builder().melee(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 30), Exclusion.builder().ranged(true).mounted(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.UNDEAD, 16), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.UNDEAD, 22), Exclusion.builder().ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.UNDEAD, 23), Exclusion.builder().ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.UNDEAD, 26), Exclusion.builder().mounted(true).ranged(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.UNDEAD, 29), Exclusion.builder().mounted(true).ranged(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.UNDEAD, 30), Exclusion.builder().mounted(true).ranged(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 12), Exclusion.builder().ranged(true).siege(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 15), Exclusion.builder().melee(true).flying(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 17), Exclusion.builder().melee(true).flying(true).ranged(true).siege(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 22), Exclusion.builder().melee(true).flying(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 23), Exclusion.builder().melee(true).flying(true).ranged(true).siege(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 26), Exclusion.builder().mounted(true).melee(true).flying(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 27), Exclusion.builder().mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 29), Exclusion.builder().mounted(true).melee(true).flying(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 30), Exclusion.builder().mounted(true).melee(true).flying(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.ELVES, 14), Exclusion.builder().melee(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.ELVES, 22), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.ELVES, 23), Exclusion.builder().ranged(true).siege(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.ELVES, 26), Exclusion.builder().ranged(true).siege(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.ELVES, 27), Exclusion.builder().ranged(true).siege(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.ELVES, 29), Exclusion.builder().ranged(true).siege(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.ELVES, 30), Exclusion.builder().ranged(true).siege(true).melee(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.CURSED, 12), Exclusion.builder().ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.CURSED, 14), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.CURSED, 22), Exclusion.builder().ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.CURSED, 23), Exclusion.builder().mounted(true).ranged(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.CURSED, 26), Exclusion.builder().ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.CURSED, 29), Exclusion.builder().ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.CURSED, 30), Exclusion.builder().ranged(true).melee(true).build());
        
        
        // Citadel
        exclusions.put(ExclusionKey.of(EnemyRarity.CITADEL, EnemyType.ELVES, 20), Exclusion.builder().mounted(true).giant(true).ranged(true).dragon(true).elemental(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.CITADEL, EnemyType.ELVES, 25), Exclusion.builder().mounted(true).giant(true).ranged(true).dragon(true).elemental(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.CITADEL, EnemyType.ELVES, 30), Exclusion.builder().mounted(true).giant(true).ranged(true).dragon(true).elemental(true).melee(true).build());
        
        exclusions.put(ExclusionKey.of(EnemyRarity.CITADEL, EnemyType.CURSED, 25), Exclusion.builder().giant(true).mounted(true).ranged(true).dragon(true).beast(true).build());
    }
    
    public static Optional<Exclusion> resolve(EnemyRarity rarity, EnemyType type, int level) {
        return Optional.ofNullable(exclusions.get(ExclusionKey.of(rarity, type, level)));
    }



    @Getter
    public static class ExclusionKey {

        private final EnemyRarity rarity;
        private final EnemyType type;
        private final int level;

        public ExclusionKey(EnemyRarity rarity, EnemyType type, int level) {
            this.rarity = rarity;
            this.type = type;
            this.level = level;
        }

        public static ExclusionKey of(EnemyRarity rarity, EnemyType type, int level) {
            return new ExclusionKey(rarity, type, level);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExclusionKey)) return false;
            ExclusionKey key = (ExclusionKey) o;
            return level == key.level &&
                    rarity == key.rarity &&
                    type == key.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rarity, type, level);
        }
    }
}
