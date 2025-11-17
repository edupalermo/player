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

    public static final String CARTER = "carter";
    public static final String TRAINER = "trainer";
    public static final String STROR = "stror";

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public CaptainSelector(Player player) {
        this.player = player;
    }
    
    public void select(String captain) {

        BufferedImage garvel = ImageUtil.loadResource("player/hero/garvel_66.png");

        BufferedImage screen = robot.captureScreen();
        Area area = RegionSelector.selectArea("MAIN_HERO_PICTURE", screen);
        Point heroPoint = findHeroPicture(area);
        
        robot.leftClick(heroPoint, garvel);
        robot.sleep(500);


        // Captain management!
        screen = robot.captureScreen();
        area = RegionSelector.selectArea("CAPTAIN_MANAGEMENT_HERO", screen);
        heroPoint = findHeroPicture(area);

        // Click on the first captain to seen all available
        robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(738, 858)));
        robot.sleep(500);
        
        // Click on the refresh top icon
        robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(1177, 420)));
        robot.sleep(500);

        Area selectedArea = Area.of(heroPoint, Point.of(591, 875), Point.of(686, 833), Point.of(987, 927));

        if (isCaptainSelected(player, selectedArea, captain)) {
            System.out.println("Captain is already selected");
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(150);
            return;
        }


        // Remove captain from the spot
        switch(captain) {
            case CARTER:
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(739, 902)));
                robot.sleep(500);
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(739, 902)));
                robot.sleep(500);
                break;
            case TRAINER:
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(835, 902)));
                robot.sleep(500);
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(835, 902)));
                robot.sleep(500);
                break;
            case STROR:
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(931, 902)));
                robot.sleep(500);
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(931, 902)));
                robot.sleep(500);
                break;
            default:
                throw new RuntimeException("Not implemented");
        }


        screen = robot.captureScreen();
        Area availableAra = Area.of(heroPoint, Point.of(591, 875), Point.of(1078, 458), Point.of(1442, 899));
        BufferedImage targetCaptain = loadResource(player, captain, "66"); 
        Point targetCaptainPoint = ImageUtil.search(targetCaptain, screen, availableAra, 0.1).orElse(null);
        
        if (targetCaptainPoint == null) {
            throw new RuntimeException("Not found!");
        }
        
        robot.leftClick(targetCaptainPoint.move(33, 30));
        robot.sleep(300);        

        /*
        screen = robot.captureScreen();
        Area firstArea = Area.of(heroPoint, Point.of(591, 875), Point.of(693, 836), Point.of(787, 930));
        ImageUtil.showImageAndWait(screen, firstArea);
        Area secondArea = Area.of(heroPoint, Point.of(591, 875), Point.of(790, 836), Point.of(881, 930));
        ImageUtil.showImageAndWait(screen, secondArea);
        Area thirdArea = Area.of(heroPoint, Point.of(591, 875), Point.of(885, 836), Point.of(979, 930));
        ImageUtil.showImageAndWait(screen, thirdArea);
      */  

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(150);
    }
    
    private boolean isCaptainSelected(Player player, Area area, String captain) {
        BufferedImage screen = robot.captureScreen();
        BufferedImage captainImage = loadResource(player, captain, "72");
        return ImageUtil.search(captainImage, screen, area, 0.1).isPresent();
    }
    
    private void select(Area spotArea, Area selectionArea, BufferedImage targetCaptain) {
        
        robot.mouseMove(Point.of(100, 100));
        robot.sleep(200);
        
        BufferedImage screen = robot.captureScreen();
        
        BufferedImage empty = ImageUtil.loadResource("player/captain/empty_66.png");
        if (ImageUtil.search(empty, screen, spotArea, 0.1).isEmpty()) {
            System.out.println("Captain spot is not empty");
        }
        
        robot.leftClick(spotArea);
        robot.sleep(200);
        
        
    }
    
    private Point findHeroPicture(Area area) {
        long start = System.currentTimeMillis();

        do {
            BufferedImage screen = robot.captureScreen();
            
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

            BufferedImage julia = ImageUtil.loadResource("player/hero/julia_66.png");
            point = ImageUtil.searchSurroundings(julia, screen, area, 0.1, 20).orElse(null);
            if (point != null) {
                return point;
            }
        } while (System.currentTimeMillis() - start < 10000);

        throw new RuntimeException("Could not find hero picture");
    }

    private BufferedImage loadResource(Player player, String captain, String size) {
        String captainName = captain;
        if (TRAINER.equalsIgnoreCase(captainName)) {
            if (player.isHasHelen()) {
                captainName = "helen";
            }
            else {
                captainName = "xi_guiying";
            }
        }
        
        return ImageUtil.loadResource(String.format("player/captain/%s_%s.png", captainName, size));
    }
}
