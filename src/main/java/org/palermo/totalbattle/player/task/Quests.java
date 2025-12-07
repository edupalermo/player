package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.TimeLeftUtil;
import org.palermo.totalbattle.player.bean.SpeedUpBean;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.player.task.shared.SpeedUp;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Quests {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    private final LockService lockService = new LockService();
    private final GameStateService gameStateService = new GameStateService();

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
        Navigate weeklyReward = Navigate.builder()
                .resourceName("player/label_weekly_reward.png")
                .areaName("QUESTS_DAILY_QUESTS_WEEKLY_REWARD")
                .waitLimit(5000)
                .build(); 

        Transformation trans = Transformation.builder()
                .real(weeklyReward.getPoint())
                .reference(Point.of(1022, 366))
                .build();
        
        Area claimArea = Area.of(weeklyReward.getPoint(), Point.of(1022, 366), Point.of(1238, 750), Point.of(1293, 770));
        BufferedImage buttonClaim = ImageUtil.loadResource("player/button_wr_claim.png");
        Point buttonClaimPoint = ImageUtil.search(buttonClaim, screen, claimArea, 0.1).orElse(null);

        if (buttonClaimPoint != null) {
            robot.leftClick(buttonClaimPoint, buttonClaim);
        }

        // Daily Jobs Tab
        robot.leftClick(weeklyReward.getPoint().move(-310, 65));
        robot.sleep(300);

        Navigate icon = Navigate.builder()
                .area(trans.transform(Point.of(1014, 379), Point.of(1046, 407)))
                .resourceName("player/daily_quests/icon_hourglass.png")
                .waitLimit(1000)
                .build();
        
        if (icon.exist()) {
            log.info("Found hourglass");

            screen = robot.captureScreen();
            BufferedImage timeLeft = ImageUtil.crop(screen, trans.transform(icon.getPoint().move(18, -2), Point.of(1128, 403)));
            String timeLeftAsText = treatTimeLeft(timeLeft);
            log.info("Time left: " + timeLeftAsText);
            LocalDateTime nextLocalDateTime = TimeLeftUtil.parse(timeLeftAsText).orElse(null);
            if (nextLocalDateTime != null) {
                if (Duration.between(LocalDateTime.now(), nextLocalDateTime).abs().toMinutes() > 15) {
                    speedUp(trans);
                }
            }
            
        }

        BufferedImage refDailyJobs = ImageUtil.loadResource("player/ref_daily_jobs.png");
        Point refDailyJobsPoint = ImageUtil.searchSurroundings(refDailyJobs, screen, 0.1, 20).orElse(null);

        if (refDailyJobsPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(refDailyJobs, "error_image.png");
            throw new RuntimeException("Couldn't find Daily Jobs reference!");
        }

        Area topButtonArea = Area.of(refDailyJobsPoint, Point.of(980, 320), Point.of(1218, 391), Point.of(1298, 416));
        BufferedImage claimButton = ImageUtil.loadResource("player/button_dj_claim.png");
        Point claimButtonPoint = ImageUtil.search(claimButton, screen, topButtonArea, 0.35).orElse(null);

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
    
    private void speedUp(Transformation transformation) {
        //Click of the speed up button
        robot.leftClick(transformation.transform(Point.of(1257, 407)));
        robot.sleep(300);

        Navigate speedUpsTitle = Navigate.builder()
                .resourceName("player/speed_up/title_speed_ups.png")
                .area(Area.fromTwoPoints(910, 325, 1066, 361))
                .waitLimit(1000)
                .build();

        if (!speedUpsTitle.exist()) {
            return;
        }

        SpeedUpBean fifteenMinutes = SpeedUp.speedUps.stream()
                .filter((sp) -> sp.getSeconds() == Duration.ofMinutes(15).getSeconds())
                .findFirst()
                .orElse(null);
        
        if (fifteenMinutes == null) { 
            return;
        }
        
        SpeedUp.clickOnSpeedUp(fifteenMinutes, speedUpsTitle.getPoint());

        if (speedUpsTitle.searchAgain().isPresent()) {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }

    }

    private String treatTimeLeft(BufferedImage input) {
        BufferedImage timeLeft = ImageUtil.toGrayscale(input, new String[] {"FFF6C2"});
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        timeLeft = ImageUtil.cropText(timeLeft);
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        if (timeLeft.getHeight() < 100) {
            timeLeft = ImageUtil.resize(timeLeft, 100);
        }
        // ImageUtil.showImageAndWait(timeLeft);
        boolean manualOcr = gameStateService.getPropertyAsBoolean(GameStateService.PROPERTY_MANUAL_OCR);
        return ImageUtil.ocr(timeLeft, ImageUtil.WHITELIST_FOR_COUNTDOWN, ImageUtil.PATTERN_FOR_COUNTDOWN, manualOcr);
    }


}
