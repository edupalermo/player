package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.selenium.leadership.MyRobot;

public class SendSilver {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public SendSilver(Player player) {
        this.player = player;
    }

    public void send() {

    }
}
