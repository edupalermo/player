package org.palermo.totalbattle;

import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.OcrUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Pattern;

public class SearchMine {


    private static final MyRobot robot = MyRobot.INSTANCE;
    
    private static final Pattern PERCENT = Pattern.compile("^[0-9]{1,2}%$");
    
    public static void main(String arg[]) {
        for (int i = 0; i < 50; i++) {
            System.out.println("Loop " + i);
            BufferedImage screen = robot.captureScreen(Area.fromTwoPoints(Point.of(912, 520), Point.of(1131, 896)));
            BufferedImage labelExplorationProgress = ImageUtil.loadResource("mining/label_exploration_progress.png");

            List<Point> points = ImageUtil.searchMultiple(labelExplorationProgress, screen, 0.03);

            points.stream().parallel().forEach(point -> {
                BufferedImage percentImage = ImageUtil.crop(screen, Area.of(point.getX() + labelExplorationProgress.getWidth() + 3, point.getY() + 1, 38, 17));

                percentImage = ImageUtil.toGrayscale(percentImage, new String[] {"4C2727"});
                percentImage = ImageUtil.linearNormalization(percentImage);
                percentImage =ImageUtil.cropText(percentImage);
                percentImage = ImageUtil.linearNormalization(percentImage);
                if (percentImage.getHeight() < 70) {
                    percentImage = ImageUtil.resize(percentImage, 70);
                }

                String numberAsText = OcrUtil.ocr(percentImage, "0123456789%", PERCENT, false);
                String number = numberAsText.substring(0, numberAsText.length() - 1);

                System.out.println("Exploration progress " + numberAsText);

                if (Integer.parseInt(number) >= 50) {
                    System.out.println(String.format("Found %s!", numberAsText));
                    Toolkit.getDefaultToolkit().beep();
                    System.exit(1);
                }
            });
            
            robot.leftClick(Point.of(929, 570));
            robot.mouseWheel(Point.of(929, 570), 10);
            robot.mouseWheel(Point.of(929, 570), 10);
            robot.mouseWheel(Point.of(929, 570), 10);
        }
    }
}
