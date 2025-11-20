package org.palermo.totalbattle.player.state;

import lombok.Getter;
import lombok.Setter;
import org.palermo.totalbattle.player.Player;

import java.util.Map;

@Getter
@Setter
public class AutomationState {
    
    public Map<Player, AutomationState> map; 
    
    // public Map<String, >
    
}
