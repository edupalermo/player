package org.palermo.totalbattle.player;

import lombok.Getter;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Unit;

import java.util.Optional;

@Getter
public enum PlayerName {

    PALERMO,
    PETER,
    MIGHTSHAPER,
    GRIRANA,
    ELANIN,
    LORVEN;

    public static PlayerName getPlayerByName(String name) {
        for (PlayerName player : PlayerName.values()) {
            if (player.name().equalsIgnoreCase(name)) {
                return player;
            }
        }
        throw new RuntimeException("Cannot find player with name " + name);
    }    
}
