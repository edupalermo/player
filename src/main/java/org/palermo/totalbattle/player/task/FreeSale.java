package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.TimeLeftUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FreeSale {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;
    
    private final LockService lockService = new LockService();

    public FreeSale(Player player) {
        this.player = player;
    }

    public void freeSale() {
        if (lockService.isLocked(player, Scenario.BONUS_SALES_FREE)) {
            return;
        }
        
        BufferedImage screen = robot.captureScreen();

        BufferedImage logoTotalBattle = ImageUtil.loadResource("player/logo_total_battle.png");
        Point logoTotalBattlePoint = ImageUtil.searchSurroundings(logoTotalBattle, screen, 0.1, 20).orElse(null);

        if (logoTotalBattlePoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(logoTotalBattle, "error_image.png");
            throw new RuntimeException("Couldn't find english label!");
        }

        // Click on the Free Sale Icon
        robot.leftClick(logoTotalBattlePoint.move(1870 - logoTotalBattlePoint.getX(), 73));
        robot.sleep(1000);

        screen = robot.captureScreen();

        BufferedImage refBonusSales = ImageUtil.loadResource("player/ref_bonus_sales.png");
        Point refBonusSalesPoint = ImageUtil.searchSurroundings(refBonusSales, screen, 0.1, 20).orElse(null);

        if (refBonusSalesPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(refBonusSales, "error_image.png");
            throw new RuntimeException("Couldn't Bonus Sales reference!");
        }


        List<Point> tabs = new ArrayList<>();
        tabs.add(Point.of(78, 60));
        tabs.add(Point.of(78, 111));
        tabs.add(Point.of(78, 166));
        tabs.add(Point.of(78, 218));
        tabs.add(Point.of(78, 271));
        tabs.add(Point.of(78, 321));
        tabs.add(Point.of(78, 374));

        // Area area = Area.of(refBonusSalesPoint, Point.of(66, 404), Point.of(377, 873), Point.of(425, 896));
        Area area = Area.of(refBonusSalesPoint, Point.of(66, 404), Point.of(341, 873), Point.of(798, 896));

        BufferedImage buttonFree = ImageUtil.loadResource("player/button_bs_free.png");
        BufferedImage iconHourglass = ImageUtil.loadResource("player/icon_bs_hourglass.png");

        for (Point move: tabs) {
            robot.leftClick(refBonusSalesPoint.move(move.getX(), move.getY()));
            robot.sleep(750);

            screen = robot.captureScreen();

            Point buttonFreePoint = ImageUtil.search(buttonFree, screen, area, 0.1).orElse(null);

            if (buttonFreePoint != null) {
                robot.leftClick(buttonFreePoint, buttonFree);
                break;
            }

            Point iconHourglassPoint = ImageUtil.search(iconHourglass, screen, area, 0.1).orElse(null);
            if (iconHourglassPoint != null) {
                BufferedImage next = ImageUtil.crop(screen, Area.of(iconHourglassPoint.getX() + 18, iconHourglassPoint.getY(), 100, 18));
                next = ImageUtil.toGrayscale(next);
                next = ImageUtil.invertGrayscale(next);
                next = ImageUtil.linearNormalization(next);
                String nextAsText = ImageUtil.ocr(next, ImageUtil.WHITELIST_FOR_COUNTDOWN, ImageUtil.LINE_OF_PRINTED_TEXT);
                LocalDateTime nextLocalDateTime = TimeLeftUtil.parse(nextAsText).orElse(null);
                if (nextLocalDateTime != null) {
                    lockService.lock(player, Scenario.BONUS_SALES_FREE, nextLocalDateTime);
                    break;
                }
            }
        }

        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }

}
