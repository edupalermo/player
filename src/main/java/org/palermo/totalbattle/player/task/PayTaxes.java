package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.server.model.FlagInfo;
import org.palermo.totalbattle.server.model.FlagScenario;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.FlagUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.time.LocalDateTime;

public class PayTaxes {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;


    public PayTaxes(Player player) {
        this.player = player;
    }
    
    public void pay() {
        try {
            internalPay();
        }
        finally {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(150);
        }
    }

    private void internalPay() {
        if (FlagUtil.isActive(player, FlagScenario.FREEZE_PAY_TAXES)) {
            return;            
        }

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


        Navigate royalTreasury = Navigate.builder()
                .resourceName("player/capitol/title_royal_treasury.png")
                .areaName(Area.CAPITOL_ROYAL_TREASURY_TITLE)
                .waitLimit(3000)
                .build();
        
        
        Point bar = Point.of(royalTreasury.getPoint(), Point.of(938, 438), Point.of(894, 707));
        robot.mouseDrag(bar, 260, 0);
        robot.sleep(300);
        
        robot.leftClick(Point.of(royalTreasury.getPoint(), Point.of(938, 438), Point.of(1003, 752)));
        robot.sleep(300);

        player.getFlags().put(FlagScenario.FREEZE_PAY_TAXES.name(), FlagInfo.builder()
                .createdAt(LocalDateTime.now())
                .expiration(LocalDateTime.now().plusHours(20))
                .message("Player has paid taxes recently")
                .build());
    }
}
