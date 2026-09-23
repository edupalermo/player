package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.server.model.Player;

public class MineSilver {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;
    public MineSilver(Player player) {
        this.player = player;
    }

    public void evaluate() {

    }
}
