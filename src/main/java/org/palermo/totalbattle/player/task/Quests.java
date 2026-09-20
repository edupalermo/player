package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.TimeLeftUtil;
import org.palermo.totalbattle.player.bean.SpeedUpBean;
import org.palermo.totalbattle.player.task.shared.SpeedUp;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.server.model.FlagInfo;
import org.palermo.totalbattle.server.model.FlagScenario;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.FlagUtil;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;
import org.palermo.totalbattle.util.OcrUtil;
import org.palermo.totalbattle.util.bean.Histogram;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class Quests {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public Quests(Player player) {
        this.player = player;
    }

    public void evaluate() {
        try {
            if (FlagUtil.isActive(player, FlagScenario.FREEZE_DAILY_JOB_EVALUATION)) {
                FlagInfo flagInfo = player.getFlags().get(FlagScenario.FREEZE_DAILY_JOB_EVALUATION.name());
                log.info(String.format("Daily Jobs is frozen[%s]: %s", FlagUtil.duration(flagInfo), flagInfo.getMessage()));
                return;
            }
            internal();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        finally {
            robot.sleep(300);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }


    private void internal() {
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

        evaluateDailyRewardChests();        

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
        
        Navigate navigateClaim = Navigate.builder()
                .area(trans.transform(Point.of(1238, 750), Point.of(1293, 770)))
                .resourceName("player/button_wr_claim.png")
                .build(); 

        while (navigateClaim.searchAgain().isPresent()) {
            navigateClaim.leftClick();
            robot.sleep(350);
        }

        // Daily Jobs Tab
        robot.leftClick(weeklyReward.getPoint().move(-310, 65));
        robot.sleep(300);
        
        playDailyJobsTab();


    }
    
    private void playDailyJobsTab() {

        Point refDailyJobsPoint = Navigate.builder()
                .resourceName("player/ref_daily_jobs.png")
                .build().ensureExistence().getPoint();

        Transformation trans = Transformation.builder()
                .reference(Point.of(980, 320))
                .real(refDailyJobsPoint)
                .build();

        Navigate icon = Navigate.builder()
            .area(trans.transform(Point.of(1014, 379), Point.of(1046, 407)))
            .resourceName("player/daily_quests/icon_hourglass.png")
            .waitLimit(1000)
            .build();
        
        BufferedImage screen = robot.captureScreen();
        if (icon.exist()) {
            // log.info("Found hourglass");
            BufferedImage timeLeft = ImageUtil.crop(screen, trans.transform(icon.getPoint().move(18, -2), Point.of(1128, 403)));
            String timeLeftAsText = treatTimeLeft(timeLeft, new String[] {"FFF6C2"});
            log.info("Time left: " + timeLeftAsText);
            LocalDateTime nextLocalDateTime = TimeLeftUtil.parse(timeLeftAsText).orElse(null);
            if (nextLocalDateTime != null) {
                if (Duration.between(LocalDateTime.now(), nextLocalDateTime).abs().toMinutes() > 15) {
                    speedUp(trans);
                }
                else if (Duration.between(LocalDateTime.now(), nextLocalDateTime).abs().toHours() <= 3) {
                    player.getFlags().put(FlagScenario.FREEZE_DAILY_JOB_EVALUATION.name(), FlagInfo.builder()
                            .expiration(nextLocalDateTime)
                            .createdAt(LocalDateTime.now())
                            .message("Waiting job to finish.")
                            .build());    
                }
            }
            return; // If there is already a hourglass... nothing else to be done
        }
        
        screen = robot.captureScreen();

        Area topButtonArea = trans.transform(Point.of(1218, 391), Point.of(1298, 416));
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

            Area area = trans.transform(Point.of(1237, 496), Point.of(1297, 517));
            BufferedImage claimStart = ImageUtil.loadResource("player/button_dj_start.png");
            Point startButtonPoint = ImageUtil.search(claimStart, screen, area, 0.1).orElse(null);

            if (startButtonPoint != null) {
                robot.leftClick(startButtonPoint, claimStart);
                robot.sleep(500);
                return;
            }
            
            robot.mouseMove(Point.of(50, 50));
            Navigate freeNavigate = Navigate.builder()
                    .area(area)
                    .resourceName("player/button_dj_free.png")
                    .build();             
            
            if (freeNavigate.exist()) {
                freeNavigate.leftClick();
                return;
            }
            
            //No job to start, should lock daily jobs evaluation!
            BufferedImage hourglass = ImageUtil.loadResource("player/daily_quests/icon_hourglass_2.png");
            
            BufferedImage slice = robot.captureScreen(trans.transform(Point.of(1026, 849), Point.of(1184, 889)));
            
            Point hourglassPoint = ImageUtil.search(hourglass, slice, 0.05).orElse(null);
            if (hourglassPoint == null) {
                log.warn("Hourglass not found!");
                return;
            }

            slice = ImageUtil.crop(slice, Area.fromTwoPoints(hourglassPoint.move(hourglass.getWidth() + 1,0), Point.of(slice.getWidth() - 1, slice.getHeight() - 1)));

            String timeLeftAsText = treatTimeLeft(slice, new String[] {"FFF7BF"});
            //log.info("Time to reload daily jobs: " + timeLeftAsText);
            LocalDateTime nextLocalDateTime = TimeLeftUtil.parse(timeLeftAsText).orElse(null);
            if (nextLocalDateTime != null && Duration.between(LocalDateTime.now(), nextLocalDateTime).abs().toHours() < 2) {
                //log.info("Lower than 2 hours! Let's lock it!");
                player.getFlags().put(FlagScenario.FREEZE_DAILY_JOB_EVALUATION.name(), FlagInfo.builder()
                                .expiration(nextLocalDateTime)
                                .createdAt(LocalDateTime.now())
                                .message("Waiting daily jobs reload.")
                        .build());
                return;
            }
        }
    }
    
    private void evaluateDailyRewardChests() {
        Point refDailyJobsPoint = Navigate.builder()
                .resourceName("player/ref_daily_jobs.png")
                .build().ensureExistence().getPoint();

        Transformation transformation = Transformation.builder()
                .reference(Point.of(980, 320))
                .real(refDailyJobsPoint)
                .build();

        Histogram minOpenedHistogram = Histogram.loadResource("player/daily_quests/dailyRewardOpenedChest.bin");
        BufferedImage screen = robot.captureScreen();
        
        for (int i = 0; i < 5; i++) {
            int x = 884 + (i * 79);
            int y = 590;
            BufferedImage it = ImageUtil.crop(screen, transformation.transform(Point.of(x, y), 55, 58));
            Histogram histogramIt = Histogram.from(it);
            if (minOpenedHistogram.contained(Histogram.from(it))) {
                robot.leftClick(Point.of(x, y), it);
                robot.sleep(350);
            }
        }
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
        
        SpeedUp.clickOnSpeedUp(speedUpsTitle, fifteenMinutes, speedUpsTitle.getPoint());

        if (speedUpsTitle.searchAgain().isPresent()) {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }

    }

    private String treatTimeLeft(BufferedImage input, String[] mainColor) {
        BufferedImage timeLeft = ImageUtil.toGrayscale(input, mainColor);
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        timeLeft = ImageUtil.cropText(timeLeft);
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        if (timeLeft.getHeight() < 100) {
            timeLeft = ImageUtil.resize(timeLeft, 100);
        }
        // ImageUtil.showImageAndWait(timeLeft);
        return OcrUtil.ocr(timeLeft, OcrUtil.WHITELIST_FOR_COUNTDOWN, OcrUtil.PATTERN_FOR_COUNTDOWN);
    }
}
