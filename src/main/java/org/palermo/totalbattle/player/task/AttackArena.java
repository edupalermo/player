package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.state.location.Arena;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;

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

    public void attack() {
        Point arenaLocation = gameStateService
                .getLocation(Arena.class)
                .stream()
                .map(Arena::getPosition)
                .findFirst()
                .orElse(null);
        if (arenaLocation == null) {
            log.info("No Arena is available");
            return;
        }

        NavigationUtil.switchToMapIfNeeded();
        
        NavigationUtil.zoomInIfNeeded();

        NavigationUtil.goToMapPosition(arenaLocation);
        
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
            log.info("Hero is not available to fight in a Arena");
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
