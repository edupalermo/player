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
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.INFERNO, 22), Exclusion.builder().ranged(true).elemental(true).melee(true).mounted(true).build());
        
        exclusions.put(ExclusionKey.of(EnemyRarity.RARE, EnemyType.ELVES, 22), Exclusion.builder().melee(true).dragon(true).mounted(true).build());
        
        
        
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 19), Exclusion.builder().mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 23), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 24), Exclusion.builder().ranged(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 25), Exclusion.builder().melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 26), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 28), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.INFERNO, 29), Exclusion.builder().melee(true).mounted(true).build());
        
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.UNDEAD, 22), Exclusion.builder().ranged(true).mounted(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 22), Exclusion.builder().melee(true).flying(true).mounted(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 23), Exclusion.builder().melee(true).flying(true).ranged(true).siege(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.BARBARIAN, 26), Exclusion.builder().mounted(true).melee(true).flying(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.ELVES, 22), Exclusion.builder().ranged(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.ELVES, 26), Exclusion.builder().ranged(true).siege(true).melee(true).build());
        
        exclusions.put(ExclusionKey.of(EnemyRarity.COMMON, EnemyType.CURSED, 22), Exclusion.builder().ranged(true).mounted(true).build());

        exclusions.put(ExclusionKey.of(EnemyRarity.CITADEL, EnemyType.ELVES, 25), Exclusion.builder().mounted(true).giant(true).ranged(true).dragon(true).elemental(true).melee(true).build());
        exclusions.put(ExclusionKey.of(EnemyRarity.CITADEL, EnemyType.ELVES, 30), Exclusion.builder().mounted(true).giant(true).ranged(true).dragon(true).elemental(true).melee(true).build());
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
