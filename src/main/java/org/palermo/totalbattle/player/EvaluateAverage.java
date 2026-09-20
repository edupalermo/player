package org.palermo.totalbattle.player;

import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;
import org.palermo.totalbattle.util.bean.Histogram;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class EvaluateAverage {
    
    private static final MyRobot robot = MyRobot.INSTANCE;
    
    private static final double[] OPENED = new double[] {166.4, 149.5, 106.7};

    public static void main(String[] args) {

        Point refDailyJobsPoint = Navigate.builder()
                .resourceName("player/ref_daily_jobs.png")
                .build().ensureExistence().getPoint();

        Transformation transformation = Transformation.builder()
                .real(refDailyJobsPoint)
                .reference(Point.of(980, 320))
                .build();

        BufferedImage screen = robot.captureScreen();
        for (int i = 0; i < 6; i++) {

            BufferedImage it = ImageUtil.crop(screen, transformation.transform(Point.of(884 + (i * 79), 590), 55, 58));
            double[] ave = ImageUtil.average(it);
            
            System.out.println(String.format("Index %d: %.1f %.1f %.1f - %s", i, ave[0], ave[1], ave[2], ImageUtil.averageMatch(it, OPENED, 0.01)));
        }
    }
    
    /*
    private static Histogram evaluate(Transformation transformation) {
        BufferedImage screen = robot.captureScreen();

        Histogram minOpened = Histogram.from(ImageUtil.crop(screen, transformation.transform(Point.of(889 + (0 * 79), 608), 42, 32)));
        minOpened = minOpened.intersect(Histogram.from(ImageUtil.crop(screen, transformation.transform(Point.of(889 + (1 * 79), 608), 42, 32))));
        minOpened = minOpened.intersect(Histogram.from(ImageUtil.crop(screen, transformation.transform(Point.of(889 + (2 * 79), 608), 42, 32))));
        
        return minOpened;
    }
     */
    
}
