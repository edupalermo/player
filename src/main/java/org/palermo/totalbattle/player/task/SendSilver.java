package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.selenium.leadership.MyRobot;

public class SendSilver {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final PlayerName playerName;

    public SendSilver(PlayerName playerName) {
        this.playerName = playerName;
    }

    public void send() {

    }
}
