package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class ClanContribution {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public ClanContribution(Player player) {
        this.player = player;
    }

    public void collectChests() {

        BufferedImage screen = robot.captureScreen();

        BufferedImage iconHelpAllies = ImageUtil.loadResource("player/label_clan.png");
        Point labelClanPoint = ImageUtil.searchSurroundings(iconHelpAllies, screen, 0.1, 20).orElse(null);

        if (labelClanPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(iconHelpAllies, "error_image.png");
            throw new RuntimeException("Couldn't find clan image!");
        }
        
        Area positiveFlagArea = Area.of(labelClanPoint, Point.of(1038, 1024), Point.of(1064, 976), Point.of(1072, 984));

        if (!Navigate.builder()
                .area(positiveFlagArea)
                .resourceName("player/my_clan/positive_flag.png")
                .build()
                .exist()) {
            log.info("Nothing to collect from clan");
            return;
        }

        // Click on the clan Icon
        robot.leftClick(labelClanPoint.move(14, -30));
        robot.sleep(1250);

        screen = robot.captureScreen();
        BufferedImage titleMyClan = ImageUtil.loadResource("player/my_clan/title_my_clan.png");
        Point titleMyClanPoint = ImageUtil.searchSurroundings(titleMyClan, screen, 0.1, 20).orElse(null);

        if (titleMyClanPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(iconHelpAllies, "error_image.png");
            throw new RuntimeException("Couldn't My Clan title!");
        }

        // Click on Gifts (left tab)
        robot.leftClick(Point.of(titleMyClanPoint, Point.of(963, 325), Point.of(611, 497)));
        robot.sleep(500);


        for (int i = 0; i < 2; i++ ) {
            if (i == 0) {
                // Click on Gifts  (top tab)
                robot.leftClick(Point.of(titleMyClanPoint, Point.of(963, 325), Point.of(833, 399)));
                robot.sleep(750);
            }
            else {
                // Click on Triumphal Gifts  (top tab)
                robot.leftClick(Point.of(titleMyClanPoint, Point.of(963, 325), Point.of(1074, 399)));
                robot.sleep(750);
            }

            // Click on open button while we have it
            Area buttonArea = Area.of(titleMyClanPoint, Point.of(963, 325), Point.of(1358, 485), Point.of(1409, 505));
            BufferedImage buttonOpen = ImageUtil.loadResource("player/my_clan/button_open.png");
            BufferedImage buttonDelete = ImageUtil.loadResource("player/my_clan/button_delete.png");
            
            boolean shouldContinue = true;
            do {
                screen = robot.captureScreen();
                Point buttonPoint = ImageUtil.search(buttonOpen, screen, buttonArea, 0.1).orElse(null);

                if (buttonPoint != null) {
                    robot.leftClick(buttonPoint, buttonOpen);
                    robot.sleep(50);
                    continue;
                }

                buttonPoint = ImageUtil.search(buttonDelete, screen, buttonArea, 0.1).orElse(null);
                if (buttonPoint != null) {
                    robot.leftClick(buttonPoint, buttonDelete);
                    robot.sleep(50);
                    continue;
                }
                shouldContinue = false;
            } while(shouldContinue);
        }

        robot.sleep(500);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(500);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }

    public void helpClanMembers() {
        BufferedImage screen = robot.captureScreen();
        BufferedImage iconHelpAllies = ImageUtil.loadResource("player/icon_help_allies.png");
        Area iconHelpAlliesArea = RegionSelector.selectArea("HELP_CLAN_SHAKING_HANDS", screen);

        Point point = ImageUtil.searchSurroundings(iconHelpAllies, screen, iconHelpAlliesArea, 0.1, 20).orElse(null);

        if (point == null) {
            System.out.println("No help allies icon found");
        }
        else {
            System.out.println("Clicked on help allies icon");
            robot.leftClick(point, iconHelpAllies);
            robot.sleep(2500);
        }
    }
}
