package org.palermo.totalbattle.player.task.shared;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

@Slf4j
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
        Navigate twentyFivePerc = Navigate.builder()
                .resourceName("player/label_zoom_25.png")
                .areaName(Area.MAIN_ZOOM_LABEL_25)
                .comparationLimit(0.03)
                .build();
        if (twentyFivePerc.exist()) {
            //log.info("Zoom is already 25%");
            return;
        }
        
        // Zoom in
        Navigate iconZoomMinus = Navigate.builder()
                .resourceName("player/icon_zoom_minus.png")
                .area(Area.fromTwoPoints(1791, 1003, 1836, 1044))
                .waitLimit(2000)
                .build();
        for (int i = 0; i < 4; i++) {
            iconZoomMinus.leftClick();
        }
    }
    
    public static Point goToMapPosition(Point position) {
        // Click on the magnifier icon
        Navigate.builder()
                .resourceName("player/icon_magnifier.png")
                .areaName(Area.MAP_MAGNIFIER)
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
        
        return buttonGoPoint.move(buttonGo.getWidth() / 2, buttonGo.getWidth() / 2).move(0, -12);
    }
    
    public static boolean belongsToAnotherClan(Point point) {
        
        return (isInsideAnotherClanArea(point.move(49, 25)) == 1) ||
                (isInsideAnotherClanArea(point.move(-49, 25)) == 1) ||
                (isInsideAnotherClanArea(point.move(49, -25)) == 1) ||
                (isInsideAnotherClanArea(point.move(-49, -25)) == 1);
    }
    
    private static int isInsideAnotherClanArea(Point point) {
        BufferedImage buttonBuildImage = ImageUtil.loadResource("player/watchtower/button_build.png");

        robot.leftClick(point);
        Navigate buttonBuild = Navigate.builder()
                .searchImage(buttonBuildImage)
                .areaName(Area.MAIN_BUILD_BUTTON)
                .waitLimit(1000)
                .build();

        int belongToAnotherClan = -1;
        
        if (buttonBuild.exist()) {
            buttonBuild.leftClick();

            Navigate iconBuild = Navigate.builder()
                    .resourceName("player/watchtower/icon_info.png")
                    .areaName(Area.MAIN_TOAST_INFO)
                    .waitLimit(1000)
                    .build();

            belongToAnotherClan = iconBuild.exist() ? 1 : 0;
        }

        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);

        return belongToAnotherClan;
    }
    
    
    public static Point spotSilverMinePositionPointInTheCenter() {
        BufferedImage mine = ImageUtil.loadResource("player/watchtower/mine_silver.png");
        BufferedImage screen = robot.captureScreen();
        Area centerArea = RegionSelector.selectArea("MAP_CENTER", screen);
        return ImageUtil.searchBestFit(new BufferedImage[] {mine}, screen, centerArea);
    }
    
    public static void switchToCityView() {
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(150);

        Navigate labelCity = Navigate.builder()
                .resourceName("player/label_city.png")
                .area(Area.fromTwoPoints(664, 1059, 716, 1075))
                .waitLimit(2000)
                .build();
        labelCity.leftClickIfExists();

        Navigate labelMap = Navigate.builder()
                .resourceName("player/label_map.png")
                .area(Area.fromTwoPoints(664, 1059, 716, 1075))
                .waitLimit(5000)
                .build();
        labelMap.ensureExistence();
    }

    public static Point identifyCenterCitadel() {
        BufferedImage image = ImageUtil.loadResource("player/watchtower/citadel.png");
        BufferedImage screen = robot.captureScreen();
        Area arenaArea = RegionSelector.selectArea("MAP_CENTER", screen);
        Point point = ImageUtil.searchBestFit(new BufferedImage[] {image}, screen, arenaArea);
        return point.centralize(image);
    }

    public static void speedUpMarch() {
        Navigate.builder()
                .areaName(Area.MAIN_ONGOING_OPERATIONS)
                .resourceName("player/icon_expand_ongoing_operations.png")
                .build()
                .leftClickIfExists();

        Navigate march = Navigate.builder()
                .areaName(Area.MAIN_ONGOING_OPERATIONS)
                .resourceName("player/ongoing_tasks/label_march.png")
                .waitLimit(1000)
                .build();

        if (!march.exist()) {
            return;
        }

        // Clicar no SpeedUps
        robot.leftClick(march.getPoint().move(255, 8));
        robot.sleep(300);


        Navigate speedUpsTitle = Navigate.builder()
                .resourceName("player/speed_up/title_speed_ups.png")
                .area(Area.fromTwoPoints(910, 325, 1066, 361))
                .waitLimit(1000)
                .build();

        if (!speedUpsTitle.exist()) {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }

        Transformation transformation = Transformation.builder()
                .real(speedUpsTitle.getPoint())
                .reference(Point.of(958, 346))
                .build();

        Navigate speedUp = Navigate.builder()
                .area(transformation.transform(Point.of(755, 483), Point.of(798, 638)))
                .resourceName("player/ongoing_tasks/speed_up_50_perc.png")
                .waitLimit(1000)
                .build();

        for (int i = 0; i < 5; i++) {
            if (speedUp.searchAgain().isPresent()) {
                robot.leftClick(speedUp.getPoint().move(402, 57));
                robot.sleep(300);
            }
            else {
                break;
            }
        }
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
}
