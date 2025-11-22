package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.state.location.Arena;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class AttackArena {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;
    
    private final GameStateService gameStateService = new GameStateService();

    public AttackArena(Player player) {
        this.player = player;
    }

    public void attackArena() {
        
        Point arenaLocation = gameStateService
                .getLocation(Arena.class)
                .map(Arena::getPosition)
                .orElse(null);
        if (arenaLocation == null) {
            log.info("No arena is available");
            return;
        }
        
        Navigate labelMap = Navigate.builder()
                .resourceName("player/label_map.png")
                .areaName("BOTTOM_MENU_MAP_LABEL")
                .waitLimit(4000)
                .build();
        if (labelMap.exist()) {
            robot.leftClick(labelMap.search().get().move(12, -31));
            robot.sleep(2000);
        }

        // When we switch to map, the Bonus Sales appear again
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(2000);

        // Zoom in

        Navigate iconZoomMinus = Navigate.builder()
                .resourceName("player/icon_zoom_minus.png")
                .area(Area.fromTwoPoints(1791, 1003, 1836, 1044))
                .build();
        for (int i = 0; i < 4; i++) {
            iconZoomMinus.leftClick();
        }

        // Click on the magnifier icon
        Navigate.builder()
                .resourceName("player/icon_magnifier.png")
                .waitLimit(10000)
                .build().leftClick();
        robot.sleep(1000);

        // Wait GO Button to appear (it will not click on it)
        BufferedImage buttonGo = ImageUtil.loadResource("player/button_go.png");
        Point buttonGoPoint = Navigate.builder()
                .searchImage(buttonGo)
                .waitLimit(5000)
                .build()
                .search().orElse(null);

        robot.leftClick(Point.of(buttonGoPoint, Point.of(981, 617), Point.of(1022, 580)));
        robot.clearText();
        robot.sleep(200);
        robot.typeString(Integer.toString(arenaLocation.getX()));

        robot.leftClick(Point.of(buttonGoPoint, Point.of(981, 617), Point.of(1127, 580)));
        robot.clearText();
        robot.sleep(200);
        robot.typeString(Integer.toString(arenaLocation.getY()));

        robot.leftClick(buttonGoPoint, buttonGo);
        robot.sleep(1000);

        // Try to click in the arena in the center of the screen
        BufferedImage arena = ImageUtil.loadResource("player/arena/arena_type_i.png");
        Point arenaPoint = ArenaUtil.identifyCenterArena();
        robot.leftClick(arenaPoint.move(0, -5), arena);
        robot.sleep(1000);

        BufferedImage screen = robot.captureScreen();
        BufferedImage labelArena = ImageUtil.loadResource("player/label_arena.png");
        Area labelArenaArea = Area.fromTwoPoints(896, 305, 1034, 338);
        Point labelArenaPoint = ImageUtil.search(labelArena, screen, labelArenaArea, 0.1).orElse(null);

        if (labelArenaPoint == null) {
            log.info("Arena doesn't exist anymore!");
            gameStateService.removeLocationAt(arenaLocation);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }

        screen = robot.captureScreen();
        BufferedImage iconCheckmark = ImageUtil.loadResource("player/icon_checkmark.png");
        Area areaForCheckmark = Area.of(labelArenaPoint, Point.of(971, 322), Point.of(865, 705), Point.of(901, 739));
        // ImageUtil.showImageAndWait(ImageUtil.crop(screen, areaForCheckmark));
        Point iconCheckmarkPoint = ImageUtil.search(iconCheckmark, screen, areaForCheckmark, 0.1)
                .orElse(null);

        if (iconCheckmarkPoint == null) {
            System.out.println("Hero is not available");
            robot.type(KeyEvent.VK_ESCAPE);
        }
        else {
            // Click Fight
            robot.leftClick(Point.of(labelArenaPoint, Point.of(971, 322), Point.of(1145, 865)));
        }

        // Close Arena window if Hero is not available
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
}
