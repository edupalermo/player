package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class CaptainSelector {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public CaptainSelector(Player player) {
        this.player = player;
    }
    
    public void select() {

        BufferedImage garvel = ImageUtil.loadResource("player/hero/garvel_66.png");

        BufferedImage screen = robot.captureScreen();
        Area area = RegionSelector.selectArea("MAIN_HERO_PICTURE", screen);
        Point heroPoint = findHeroPicture(screen, area);
        
        robot.leftClick(heroPoint, garvel);
        robot.sleep(3500);


        // Captain management!
        screen = robot.captureScreen();
        area = RegionSelector.selectArea("CAPTAIN_MANAGEMENT_HERO", screen);
        heroPoint = findHeroPicture(screen, area);

        // Click on the first captain to seen all available
        robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(738, 858)));
        robot.sleep(500);
        
        // Click on the refresh top icon
        robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(1177, 420)));
        robot.sleep(500);


        screen = robot.captureScreen();
        ImageUtil.showImageAndWait(screen, Area.of(heroPoint, Point.of(591, 875), Point.of(1078, 458), Point.of(1442, 899)));
        
        
        Area selectedArea = Area.of(heroPoint, Point.of(591, 875), Point.of(686, 833), Point.of(987, 927));
        ImageUtil.showImageAndWait(screen, selectedArea);
        
        // TODO Remove me!
        robot.sleep(10000);
        
        

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(150);
    }
    
    private Point findHeroPicture(BufferedImage screen, Area area) {
        BufferedImage garvel = ImageUtil.loadResource("player/hero/garvel_66.png");
        Point point = ImageUtil.searchSurroundings(garvel, screen, area, 0.1, 20).orElse(null);
        if (point != null) {
            return point;
        }

        BufferedImage ayrin = ImageUtil.loadResource("player/hero/ayrin_66.png");
        point = ImageUtil.searchSurroundings(ayrin, screen, area, 0.1, 20).orElse(null);
        if (point != null) {
            return point;
        }

        throw new RuntimeException("Could not find hero picture");
    }

}
