package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.util.Navigate;

public class NoName {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public NoName(Player player) {
        this.player = player;
    }

    public void evaluate() {
        
        Navigate.builder()
                .resourceName("")
                .build();
        

    }
}
