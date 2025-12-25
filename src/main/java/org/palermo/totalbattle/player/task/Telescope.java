package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.state.location.Arena;
import org.palermo.totalbattle.player.state.location.Citadel;
import org.palermo.totalbattle.player.state.location.Crypt;
import org.palermo.totalbattle.player.state.location.Mine;
import org.palermo.totalbattle.player.state.location.MineType;
import org.palermo.totalbattle.player.state.location.Rarity;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class Telescope {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;
    
    private final GameStateService gameStateService = new GameStateService();
    
    private final int MINE_COUNT_TARGET = 1;

    public Telescope(Player player) {
        this.player = player;
    }
    
    public void findArena() {
        if (!gameStateService.getLocation(Arena.class).isEmpty()) {
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

        // for (int i = 0; i < 2; i++) {
            findArenaByIndex(activeTelescope, 0);
        //}
        
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
        
        log.info("Watchtower title found at {} {}", titleWatchtower.getPoint().getX(), titleWatchtower.getPoint().getY());

        Navigate labelCryptsAndArenas = Navigate.builder()
                .areaName("WATCHTOWER_LEFT_TAB_CRYPTS_AND_ARENAS_LABEL")
                .resourceName("player/watchtower/label_crypts_and_arenas.png")
                .waitLimit(Duration.ofSeconds(3).toMillis())
                .build();

        if (!labelCryptsAndArenas.exist()) {
            throw new RuntimeException("Could not find crypts and arenas label");
        }
        labelCryptsAndArenas.leftClick();

        Transformation transformation = Transformation.builder()
                .real(titleWatchtowerPoint)
                .reference(Point.of(946, 323))
                .build();

        configureCryptsAndArenasMenu(transformation, new boolean[] {false, false, false, true, false});
        configureCryptsAndArenasSlider(transformation, 5);


        // Here we click on the GO
        robot.leftClick(Point.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(1249, 591 + (index * 100))));
        
        robot.sleep(2000);
        robot.type(KeyEvent.VK_ESCAPE); // Sometimes the bonus sale is shown
        robot.sleep(3000);
        
        NavigationUtil.zoomInIfNeeded();

        BufferedImage arena = ImageUtil.loadResource("player/arena/arena_type_i.png");
        Point arenaPoint = ArenaUtil.identifyCenterArena();
        
        robot.mouseMove(arenaPoint.move(arena.getWidth() / 2, arena.getHeight() / 2));
        robot.sleep(500);

        Point arenaCoordinate = readCoordinate();
        log.info("Arena found at {}, {}", arenaCoordinate.getX(), arenaCoordinate.getY());
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
        if (image.getHeight() < 100) {
            image = ImageUtil.resize(image, 100);
        }
        boolean manualOcr = gameStateService.getPropertyAsBoolean(GameStateService.PROPERTY_MANUAL_OCR);
        String quantityAsString = ImageUtil.ocr(image, ImageUtil.WHITELIST_FOR_ONLY_NUMBERS, ImageUtil.PATTERN_FOR_ONLY_NUMBERS, manualOcr);
        return Integer.parseInt(quantityAsString);
    }
    
    public void findSilverMines() {
        
        if (gameStateService.countMines(MineType.SILVER) >= MINE_COUNT_TARGET) {
            log.info("No need for look for new mines");
            return;
        }

        Point minePoint = null;
        
        int mainLoopCount = 0;

        Navigate activeTelescope = Navigate.builder()
                .areaName("ACTIVE_TELESCOPE")
                .resourceName("player/icon_telescope.png")
                .build();
        
        if (!activeTelescope.exist()) {
            log.info("Telescope is not activated");
            return;
        }

        do {
            Point titleWatchtowerPoint = openWatchtower(activeTelescope);

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


            final int SCROLL_PIXELS = 8;
            Point initialPoint = Point.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(1347, 523));
            robot.leftClick(initialPoint);
            robot.sleep(150);
            if (mainLoopCount / 3 > 0) {
                robot.mouseDrag(initialPoint, 0, SCROLL_PIXELS * (mainLoopCount / 3));
                robot.sleep(150);
            }
            
            Area buttonGoArea = Area.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(1225, 509), Point.of(1284, 901));

            BufferedImage buttonGo = ImageUtil.loadResource("player/watchtower/button_go.png");


            BufferedImage screen = robot.captureScreen();
            List<Point> buttons = ImageUtil.searchMultiple(buttonGo, screen, buttonGoArea, 0.1);
           
            // Click on GO Button
            robot.leftClick(buttons.get(mainLoopCount % 3)); // It seems that 4 buttons appear, but we use 3 
            robot.sleep(1000);

            Navigate.builder()
                    .resourceName("player/label_city.png")
                    .areaName("MAIN_LABEL_CITY")
                    .waitLimit(7500)
                    .pressEscapeWhileWaiting(true)
                    .build()
                    .ensureExistence();
            robot.sleep(2000);
            
            NavigationUtil.zoomInIfNeeded();
        
            if (minePoint == null) {
                BufferedImage mine = ImageUtil.loadResource("player/watchtower/mine_silver.png");
                minePoint = NavigationUtil.spotSilverMinePositionPointInTheCenter()
                        .centralize(mine)
                        .move(0, -6);
            }

            robot.mouseMove(minePoint);
            Point arenaCoordinate = readCoordinate();

            robot.leftClick(minePoint);

            Point titleVillagePoint = Navigate.builder()
                    .resourceName("player/watchtower/title_village.png")
                    .areaName("TELESCOPE_VILLAGE_TITLE")
                    .waitLimit(10000)
                    .build()
                    .getPoint();
            
            Point buttonCapturePoint = Navigate.builder()
                    .resourceName("player/watchtower/button_capture.png")
                    .area(Area.of(titleVillagePoint, Point.of(969, 481), Point.of(933, 682), Point.of(1042, 724)))
                    .build().search().orElse(null);
            
            if (buttonCapturePoint == null) {
                log.info("Mine is busy! " + arenaCoordinate.getX() + ", " + arenaCoordinate.getY());

                mainLoopCount = mainLoopCount + 1;

                // Close pop up window
                robot.type(KeyEvent.VK_ESCAPE);
                robot.sleep(300);
                continue;
            }
                

            // Close pop up window
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);

            if (NavigationUtil.belongsToAnotherClan(minePoint)) {
                log.info("Mine belong to another clan");
                mainLoopCount = mainLoopCount + 1;
                continue;
            }


            log.info("Mine can be captured! {} {}", arenaCoordinate.getX(), arenaCoordinate.getY());
            gameStateService.add(Mine.builder()
                    .position(arenaCoordinate)
                    .type(MineType.SILVER)
                    .build());

            mainLoopCount = mainLoopCount + 1;
            
                
        } while (gameStateService.countMines(MineType.SILVER) < 3);

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
    
    private boolean isTelescopeActivated() {
        Navigate activeTelescope = Navigate.builder()
                .areaName("ACTIVE_TELESCOPE")
                .resourceName("player/icon_telescope.png")
                .waitLimit(1500)
                .build();
        return activeTelescope.exist();
    }
    
    private Point openWatchtower(Navigate activeTelescope) {
        if (!activeTelescope.exist()) {
            throw new RuntimeException("Telescope is not activated");
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
        return titleWatchtowerPoint;
    }


    // Used for Monsters and Citadels
    private Map<Integer, Point> leftSlider = new HashMap<>();
    private Map<Integer, Point> rightSlider = new HashMap<>();
    {
        leftSlider.put(1, Point.of(893 + 16, 551));
        leftSlider.put(10, Point.of(969 + 16, 551));
        leftSlider.put(15, Point.of(1006 + 16, 551));
        
        
        rightSlider.put(10, Point.of(1003 + 16, 551));
        rightSlider.put(15, Point.of(1040 + 16, 551));
        rightSlider.put(35, Point.of(1290 + 19, 551));
        
    }
    
    public void findCitadels() {
        Navigate activeTelescope = Navigate.builder()
                .areaName("ACTIVE_TELESCOPE")
                .resourceName("player/icon_telescope.png")
                .build();
        
        if (!activeTelescope.exist()) {
            log.info("Telescope is not activated");
            return;
        }
        
        handleCitadel(10, activeTelescope);
        handleCitadel(15, activeTelescope);
    }
    
    private void handleCitadel(int citadelLevel, Navigate activeTelescope) {
        try {
            int count = gameStateService.countCitadels(citadelLevel);
            if (count == 0) {
                findCitadel(0, citadelLevel, activeTelescope);
            }
        }
        catch(Exception e) {
            log.error(e.getMessage() ,e);
            robot.type(KeyEvent.VK_ESCAPE); // Sometimes the bonus sale is shown
            robot.sleep(3000);
        }
    }
    
    private void findCitadel(int index, int citadelLevel, Navigate activeTelescope) {
        Point titleWatchtowerPoint = openWatchtower(activeTelescope);

        Transformation transformation = Transformation.builder()
                .real(titleWatchtowerPoint)
                .reference(Point.of(946, 323))
                .build();

        // Click on Monsters let tab
        robot.leftClick(transformation.transform(Point.of(715, 497)));
        robot.sleep(500);

        selectMonsters(titleWatchtowerPoint, new boolean[] {false, false, false, true, false, false});
        selectRarity(titleWatchtowerPoint, new boolean[] {false, false, false, true});

        if (index == 0) {
            BufferedImage screen = robot.captureScreen();
            Area sliderArea = transformation.transform(Point.of(882, 536), Point.of(1338, 565));
            BufferedImage slider = ImageUtil.loadResource("player/watchtower/monsters/slider.png");

            List<Point> sliders = ImageUtil.searchMultiple(slider, screen, sliderArea, 0.07);
            System.out.println("Sliders: " + sliders.size());
            sliders = simplify(slider, sliders);
            System.out.println("Sliders: " + sliders.size());
            
            if (sliders.size() != 2) {
                throw new RuntimeException("Fail to get sliders");
            }

            final int shift = 7;

            boolean shouldMoveLeft = Math.abs(sliders.get(0).centralize(slider).getX() - transformation.transform(leftSlider.get(citadelLevel)).getX()) >= 2;
            boolean shouldMoveRight = Math.abs(sliders.get(1).centralize(slider).getX() - transformation.transform(rightSlider.get(citadelLevel)).getX()) >= 2;

            if (shouldMoveLeft) {
                robot.leftClick(sliders.get(0), slider);
                robot.sleep(300);
                robot.mouseDrag(sliders.get(0).centralize(slider), transformation.transform(leftSlider.get(1)).move(-shift, 0));
            }

            if (shouldMoveRight) {
                int sign = (int) Math.signum(caRightSlider.get(citadelLevel).getX() - sliders.get(1).getX());
                robot.leftClick(sliders.get(1), slider);
                robot.sleep(300);
                robot.mouseDrag(sliders.get(1).centralize(slider), transformation.transform(rightSlider.get(citadelLevel)).move(sign * shift, 0));
            }

            if (shouldMoveLeft) {
                robot.leftClick(sliders.get(0), slider);
                robot.sleep(300);
                robot.mouseDrag(transformation.transform(leftSlider.get(1)), transformation.transform(leftSlider.get(citadelLevel)).move(shift, 0));
            }

            /*
            if (shouldMoveRight) {
                robot.leftClick(sliders.get(1), slider);
                robot.sleep(300);
                robot.mouseDrag(transformation.transform(rightSlider.get(35)), transformation.transform(rightSlider.get(10)).move(-shift, 0));
            }
            */
            robot.sleep(350);
        }

        final int SCROLL_PIXELS = 53;
        Point initialPoint = Point.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(1347, 579));
        robot.leftClick(initialPoint);
        robot.sleep(150);
        if (index / 3 > 0) {
            robot.mouseDrag(initialPoint, 0, SCROLL_PIXELS * (index / 3));
            robot.sleep(150);
        }

        Area buttonGoArea = Area.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(1225, 509), Point.of(1284, 901));
        BufferedImage buttonGo = ImageUtil.loadResource("player/watchtower/button_go.png");
        BufferedImage screen = robot.captureScreen();
        List<Point> buttons = ImageUtil.searchMultiple(buttonGo, screen, buttonGoArea, 0.1);
        
        if (buttons.size() < 3) {
            throw new RuntimeException("Found not buttons!");
        } 

        robot.leftClick(buttons.get(index % 3), buttonGo); //Click GO Button
        robot.sleep(2000);
        
        
        
        robot.type(KeyEvent.VK_ESCAPE); // Sometimes the bonus sale is shown
        robot.sleep(3000);

        Point citadelPoint = NavigationUtil.identifyCenterCitadel();

        robot.mouseMove(citadelPoint);
        robot.sleep(500);

        Point mapCoordinates = readCoordinate();

        log.info("Citadel found at {}, {}", mapCoordinates.getX(), mapCoordinates.getY());

        gameStateService.add(Citadel.builder()
                .position(mapCoordinates)
                .level(citadelLevel)
                .build());
    }
    
    private List<Point> simplify(BufferedImage image, List<Point> points) {
        List<Point> answer = new ArrayList<>();
        
        for (Point point : points) {
            boolean intersect = false;
            
            for (Point included: answer) {
                if (point.getX() >= included.getX() && 
                        point.getX() < included.getX() + image.getWidth()) {
                    intersect = true;
                    break;
                }
            }
            
            if (!intersect) {
                answer.add(point);
            }
        }
        
        return answer;
    }
    
    public void selectMonsters(Point titleWatchtowerPoint, boolean[] enabled) {
        Area monsterTypeArea = Area.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(825, 426), Point.of(1334, 465));

        List<BufferedImage> monsterIcons = new ArrayList<>();
        monsterIcons.add(ImageUtil.loadResource("player/watchtower/monsters/icon_barbarians_on.png"));
        monsterIcons.add(ImageUtil.loadResource("player/watchtower/monsters/icon_inferno_on.png"));
        monsterIcons.add(ImageUtil.loadResource("player/watchtower/monsters/icon_undead_on.png"));
        monsterIcons.add(ImageUtil.loadResource("player/watchtower/monsters/icon_elves_on.png"));
        monsterIcons.add(ImageUtil.loadResource("player/watchtower/monsters/icon_cursed_on.png"));
        monsterIcons.add(ImageUtil.loadResource("player/watchtower/monsters/icon_others_on.png"));
        
        List<Point> iconPoints = new ArrayList<>();
        iconPoints.add(Point.of(846, 449));
        iconPoints.add(Point.of(943, 449));
        iconPoints.add(Point.of(1031, 449));
        iconPoints.add(Point.of(1122, 449));
        iconPoints.add(Point.of(1215, 449));
        iconPoints.add(Point.of(1306, 449));
        
        
        for (int i = 0; i < enabled.length; i++) {
            Navigate item = Navigate.builder()
                    .area(monsterTypeArea)
                    .searchImage(monsterIcons.get(i))
                    .build();           
            if (item.exist() && !enabled[i] ||
                    !item.exist() && enabled[i]) {
                robot.leftClick(iconPoints.get(i));
                robot.sleep(150);
            }
        }
    }
    
    private void selectRarity(Point titleWatchtowerPoint, boolean[] enabled) {

        for (int i = 0; i < 4; i++) {
            Area flagArea = Area.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(833 + (i * 129), 494), Point.of(852 + (i * 129), 515));
            Navigate buttonOn = Navigate.builder()
                    .area(flagArea)
                    .resourceName("player/watchtower/button_on.png")
                    .build();
            if ((!enabled[i] && buttonOn.exist()) ||
                    (enabled[i] && !buttonOn.exist())) {
                robot.leftClick(flagArea);
                robot.sleep(200);
            }
        }
    }
    
    public void findCrypts() {
        Navigate activeTelescope = Navigate.builder()
                .areaName("ACTIVE_TELESCOPE")
                .resourceName("player/icon_telescope.png")
                .build();
        
        if (!activeTelescope.exist()) {
            log.info("Telescope is not activated");
            return;
        }

        if (gameStateService.getCrypt(10).isEmpty()) {
            handleCrypt(10, activeTelescope);
        }

        if (gameStateService.getCrypt(15).isEmpty()) {
            handleCrypt(15, activeTelescope);
        }
    }
    
    private void handleCrypt(int cryptLevel, Navigate activeTelescope) {
        Point titleWatchtowerPoint = openWatchtower(activeTelescope);
        robot.sleep(1000);

        // Click on Crypt and Arenas let tab
        robot.leftClick(Point.of(titleWatchtowerPoint, Point.of(946, 323), Point.of(715, 556)));
        robot.sleep(1000);

        Transformation transformation = Transformation.builder()
                .real(titleWatchtowerPoint)
                .reference(Point.of(946, 323))
                .build();

        configureCryptsAndArenasMenu(transformation, new boolean[] {true, false, false, false, false});
        configureCryptsAndArenasSlider(transformation, cryptLevel);

        Area area = transformation.transform(Point.of(832, 529), Point.of(888, 808));

        boolean found = false;
        int counter = 0;


        BufferedImage crypt = ImageUtil.loadResource("player/crypts/crypt_stone_range.png");


        while (!found) {

            robot.leftClick(Point.of(1347, 535));
            robot.sleep(300);
            if (counter > 0) {
                robot.mouseDrag(Point.of(1347, 535), 0, counter * 5);
            }

            BufferedImage screen = robot.captureScreen();
            List<Point> cryptPoints = ImageUtil.searchMultiple(crypt, screen, area, 0.07);

            if (cryptPoints.size() > 0) {
                // Here we click on the GO
                robot.leftClick(cryptPoints.get(0).move(400, 31));

                robot.sleep(2000);
                robot.type(KeyEvent.VK_ESCAPE); // Sometimes the bonus sale is shown
                robot.sleep(3000);

                NavigationUtil.zoomInIfNeeded();


                BufferedImage cryptMap = ImageUtil.loadResource("player/crypts/crypt_stone_range_map.png");
                Point cryptPoint = ArenaUtil.identifyCenter(cryptMap);

                robot.mouseMove(cryptPoint.move(cryptMap.getWidth() / 2, cryptMap.getHeight() / 2));
                robot.sleep(500);

                Point arenaCoordinate = readCoordinate();
                log.info("Crypt found at {}, {}", arenaCoordinate.getX(), arenaCoordinate.getY());

                gameStateService.add(Crypt.builder()
                        .position(arenaCoordinate)
                        .rarity(Rarity.COMMON)
                        .level(cryptLevel)
                        .build());

                found = true;
            }
            counter = counter + 1;

            if (counter >= 10) {
                found = true; // Just to stop loop!
            }
        }


        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
    
    private void configureCryptsAndArenasMenu(Transformation transformation, boolean enabled[]) {
        List<Point> topButtons = new ArrayList<>();
        topButtons.add(Point.of(833,427)); // Common
        topButtons.add(Point.of(962,427)); // Rare
        topButtons.add(Point.of(1091,427)); // Epic
        topButtons.add(Point.of(1219,427)); // Arenas
        topButtons.add(Point.of(833,453)); // Others

        for (int i = 0; i < topButtons.size(); i++) {

            Point topButton = topButtons.get(i);

            Area area = transformation.transform(topButton, topButton.move(28, 17)); 
            Navigate navigate = Navigate.builder()
                    .area(area)
                    .resourceName("player/watchtower/button_on.png")
                    .build();

            if (!enabled[i] && navigate.exist()) {
                navigate.leftClick();
                robot.sleep(300);
            }
            else if (enabled[i] && !navigate.exist()) {
                robot.leftClick(transformation.transform(topButton), area);
                robot.sleep(300);
            }
        }
        robot.sleep(1000);
    }

    private Map<Integer, Point> caLeftSlider = new HashMap<>(); // Crypts and Arena
    private Map<Integer, Point> caRightSlider = new HashMap<>();
    {
        caLeftSlider.put(1, Point.of(893 + 16, 508));
        caLeftSlider.put(5, Point.of(934 + 16, 508));
        caLeftSlider.put(10, Point.of(989 + 16, 508));
        caLeftSlider.put(15, Point.of(1044 + 16, 508));
        
        caRightSlider.put(5, Point.of(964 + 16, 508));
        caRightSlider.put(10, Point.of(1023 + 16, 508));
        caRightSlider.put(15, Point.of(1078 + 16, 508));
        caRightSlider.put(35, Point.of(1290 + 19, 508));

    }


    private void configureCryptsAndArenasSlider(Transformation transformation, int level) {
        BufferedImage screen = robot.captureScreen();
        Area sliderArea = transformation.transform(Point.of(878, 496), Point.of(1338, 518));
        BufferedImage slider = ImageUtil.loadResource("player/watchtower/monsters/slider.png");

        List<Point> sliders = ImageUtil.searchMultiple(slider, screen, sliderArea, 0.07);
        sliders = simplify(slider, sliders);

        if (sliders.size() != 2) {
            throw new RuntimeException("Fail to get sliders");
        }

        final int shift = 6;

        boolean shouldMoveLeft = Math.abs(sliders.get(0).centralize(slider).getX() - transformation.transform(caLeftSlider.get(level)).getX()) >= 5;
        boolean shouldMoveRight = Math.abs(sliders.get(1).centralize(slider).getX() - transformation.transform(caRightSlider.get(level)).getX()) >= 5;

        if (shouldMoveLeft) {
            robot.leftClick(sliders.get(0), slider);
            robot.mouseDrag(sliders.get(0).centralize(slider), transformation.transform(caLeftSlider.get(1)).move(-shift, 0));
        }

        if (shouldMoveRight) {
            int sign = (int) Math.signum(caRightSlider.get(level).getX() - sliders.get(1).getX());
            robot.leftClick(sliders.get(1), slider);
            robot.mouseDrag(sliders.get(1).centralize(slider), transformation.transform(caRightSlider.get(level)).move(sign * shift, 0));
        }

        if (shouldMoveLeft) {
            robot.leftClick(sliders.get(0), slider);
            robot.mouseDrag(transformation.transform(caLeftSlider.get(1)), transformation.transform(caLeftSlider.get(level)).move(shift, 0));
        }

        /*
        if (shouldMoveRight) {
            robot.leftClick(sliders.get(1), slider);
            robot.mouseDrag(transformation.transform(caRightSlider.get(35)), transformation.transform(caRightSlider.get(level)).move(-shift, 0));
        }
         */
        robot.sleep(1000);
    }

}
