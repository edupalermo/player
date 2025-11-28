package org.palermo.totalbattle.player.state;

import lombok.Getter;
import lombok.Setter;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.message.Message;
import org.palermo.totalbattle.player.state.location.Location;

import java.util.*;

@Getter
@Setter
public class AutomationState {
    
    public Map<Player, PlayerState> playerStates;

    public List<Location> locations = new ArrayList<>();
    
    public Map<String, String> properties = new HashMap<>();
    
    private Set<Message> messages = new HashSet<>();

    public Map<Player, PlayerState> getPlayerStates() {
        if (playerStates == null) {
            this.playerStates = new HashMap<>();
        }
        return playerStates;
    }
}
