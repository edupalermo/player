package org.palermo.totalbattle.player.task.shared;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.bean.SpeedUpBean;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SpeedUp {
    
    private static MyRobot robot = SharedData.INSTANCE.robot;

    public static final List<SpeedUpBean> speedUps = new ArrayList<>();
    static {
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/1m.png"))
                .seconds(Duration.ofMinutes(1).getSeconds())
                .label("1m")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/15m.png"))
                .seconds(Duration.ofMinutes(15).getSeconds())
                .label("15m")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/1h.png"))
                .seconds(Duration.ofHours(1).getSeconds())
                .label("1h")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/3h.png"))
                .seconds(Duration.ofHours(3).getSeconds())
                .label("3h")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/8h.png"))
                .seconds(Duration.ofHours(8).getSeconds())
                .label("8h")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/15h.png"))
                .seconds(Duration.ofHours(15).getSeconds())
                .label("15h")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/1d.png"))
                .seconds(Duration.ofDays(1).getSeconds())
                .label("1d")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/3d.png"))
                .seconds(Duration.ofDays(3).getSeconds())
                .label("3d")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/7d.png"))
                .seconds(Duration.ofDays(7).getSeconds())
                .label("7d")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/30d.png"))
                .seconds(Duration.ofDays(30).getSeconds())
                .label("30d")
                .build());
    }

    public static boolean clickOnSpeedUp(SpeedUpBean speedUpBean, Point speedUpsTitlePoint) {
        log.info("Searching for {}", speedUpBean.getLabel());

        Area searchArea = Area.of(speedUpsTitlePoint, Point.of(958, 346), Point.of(749, 463), Point.of(797, 780));
        BufferedImage buttonUse = ImageUtil.loadResource("player/speed_up/button_use.png");

        Point scrollPoint = Point.of(speedUpsTitlePoint, Point.of(958, 346), Point.of(1258, 494));

        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                robot.leftClick(scrollPoint);
                robot.sleep(300);
            }
            BufferedImage screen = robot.captureScreen();
            Point speedUpPoint = ImageUtil.search(speedUpBean.getImage(), screen, searchArea, 0.03).orElse(null);
            if (speedUpPoint != null) {
                Area useButtonArea = Area.of(speedUpPoint, 376, 42, 54, 26);
                Point buttonUsePoint = ImageUtil.search(buttonUse, screen, useButtonArea, 0.1).orElse(null);
                if (buttonUsePoint == null) {
                    log.info("Speed up {} not available", speedUpBean.getLabel());
                    return false;
                }
                //log.info("Speed up {} is available, position {}, y {}", speedUpBean.getLabel(), i, buttonUsePoint.getY());
                robot.leftClick(buttonUsePoint, buttonUse);
                robot.sleep(200);
                return true;
            }
            else {
                if (speedUpBean.getLabel().equals("1m")) {
                    return false;
                }
                robot.mouseDrag(scrollPoint, 0, 150);
                robot.sleep(150);
                scrollPoint = scrollPoint.move(0, 150);
            }
        }
        return false;
    }
}
