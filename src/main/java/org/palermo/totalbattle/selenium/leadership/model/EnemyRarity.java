package org.palermo.totalbattle.selenium.leadership.model;

public enum EnemyRarity {
    COMMON, RARE;

    public static EnemyRarity fromString(String value) {
        if (value == null) {
            return null;
        }

        for (EnemyRarity type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown EnemyRarity: " + value);
    }
}
