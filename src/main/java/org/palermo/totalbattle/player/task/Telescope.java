package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.state.location.Arena;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class Telescope {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;
    
    private final GameStateService gameStateService = new GameStateService();

    public Telescope(Player player) {
        this.player = player;
    }
    
    public void findArena() {
        if (gameStateService.getLocation(Arena.class).isPresent()) {
            log.info("No need for more arenas");
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

        for (int i = 0; i < 3; i++) {
            findArenaByIndex(activeTelescope, i);
        }
        
    }
    
    private void findArenaByIndex(Navigate activeTelescope, int index) {
        // Click on the telescope icon
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
        
        // Here we click on the GO
        robot.leftClick(Point.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(1249, 591 + (index * 100))));
        
        robot.sleep(2000);
        robot.type(KeyEvent.VK_ESCAPE); // Sometimes the bonus sale is shown
        robot.sleep(3000);
        
        zoomMinus();

        BufferedImage arena = ImageUtil.loadResource("player/arena/arena_type_i.png");
        Point arenaPoint = ArenaUtil.identifyCenterArena();
        
        robot.mouseMove(arenaPoint.move(arena.getWidth() / 2, arena.getHeight() / 2));
        robot.sleep(500);

        Point arenaCoordinate = readCoordinate();
        log.info("Arena found at {}, {}", arenaCoordinate.getX(), arenaCoordinate.getY());
        SharedData.INSTANCE.addArena(arenaCoordinate);
        gameStateService.add(Arena.builder()
                .position(arenaCoordinate)
                .build());

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
    
    private Point readCoordinate() {
        BufferedImage screen = robot.captureScreen();
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

        return Point.of(ocr(ImageUtil.crop(screen, xArea)), ocr(ImageUtil.crop(screen, yArea)));
    } 
    
    private int ocr(BufferedImage input) {
        BufferedImage image = ImageUtil.toGrayscale(input, new String[] {"F6E7B6"});
        image = ImageUtil.linearNormalization(image);
        image = ImageUtil.cropText(image);
        image = ImageUtil.linearNormalization(image);
        if (image.getHeight() < ImageUtil.OCR_HEIGHT) {
            image = ImageUtil.resize(image, ImageUtil.OCR_HEIGHT);
        }
        String quantityAsString = ImageUtil.ocr(image, ImageUtil.WHITELIST_FOR_ONLY_NUMBERS, ImageUtil.PATTERN_FOR_ONLY_NUMBERS);
        return Integer.parseInt(quantityAsString);
    }
    
    private void zoomMinus() {
        Navigate iconZoomMinus = Navigate.builder()
                .resourceName("player/icon_zoom_minus.png")
                .area(Area.fromTwoPoints(1791, 1003, 1836, 1044))
                .build();
        for (int i = 0; i < 4; i++) {
            iconZoomMinus.leftClick();
        }
    }
    
    public void findSilverMines() {

        Point minePoint = null;

        for (int i = 0; i < 10; i++) {

            Point titleWatchtowerPoint = openWatchtower().orElse(null);

            if (titleWatchtowerPoint == null) {
                System.out.println("Telescope is not activated");
                return;
            }

            // Click on Mines let tab
            robot.leftClick(Point.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(715, 613)));
            robot.sleep(500);

            Area resourcesArea = Area.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(831, 420), Point.of(1337, 461));

            clickIfFindIt(ImageUtil.loadResource("player/watchtower/icon_wood_on.png"), resourcesArea);
            clickIfFindIt(ImageUtil.loadResource("player/watchtower/icon_iron_on.png"), resourcesArea);
            clickIfFindIt(ImageUtil.loadResource("player/watchtower/icon_stone_on.png"), resourcesArea);
            clickIfFindIt(ImageUtil.loadResource("player/watchtower/icon_food_on.png"), resourcesArea);
            clickIfFindIt(ImageUtil.loadResource("player/watchtower/icon_silver_off.png"), resourcesArea);
            clickIfFindIt(ImageUtil.loadResource("player/watchtower/icon_gold_on.png"), resourcesArea);
            clickIfFindIt(ImageUtil.loadResource("player/watchtower/icon_tar_on.png"), resourcesArea);


            int scroll = 10;
            for (int s = 0; s < i / 3; s++) {
                Point initialPoint = Point.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(1347, 523)); 
                robot.mouseDrag(initialPoint, 0, 10 * (i / 3));
            }
            
            Area buttonGoArea = Area.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(1225, 509), Point.of(1284, 901));

            BufferedImage buttonGo = ImageUtil.loadResource("player/watchtower/button_go.png");


            BufferedImage screen = robot.captureScreen();
            List<Point> buttons = ImageUtil.searchMultiple(buttonGo, screen, buttonGoArea, 0.1);
           
                // Click on GO Button
                robot.leftClick(buttons.get(i % 3)); // It seems that 4 buttons appear, but we use 3 
                robot.sleep(1000);

                Navigate.builder()
                        .resourceName("player/label_city.png")
                        .areaName("MAIN_LABEL_CITY")
                        .waitLimit(7500)
                        .pressEscapeWhileWaiting(true)
                        .build()
                        .ensureExistence();
                robot.sleep(2000);

                BufferedImage mine = ImageUtil.loadResource("player/watchtower/mine_silver.png");
                if (minePoint == null) {
                    screen = robot.captureScreen();
                    Area centerArea = RegionSelector.selectArea("MAP_CENTER", screen);
                    minePoint = ImageUtil.searchBestFit(new BufferedImage[] {mine}, screen, centerArea);
                }
                
                //ImageUtil.showImageAndWait(screen, Area.of(minePoint.getX(), minePoint.getY(), mine.getWidth(), mine.getHeight()));
                
                robot.mouseMove(minePoint.move(mine.getWidth() / 2, mine.getHeight() / 2));

                Point arenaCoordinate = readCoordinate();

                robot.leftClick(minePoint, mine);

                Point titleVillagePoint = Navigate.builder()
                        .resourceName("player/watchtower/title_village.png")
                        .areaName("TELESCOPE_VILLAGE_TITLE")
                        .waitLimit(10000)
                        .build()
                        .ensureExistence();
                
                Point buttonCapturePoint = Navigate.builder()
                        .resourceName("player/watchtower/button_capture.png")
                        .area(Area.of(titleVillagePoint, Point.of(969, 481), Point.of(933, 682), Point.of(1042, 724)))
                        .build().search().orElse(null);
                
                if (buttonCapturePoint != null) {
                    log.info("Mine can be captured! " + arenaCoordinate.getX() + ", " + arenaCoordinate.getY());
                }
                else {
                    log.info("Mine is busy! " + arenaCoordinate.getX() + ", " + arenaCoordinate.getY());
                }

                // Close pop up window
                robot.type(KeyEvent.VK_ESCAPE);
                robot.sleep(300);
        }

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
    
    private void clickIfFindIt(BufferedImage item, Area area) {
        
        Point point = Navigate.builder()
                .area(area)
                .searchImage(item)
                .build().search().orElse(null);
        
        if (point != null) {
            robot.leftClick(point, item);
            robot.sleep(300);
        }
    }
    
    private Optional<Point> openWatchtower() {
        Navigate activeTelescope = Navigate.builder()
                .areaName("ACTIVE_TELESCOPE")
                .resourceName("player/icon_telescope.png")
                .build();
        if (!activeTelescope.exist()) {
            System.out.println("Telescope is not activated");
            return Optional.empty();
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
        return Optional.ofNullable(titleWatchtowerPoint);
    }
}
