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

public class EvaluateHistogram {
    
    private static final MyRobot robot = MyRobot.INSTANCE;

    public static void main(String[] args) {

        Point refDailyJobsPoint = Navigate.builder()
                .resourceName("player/ref_daily_jobs.png")
                .build().ensureExistence().getPoint();

        Transformation transformation = Transformation.builder()
                .real(refDailyJobsPoint)
                .reference(Point.of(980, 320))
                .build();


        BufferedImage screen = robot.captureScreen();
        /*
        Histogram minOpenedHistogram = Histogram.from(ImageUtil.crop(screen, transformation.transform(Point.of(884 + (3 * 79), 590), 55, 58)));
        System.out.println("Weight: " + minOpenedHistogram.getWeight());
        minOpenedHistogram = minOpenedHistogram.minus(Histogram.from(ImageUtil.crop(screen, transformation.transform(Point.of(884 + (4 * 79), 590), 55, 58))));
        System.out.println("Weight: " + minOpenedHistogram.getWeight());
        minOpenedHistogram = minOpenedHistogram.minus(Histogram.from(ImageUtil.crop(screen, transformation.transform(Point.of(884 + (0 * 79), 590), 55, 58))));
        System.out.println("Weight: " + minOpenedHistogram.getWeight());
         */
        Histogram minOpenedHistogram = Histogram.loadResource("player/daily_quests/dailyRewardOpenedChest.bin");
        System.out.println("Weight: " + minOpenedHistogram.getWeight());

        long lastChange = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        
        Set<Long> crcs = new HashSet<>();
        
        boolean keepGoing;
        do {
            keepGoing = false;
            
            System.out.println("Loop! " + (System.currentTimeMillis() - lastChange) + " " + (System.currentTimeMillis() - start));
            
            screen = robot.captureScreen();
            for (int i = 3; i <= 3; i++) {
                
                BufferedImage it = ImageUtil.crop(screen, transformation.transform(Point.of(884 + (i * 79), 590), 55, 58));
                Histogram histogramIt = Histogram.from(it);
                long crc = ImageUtil.crcImage(it);
                
                if (!minOpenedHistogram.contained(histogramIt)) {
                    if (crcs.contains(crc)) {
                        throw new RuntimeException("Something is wrong!");
                    }
                    minOpenedHistogram = minOpenedHistogram.intersect(histogramIt);
                    System.out.println("Weight: " + minOpenedHistogram.getWeight());
                    lastChange = System.currentTimeMillis();
                    keepGoing = true;
                }
                
                crcs.add(crc);
                
            }
        } while ((keepGoing || ((System.currentTimeMillis() - lastChange) < 60000)) && 
                ((System.currentTimeMillis() - start) < 180000));

        System.out.println("Final Evaluation");

        screen = robot.captureScreen();
        for (int i = 0; i < 5; i++) {

            BufferedImage it = ImageUtil.crop(screen, transformation.transform(Point.of(884 + (i * 79), 590), 55, 58));
            Histogram histogramIt = Histogram.from(it);
            
            System.out.println("Opened[" + i + "] " + minOpenedHistogram.contained(histogramIt));
        }
        minOpenedHistogram.save(Path.of("dailyRewardOpenedChest.bin"));
        
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
