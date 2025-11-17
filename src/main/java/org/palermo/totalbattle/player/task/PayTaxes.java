package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.selenium.leadership.MyRobot;

public class PayTaxes {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public PayTaxes(Player player) {
        this.player = player;
    }

    public void pay() {

    }
}
