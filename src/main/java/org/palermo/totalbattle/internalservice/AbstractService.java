package org.palermo.totalbattle.internalservice;

import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.state.AutomationState;
import org.palermo.totalbattle.player.state.PlayerState;

public abstract class AbstractService {

    protected SharedData sharedData = SharedData.INSTANCE;

    protected PlayerState getPlayerState(PlayerName playerName) {
        return sharedData.getAutomationState()
                .getPlayerStates()
                .computeIfAbsent((playerName), k -> new PlayerState());
    }

    public void saveGameState() {
        sharedData.saveAutomationState();
    }         
    
    protected AutomationState getAutomationState() {
        return sharedData.getAutomationState();
    }
}
