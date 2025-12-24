package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.util.Navigate;

@Slf4j
public class CheckHeroHealth {

    private Player player;
    private final MyRobot robot = MyRobot.INSTANCE;

    public CheckHeroHealth(Player player) {
        this.player = player;
    }
    
    public boolean isDead() {
        Navigate navigate = Navigate.builder()
                .areaName("MAIN_HERO_PICTURE")
                .resourceName("player/hero/dead_66.png")
                .comparationLimit(0.05)
                .build();
        
        return navigate.exist();
    }
}
