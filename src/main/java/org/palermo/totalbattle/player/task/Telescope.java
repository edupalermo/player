package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Telescope {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public Telescope(Player player) {
        this.player = player;
    }
    
    public void evaluate() {
        if (SharedData.INSTANCE.getArena().isPresent()) {
            System.out.println("No need to look for arenas");
            return;
        }
        
        Navigate activeTelescope = Navigate.builder()
                .areaName("ACTIVE_TELESCOPE")
                .resourceName("player/icon_telescope.png")
                .build();
        if (!activeTelescope.exist()) {
            System.out.println("Telescope is not activated");
            return;
        }
        activeTelescope.leftClick();

        Navigate titleWatchtower = Navigate.builder()
                .areaName("WATCHTOWER_TITLE")
                .resourceName("player/watchtower/title_watchtower.png")
                .waitLimit(Duration.ofSeconds(3).toMillis())
                .build();
        Point titleWatchtowerPoint = titleWatchtower.search().orElse(null);

        if (!titleWatchtower.exist()) {
            throw new RuntimeException("Could not find watchtower title");
        }

        Navigate labelCryptsAndArenas = Navigate.builder()
                .areaName("WATCHTOWER_LEFT_TAB_CRYPTS_AND_ARENAS_LABEL")
                .resourceName("player/watchtower/label_crypts_and_arenas.png")
                .waitLimit(Duration.ofSeconds(3).toMillis())
                .build();

        if (!labelCryptsAndArenas.exist()) {
            throw new RuntimeException("Could not find crypts and arenas label");
        }
        labelCryptsAndArenas.leftClick();
        
        
        List<Point> topButtons = new ArrayList<>();
        topButtons.add(Point.of(833,427)); // Common
        topButtons.add(Point.of(962,427)); // Rare
        topButtons.add(Point.of(1091,427)); // Epic
        topButtons.add(Point.of(1219,427)); // Arenas
        topButtons.add(Point.of(833,453)); // Others
        
        boolean enabled[] = new boolean[] {false, false, false, true, false};

        for (int i = 0; i < topButtons.size(); i++) {

            Point topButton = topButtons.get(i);
            
            Area area = Area.of(titleWatchtowerPoint, Point.of(946, 323), topButton, topButton.move(28, 17));

            Navigate navigate = Navigate.builder()
                    .area(area)
                    .resourceName("player/watchtower/button_on.png")
                    .build();
            
            if (!enabled[i] && navigate.exist()) {
                navigate.leftClick();                
            }
            else if (enabled[i] && !navigate.exist()) {
                robot.leftClick(topButton, area);
                robot.sleep(500);
            }
        }

        robot.sleep(500);
        robot.leftClick(Point.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(1249, 591)));
        robot.sleep(2000);
        robot.type(KeyEvent.VK_ESCAPE); // Sometimes the bonus sale is shown
        robot.sleep(2000);


        Navigate iconZoomMinus = Navigate.builder()
                .resourceName("player/icon_zoom_minus.png")
                .area(Area.fromTwoPoints(1791, 1003, 1836, 1044))
                .build();
        for (int i = 0; i < 4; i++) {
            iconZoomMinus.leftClick();
        }

        BufferedImage[] arenas = new BufferedImage[2];
        arenas[0] = ImageUtil.loadResource("player/arena/arena_type_i.png");
        arenas[1] = ImageUtil.loadResource("player/arena/arena_type_ii.png");

        BufferedImage screen = robot.captureScreen();
        Area arenaArea = RegionSelector.selectArea("MAP_CENTER_", screen);
        Point arenaPoint = ImageUtil.searchBestFit(arenas, screen, arenaArea);
        robot.mouseMove(arenaPoint.move(arenas[0].getWidth() / 2, arenas[0].getHeight() / 2));


        screen = robot.captureScreen();
        Area coordinatesArea = RegionSelector.selectArea("MAP_COORDINATES", screen);

        
        Navigate yCoordinate = Navigate.builder()
                .area(coordinatesArea)
                .resourceName("player/label_y.png")
                .build();

        Point yPoint = yCoordinate.search().orElse(null);
        if (yPoint == null) {
            throw new RuntimeException("Not found!");
        }

        Area xArea = Area.of(yPoint, Point.of(184, 1056), Point.of(150, 1054), Point.of(176, 1069));
        Area yArea = Area.of(yPoint, Point.of(184, 1056), Point.of(200, 1054), Point.of(228, 1069));
        
        Point arenaCoordinate = Point.of(ImageUtil.ocrNumber(ImageUtil.crop(screen, xArea), true),
                ImageUtil.ocrNumber(ImageUtil.crop(screen, yArea), true));
        SharedData.INSTANCE.addArena(arenaCoordinate);

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
}
