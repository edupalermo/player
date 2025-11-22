package org.palermo.totalbattle.internalservice;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.selenium.stacking.Captain;

import java.util.ArrayList;
import java.util.List;

public class PlayerStateService extends AbstractService {

    private SharedData sharedData = SharedData.INSTANCE;

    public void setCaptains(Player player, List<Captain> captains) {
        if (captains.size() != 3) {
            throw new RuntimeException("Captains list with wrong size. " + captains.size());
        }

        PlayerState playerState = getPlayerState(player);

        playerState.setCaptains(new ArrayList<>(captains));
        sharedData.saveAutomationState();
    }
    
    public boolean hasCaptain(Player player, Captain captain) {
        PlayerState playerState = getPlayerState(player);
        return playerState.getCaptains().contains(captain);
    }

}
