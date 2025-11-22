package org.palermo.totalbattle.internalservice;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.state.AutomationState;
import org.palermo.totalbattle.player.state.PlayerState;

import java.time.LocalDateTime;

public class LockService {

    private SharedData sharedData = SharedData.INSTANCE;

    public void lock(Player player, Scenario scenario, LocalDateTime until) {
        AutomationState automationState = sharedData.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates()
                .computeIfAbsent(player, (it) -> new PlayerState());
        playerState.getLocks().put(scenario, until);
        sharedData.saveAutomationState();;
    }
    
    public boolean isLocked(Player player, Scenario scenario) {
        AutomationState automationState = sharedData.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates()
                .computeIfAbsent(player, (it) -> new PlayerState());
        LocalDateTime until = playerState.getLocks().get(scenario);
        if (until == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(until);
    }

    public void clear(Player player, Scenario scenario) {
        AutomationState automationState = sharedData.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates()
                .computeIfAbsent(player, (it) -> new PlayerState());
        playerState.getLocks().remove(scenario);
        sharedData.saveAutomationState();;
    }
}
