package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Quests {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    private final LockService lockService = new LockService();

    public Quests(Player player) {
        this.player = player;
    }

    public void evaluate() {
        BufferedImage screen = robot.captureScreen();

        BufferedImage labelQuestes = ImageUtil.loadResource("player/label_quests.png");
        Point labelQuestesPoint = ImageUtil.searchSurroundings(labelQuestes, screen, 0.1, 20).orElse(null);

        if (labelQuestesPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(labelQuestes, "error_image.png");
            throw new RuntimeException("Couldn't find quests label!");
        }

        // Click on the Quests icon
        robot.leftClick(labelQuestesPoint.move(14, -30));
        robot.sleep(1000);

        // Tem que checar se tem ouro
        if (lockService.isFree(player, Scenario.QUESTS_TRY_FULL_CHESTS))  {

            List<Point> chests = new ArrayList<Point>();

            chests.add(Point.of(958, 455));
            chests.add(Point.of(1088, 455));
            chests.add(Point.of(1222, 455));

            chests.add(Point.of(910, 620));
            chests.add(Point.of(990, 620));
            chests.add(Point.of(1068, 620));
            chests.add(Point.of(1144, 620));
            chests.add(Point.of(1220, 620));
            chests.add(Point.of(1304, 620));

            for (Point point : chests) {
                robot.leftClick(point);
                robot.sleep(450);
            }
            robot.sleep(3500); // Wait toast to disappear
            lockService.lock(player, Scenario.QUESTS_TRY_FULL_CHESTS, LocalDateTime.now().plusHours(2));
        }

        screen = robot.captureScreen();
        BufferedImage weeklyReward = ImageUtil.loadResource("player/label_weekly_reward.png");
        Area weeklyRewardArea = RegionSelector.selectArea("QUESTS_DAILY_QUESTS_WEEKLY_REWARD", screen);
        Point weeklyRewardPoint = ImageUtil.searchSurroundings(weeklyReward, screen, weeklyRewardArea, 0.1, 20).orElse(null);

        if (weeklyRewardPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(labelQuestes, "error_image.png");
            throw new RuntimeException("Couldn't find weekly reward label!");
        }

        Area claimArea = Area.of(weeklyRewardPoint, Point.of(1022, 366), Point.of(1238, 750), Point.of(1293, 770));
        BufferedImage buttonClaim = ImageUtil.loadResource("player/button_wr_claim.png");
        Point buttonClaimPoint = ImageUtil.search(buttonClaim, screen, claimArea, 0.1).orElse(null);

        if (buttonClaimPoint != null) {
            robot.leftClick(buttonClaimPoint, buttonClaim);
        }

        // Daily Jobs Tab
        robot.leftClick(weeklyRewardPoint.move(-310, 65));
        robot.sleep(300);

        screen = robot.captureScreen();
        BufferedImage refDailyJobs = ImageUtil.loadResource("player/ref_daily_jobs.png");
        Point refDailyJobsPoint = ImageUtil.searchSurroundings(refDailyJobs, screen, 0.1, 20).orElse(null);

        if (refDailyJobsPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(refDailyJobs, "error_image.png");
            throw new RuntimeException("Couldn't find Daily Jobs reference!");
        }


        Area topButtonArea = Area.of(refDailyJobsPoint, Point.of(980, 320), Point.of(1218, 391), Point.of(1298, 416));
        BufferedImage claimButton = ImageUtil.loadResource("player/button_dj_claim.png");
        Point claimButtonPoint = ImageUtil.search(claimButton, screen, topButtonArea, 0.1).orElse(null);

        if (claimButtonPoint != null) {
            robot.leftClick(claimButtonPoint, claimButton);
            robot.sleep(500);
        }

        screen = robot.captureScreen();
        BufferedImage speedUpButton = ImageUtil.loadResource("player/button_dj_speed_up.png");
        Point speedUpButtonPoint = ImageUtil.search(speedUpButton, screen, topButtonArea, 0.1).orElse(null);
        if (speedUpButtonPoint == null) {

            Area area = Area.of(refDailyJobsPoint, Point.of(980, 320), Point.of(1237, 496), Point.of(1297, 517));
            BufferedImage claimStart = ImageUtil.loadResource("player/button_dj_start.png");
            Point startButtonPoint = ImageUtil.search(claimStart, screen, area, 0.1).orElse(null);

            if (startButtonPoint != null) {
                robot.leftClick(startButtonPoint, claimStart);
                robot.sleep(500);
            }
        }

        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }

}
