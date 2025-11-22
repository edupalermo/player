package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Captain;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

@Slf4j
public class CaptainSelector {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;
    
    private PlayerStateService playerStateService = new PlayerStateService();

    public CaptainSelector(Player player) {
        this.player = player;
    }

    public void select(String first, String second, String third) {
        Point heroPoint = openCaptainManagementArea();

        throw new RuntimeException("Not Implemented!");
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
        Point heroPoint = openCaptainManagementArea();
        enableCaptainsLeftPane(heroPoint);

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
            case HELEN:
            case XI_GUIYING:
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

        selectCaptain(heroPoint, captain);

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
    
    private void selectCaptain(Point heroPoint, Captain captain) {
        BufferedImage screen = robot.captureScreen();
        Area availableAra = Area.of(heroPoint, Point.of(591, 875), Point.of(1078, 458), Point.of(1442, 899));
        Point targetCaptainPoint = ImageUtil.search(captain.getImage66(), screen, availableAra, 0.1).orElse(null);

        if (targetCaptainPoint == null) {
            throw new RuntimeException("Not found!");
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

        playerStateService.setCaptains(player, captains);
        
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
