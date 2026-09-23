package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.Navigate;

@Slf4j
public class CollectAdministrationGift {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public CollectAdministrationGift(Player player) {
        this.player = player;
    }


    public void evaluate() {
        
        Area screenArea = robot.getScreenArea();
        Navigate activeTelescope = Navigate.builder()
                .area(Area.fromTwoPoints(0, (3 * screenArea.getHeight()) / 4, screenArea.getWidth() - 1, screenArea.getHeight() - 1))
                .resourceName("player/label_journal.png")
                .build();

        if (!activeTelescope.exist()) {
            log.info("Label Journal not found!");
            return;
        }

        log.info("Label journal found!");

    }
}
