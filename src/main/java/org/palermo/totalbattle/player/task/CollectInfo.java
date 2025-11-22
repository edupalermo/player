package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.selenium.leadership.MyRobot;

public class CollectInfo {
    
    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public CollectInfo(Player player) {
        this.player = player;
    }
    
    
    
}
