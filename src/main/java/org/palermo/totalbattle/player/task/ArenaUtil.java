package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;

import java.awt.image.BufferedImage;

public class ArenaUtil {

    private static MyRobot robot = MyRobot.INSTANCE;

    public static Point identifyCenterArena() {
        BufferedImage[] arenas = new BufferedImage[2];
        arenas[0] = ImageUtil.loadResource("player/arena/arena_type_i.png");
        arenas[1] = ImageUtil.loadResource("player/arena/arena_type_ii.png");

        BufferedImage screen = robot.captureScreen();
        Area arenaArea = RegionSelector.selectArea("MAP_CENTER", screen);
        return ImageUtil.searchBestFit(arenas, screen, arenaArea);
    }
}
