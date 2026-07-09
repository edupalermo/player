package org.palermo.totalbattle.player;

import org.palermo.totalbattle.player.task.BuildArmy;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;

public class Improving {

    private static final MyRobot robot = MyRobot.INSTANCE;

    public static void main(String[] args) {
        robot.leftClick(Point.of(467, 50));
        
        BuildArmy buildArmy = new BuildArmy(Player.PALERMO);
        
        buildArmy.buildArmy();
        
        // buildArmy.playSpeedUpPopup(60);
        
        // buildArmy.fillResource(13900000);
        // buildArmy.playOpenBoostersPopUp();
    }
}
