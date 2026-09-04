package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Captain;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

@Slf4j
public class CaptainSelector {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;
    
    public CaptainSelector(Player player) {
        this.player = player;
    }

    public boolean select(Captain firstCaptain, Captain secondCaptain, Captain thirdCaptain) {
        Point heroPoint = openCaptainManagementArea();
        enableCaptainsLeftPane(heroPoint);
        
        boolean configured = select(firstCaptain, heroPoint, 0) &&
                select(secondCaptain, heroPoint, 1) &&
                select(thirdCaptain, heroPoint, 2);

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);

        return configured;
    }
    
    private boolean select(Captain captain, Point heroPoint, int slot) {
        if (captain == Captain.UNKNOW || captain == Captain.EMPTY) {
            return true;
        }
        Navigate navigate = Navigate.builder()
                .area(getCaptainArea(heroPoint, slot))
                .searchImage(captain.getImage66())
                .build();

        if (!navigate.exist()) {
            removeCaptainAndSelectSpot(slot, heroPoint);
            selectCaptain(heroPoint, captain);
        }

        return navigate.searchAgain().isPresent();
    }
    

    public void enable(Captain captain) {
        Point heroPoint = openCaptainManagementArea();
        enableCaptainsLeftPane(heroPoint);

        Area selectedArea = Area.of(heroPoint, Point.of(591, 875), Point.of(686, 833), Point.of(987, 927));

        if (isCaptainSelected(player, selectedArea, captain)) {
            log.info("Captain {} is already selected", captain);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }
        
        for (int i = 0; i < 3; i++) {
            removeCaptainAndSelectSpot(i, heroPoint);
            selectCaptain(heroPoint, captain);

            if (isCaptainSelected(player, selectedArea, captain)) {
                log.info("Captain {} is already selected", captain);
                robot.type(KeyEvent.VK_ESCAPE);
                robot.sleep(300);
                return;
            }
        }

        throw new RuntimeException("Could not enable " + captain);
    }

    public void select(Captain captain) {
        try {
            innerSelect(captain);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }
    
    public void innerSelect(Captain captain) {
        Point heroPoint = openCaptainManagementArea();

        Area selectedArea = Area.of(heroPoint, Point.of(591, 875), Point.of(686, 833), Point.of(987, 927));
        if (isCaptainSelected(player, selectedArea, captain)) {
            log.info("Captain is already selected");
            return;
        }

        enableCaptainsLeftPane(heroPoint);

        Area availableAra = Area.of(heroPoint, Point.of(591, 875), Point.of(1078, 458), Point.of(1442, 899));

        // We should do a loop and only proceed if we find the Captain
        boolean foundCaptain = false;
        for (int i = 0; i < 10; i++) {
            BufferedImage screen = robot.captureScreen();

            if (ImageUtil.search(captain.getImage72(), screen, selectedArea, 0.1).isPresent()) {
                log.info("Captain is already selected");
                return;
            }

            if (ImageUtil.search(captain.getImage72(), screen, availableAra, 0.1).isPresent()) {
                foundCaptain = true;
                break;
            }
            
            robot.sleep(1000);            
        }
        
        if (!foundCaptain) {
            throw new RuntimeException("Captain " + captain.name() + " not found!");
        }
        

        // Remove captain from the spot
        switch(captain) {
            case CARTER:
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(835, 902)));
                robot.sleep(500);
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(835, 902)));
                robot.sleep(500);
                break;
            case HELEN:
            case XI_GUIYING:
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(739, 902)));
                robot.sleep(500);
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(739, 902)));
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

        selectCaptain(heroPoint, captain);

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
    
    private void selectCaptain(Point heroPoint, Captain captain) {
        BufferedImage screen = robot.captureScreen();
        Area availableAra = Area.of(heroPoint, Point.of(591, 875), Point.of(1078, 458), Point.of(1442, 899));
        Point targetCaptainPoint = ImageUtil.search(captain.getImage66(), screen, availableAra, 0.1).orElse(null);

        if (targetCaptainPoint == null) {
            throw new RuntimeException(String.format("Captain %s not found!", captain.name()));
            // log.warn("Cannot find captain, we will assume that the captain is already selected");
            //return false;
        }

        robot.leftClick(targetCaptainPoint.move(33, 30));
        robot.sleep(300);
    }
    
    private void removeCaptainAndSelectSpot(int spot, Point heroPoint) {
        switch(spot) {
            case 0:
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(739, 902)));
                robot.sleep(500);
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(739, 902)));
                robot.sleep(500);
                break;
            case 1:
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(835, 902)));
                robot.sleep(500);
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(835, 902)));
                robot.sleep(500);
                break;
            case 2:
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(931, 902)));
                robot.sleep(500);
                robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(931, 902)));
                robot.sleep(500);
                break;
            default:
                throw new RuntimeException("Spot " + spot + " not implemented");
        }
    }
    
    private Point openCaptainManagementArea() {
        // Just to know where to click...
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

        
        return heroPoint;
    }
    
    private void enableCaptainsLeftPane(Point heroPoint) {
        // Click on the first captain to see captains all available
        robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(738, 858)));
        robot.sleep(500);

        // Click on the refresh top icon
        robot.leftClick(Point.of(heroPoint, Point.of(591, 875), Point.of(1177, 420)));
        robot.sleep(500);
    }
    
    private boolean isCaptainSelected(Player player, Area area, Captain captain) {
        BufferedImage screen = robot.captureScreen();
        return ImageUtil.search(captain.getImage72(), screen, area, 0.1).isPresent();
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
            
            BufferedImage items[] = new BufferedImage[6];
            items[0] = ImageUtil.loadResource("player/hero/garvel_66.png");
            items[1] = ImageUtil.loadResource("player/hero/meriones_66.png");
            items[2] = ImageUtil.loadResource("player/hero/thelensia_66.png");
            items[3] = ImageUtil.loadResource("player/hero/ayrin_66.png");
            items[4] = ImageUtil.loadResource("player/hero/ayrin_66_1.png");
            items[5] = ImageUtil.loadResource("player/hero/julia_66.png");
            
            Point point = ImageUtil.searchSurroundings(items, screen, area, 0.1, 20).orElse(null);
            if (point != null) {
                return point;
            }
            else {
                robot.sleep(450);
            }
        } while (System.currentTimeMillis() - start < 15000);

        
        System.out.println("Waited: " + ((System.currentTimeMillis() - start) / 1000));
        throw new RuntimeException("Could not find hero picture");
    }
    
    public void updatePlayerState() {

        Point heroPoint = openCaptainManagementArea();
        
        ArrayList<Captain> captains = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            for (Captain captain: Captain.values()) {
                if (!captain.isReal()) {
                    continue;
                }
                
                if (Navigate.builder()
                        .area(getCaptainArea(heroPoint, i))
                        .searchImage(captain.getImage66())
                        .build()
                        .exist()) {
                    captains.add(captain);
                    break;
                }
            }
            
            if (captains.size() < i + 1) {
                captains.add(Captain.UNKNOW);
            }
        }

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);

    }
    
    private Area getCaptainArea(Point heroPoint, int slot) {
        
        switch (slot) {
            case 0:
                return Area.of(heroPoint, Point.of(591, 875), Point.of(700, 842), Point.of(779, 922));                
            case 1:
                return Area.of(heroPoint, Point.of(591, 875), Point.of(700 + 96, 842), Point.of(779 + 96, 922));
            case 2:
                return Area.of(heroPoint, Point.of(591, 875), Point.of(700 + 192 , 842), Point.of(779 + 192, 922));
            default:
                throw new RuntimeException("Spot not implemented");
                
        }
    }
}
