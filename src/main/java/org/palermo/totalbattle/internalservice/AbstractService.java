package org.palermo.totalbattle.internalservice;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.state.AutomationState;
import org.palermo.totalbattle.player.state.PlayerState;

public abstract class AbstractService {

    protected SharedData sharedData = SharedData.INSTANCE;

    protected PlayerState getPlayerState(Player player) {
        return sharedData.getAutomationState()
                .getPlayerStates()
                .computeIfAbsent((player), k -> new PlayerState());
    }

    protected void saveGameState() {
        sharedData.saveAutomationState();
    }         
    
    protected AutomationState getAutomationState() {
        return sharedData.getAutomationState();
    }
}
