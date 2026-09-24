package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;
import org.palermo.totalbattle.util.OcrUtil;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AttackArena {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;
    
    public AttackArena(Player player) {
        this.player = player;
    }

    public void evaluate() {
        try {
            attack();            
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        finally {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }
    
    private void attack() {
        Navigate activeTelescope = Navigate.builder()
                .areaName("ACTIVE_TELESCOPE")
                .resourceName("player/icon_telescope.png")
                .build();

        if (!activeTelescope.exist()) {
            log.info("Telescope is not activated");
            return;
        }

        activeTelescope.leftClick();
        
        playWatchtowerPopUp();
    }
    
    public void playWatchtowerPopUp() {
        
        Navigate titleWatchtower = Navigate.builder()
                .areaName("WATCHTOWER_TITLE")
                .resourceName("player/watchtower/title_watchtower.png")
                .waitLimit(Duration.ofSeconds(3).toMillis())
                .build()
                .ensureExistence();

        log.info("Watchtower title found at {} {}", titleWatchtower.getPoint().getX(), titleWatchtower.getPoint().getY());

        Navigate labelCryptsAndArenas = Navigate.builder()
                // .areaName("WATCHTOWER_LEFT_TAB_CRYPTS_AND_ARENAS_LABEL")
                .resourceName("player/watchtower/label_crypts_and_arenas.png")
                .waitLimit(Duration.ofSeconds(3).toMillis())
            //    .comparationLimit(0.3)
                .build();

        if (labelCryptsAndArenas.exist()) {
            labelCryptsAndArenas.leftClick();
        }

        Transformation transformation = Transformation.builder()
                .real(titleWatchtower.getPoint())
                .reference(Point.of(946, 323))
                .build();

        configureCryptsAndArenasMenu(transformation, new boolean[] {false, false, false, true, false});
        configureCryptsAndArenasSlider(transformation, 5);

        findArenaByIndex(transformation, 0);
    }

    private void findArenaByIndex(Transformation transformation, int index) {
        // Here we click on the GO
        robot.leftClick(transformation.transform(Point.of(1249, 591 + (index * 100))));

        robot.sleep(2000);
        robot.type(KeyEvent.VK_ESCAPE); // Sometimes the bonus sale is shown
        robot.sleep(3000);

        NavigationUtil.zoomInIfNeeded();
        
        
        Point centerPoint = getCenterPoint();

        // robot.mouseMove(centerPoint);
        robot.leftClick(centerPoint);
        robot.sleep(500);

        playArenaPopUp();
    }
    
    private void playArenaPopUp() {
        
        Navigate labelArena = Navigate.builder()
                .resourceName("player/label_arena.png")
                .area(Area.fromTwoPoints(896, 305, 1034, 338))
                .build()
                .ensureExistence();
        
        Transformation transform = Transformation.builder()
                .reference(Point.of(971, 322))
                .real(labelArena.getPoint())
                .build();
        
        Navigate checkmark = Navigate.builder()
                .resourceName("player/icon_checkmark.png")
                .area(transform.transform(Point.of(865, 705), Point.of(901, 739)))
                .build();
        
        if (!checkmark.exist()) {
            log.info("Hero is not available to fight in a Arena");
            robot.type(KeyEvent.VK_ESCAPE);
        }
        else {
            // Click Fight
            robot.leftClick(transform.transform(Point.of(1145, 865)));
        }
    }
    
    private static Point cachedCenterPoint = null;
    
    private Point getCenterPoint() {
        if (cachedCenterPoint != null) {
            return cachedCenterPoint;
        }
        
        Navigate.builder()
                .resourceName("player/icon_magnifier.png")
                .areaName(Area.MAP_MAGNIFIER)
                .waitLimit(10000)
                .build().leftClick();

        // Wait GO Button to appear (it will not click on it)
        BufferedImage buttonGo = ImageUtil.loadResource("player/button_go.png");
        Point buttonGoPoint = Navigate.builder()
                .searchImage(buttonGo)
                .waitLimit(5000)
                .build()
                .ensureExistence()
                .getPoint();

        cachedCenterPoint = buttonGoPoint.move(buttonGo.getWidth() / 2, buttonGo.getWidth() / 2).move(0, -12);

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);

        return cachedCenterPoint;
    }
    
    private void old_attack() {
        /*
        Point arenaLocation = gameStateService
                .getLocation(Arena.class)
                .stream()
                .map(Arena::getPosition)
                .findFirst()
                .orElse(null);
         */
        Point arenaLocation = null;
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
            // gameStateService.removeLocationAt(arenaLocation);
            // Remove Arena?
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
        String quantityAsString = OcrUtil.ocr(image, OcrUtil.WHITELIST_FOR_ONLY_NUMBERS, OcrUtil.PATTERN_FOR_ONLY_NUMBERS);
        return Integer.parseInt(quantityAsString);
    }
}

