package org.palermo.totalbattle.player.task.shared;

import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class NavigationUtil {
    
    private static final MyRobot robot = SharedData.INSTANCE.robot; 
    
    public static void switchToMapIfNeeded() {
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
    }
    
    public static void zoomInIfNeeded() {
        // Zoom in
        Navigate iconZoomMinus = Navigate.builder()
                .resourceName("player/icon_zoom_minus.png")
                .area(Area.fromTwoPoints(1791, 1003, 1836, 1044))
                .build();
        for (int i = 0; i < 4; i++) {
            iconZoomMinus.leftClick();
        }
    }
    
    public static void goToMapPosition(Point position) {
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
        robot.typeString(Integer.toString(position.getX()));

        robot.leftClick(Point.of(buttonGoPoint, Point.of(981, 617), Point.of(1127, 580)));
        robot.clearText();
        robot.sleep(200);
        robot.typeString(Integer.toString(position.getY()));

        robot.leftClick(buttonGoPoint, buttonGo);
        robot.sleep(2000);
    }
    
    public static Point spotSilverMinePositionPointInTheCenter() {
        BufferedImage mine = ImageUtil.loadResource("player/watchtower/mine_silver.png");
        BufferedImage screen = robot.captureScreen();
        Area centerArea = RegionSelector.selectArea("MAP_CENTER", screen);
        return ImageUtil.searchBestFit(new BufferedImage[] {mine}, screen, centerArea);
    }
}
