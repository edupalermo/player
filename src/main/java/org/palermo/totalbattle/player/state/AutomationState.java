package org.palermo.totalbattle.player.state;

import lombok.Getter;
import lombok.Setter;
import org.palermo.totalbattle.player.Player;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class AutomationState {
    
    public Map<Player, PlayerState> playerStates;

    public Map<Player, PlayerState> getPlayerStates() {
        if (playerStates == null) {
            this.playerStates = new HashMap<>();
        }
        return playerStates;
    }
}
