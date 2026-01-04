package org.palermo.totalbattle.internalservice;

import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.state.AutomationState;
import org.palermo.totalbattle.player.state.PlayerState;

import java.time.LocalDateTime;

public class LockService {

    private SharedData sharedData = SharedData.INSTANCE;

    public void lock(PlayerName playerName, Scenario scenario, LocalDateTime until) {
        AutomationState automationState = sharedData.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates()
                .computeIfAbsent(playerName, (it) -> new PlayerState());
        playerState.getLocks().put(scenario, until);
        sharedData.saveAutomationState();;
    }

    public boolean isLocked(PlayerName playerName, Scenario scenario) {
        AutomationState automationState = sharedData.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates()
                .computeIfAbsent(playerName, (it) -> new PlayerState());
        LocalDateTime until = playerState.getLocks().get(scenario);
        if (until == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(until);
    }
    
    public boolean isFree(PlayerName playerName, Scenario scenario) {
        return !isLocked(playerName, scenario);
    }

    public void clear(PlayerName playerName, Scenario scenario) {
        AutomationState automationState = sharedData.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates()
                .computeIfAbsent(playerName, (it) -> new PlayerState());
        playerState.getLocks().remove(scenario);
        sharedData.saveAutomationState();;
    }
}
