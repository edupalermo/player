package org.palermo.totalbattle.selenium.leadership.model;

public enum EnemyType {

    BARBARIAN, 
    INFERNO, 
    UNDEAD, 
    ELVES,
    CURSED;

    public static EnemyType fromString(String value) {
        if (value == null) {
            return null;
        }

        for (EnemyType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown EnemyType: " + value);
    }
}
