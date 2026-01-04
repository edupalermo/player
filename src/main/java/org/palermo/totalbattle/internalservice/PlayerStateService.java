package org.palermo.totalbattle.internalservice;

import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.selenium.stacking.Captain;

import java.util.ArrayList;
import java.util.List;

public class PlayerStateService extends AbstractService {

    private SharedData sharedData = SharedData.INSTANCE;

    public PlayerState getState(PlayerName playerName) {
        return getPlayerState(playerName);
    }

}
