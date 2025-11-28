package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.Navigate;

public class PayTaxes {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public PayTaxes(Player player) {
        this.player = player;
    }

    public void pay() {
        NavigationUtil.switchToCityView();
        NavigationUtil.zoomInIfNeeded();

        // Put map in a predictable position
        robot.mouseDrag(Point.of(1350, 446), -240, 150);
        robot.sleep(250);

        robot.leftClick(Point.of(1545, 507));
        robot.sleep(250);


        Navigate titleCapitol = Navigate.builder()
                .resourceName("player/capitol/title_capitol.png")
                .areaName(Area.CAPITOL_TITLE)
                .waitLimit(3000)
                .build();
        
        robot.leftClick(Point.of(titleCapitol.getPoint(), Point.of(966, 379), Point.of(1138, 490)));
        robot.sleep(300);
        
        
    }
}
