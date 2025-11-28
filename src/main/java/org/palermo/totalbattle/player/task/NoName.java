package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.image.BufferedImage;

public class NoName {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;
    
    private static final GameStateService gameStateService = new GameStateService();

    public NoName(Player player) {
        this.player = player;
    }

    public void evaluate() {

        Navigate iconSilver = hoverTopMenuIcon("player/top_menu/icon_silver.png");
        
        BufferedImage screen = robot.captureScreen();
        Area silverAmountArea = Area.of(iconSilver.getPoint(), Point.of(1141, 179), Point.of(1222,271), Point.of(1316, 290));
        System.out.println("Silver: " + ocr(ImageUtil.crop(screen, silverAmountArea)));

        Navigate iconCommonTar = hoverTopMenuIcon("player/top_menu/icon_common_tar.png");

        screen = robot.captureScreen();
        Area commonTarAmountArea = Area.of(iconCommonTar.getPoint(), Point.of(767, 190), Point.of(858,281), Point.of(931, 300));
        System.out.println("Tar: " + ocr(ImageUtil.crop(screen, commonTarAmountArea)));
    }
    
    private static Navigate hoverTopMenuIcon(String resourceName) {
        Navigate iconNext = Navigate.builder()
                .areaName(Area.MAIN_TOP_MENU)
                .resourceName("player/top_menu/icon_next.png")
                .build()
                .ensureExistence();

        Navigate resource = Navigate.builder()
                .areaName(Area.MAIN_TOP_MENU)
                .resourceName(resourceName)
                .build();

        while (!resource.exist()) {
            iconNext.leftClick();
        }

        resource.mouseHover();
        return resource;
    }
    
    private static String ocr(BufferedImage input) {
        BufferedImage image = ImageUtil.toGrayscale(input, new String[] {"FFF7BF"});
        image = ImageUtil.linearNormalization(image);
        image =ImageUtil.cropText(image);
        image = ImageUtil.linearNormalization(image);
        if (image.getHeight() < ImageUtil.OCR_HEIGHT) {
            image = ImageUtil.resize(image, ImageUtil.OCR_HEIGHT);
        }

        boolean manualOcr = gameStateService.getPropertyAsBoolean(GameStateService.PROPERTY_MANUAL_OCR);
        return ImageUtil.ocr(image, ImageUtil.WHITELIST_FOR_ONLY_NUMBERS, ImageUtil.PATTERN_FOR_ONLY_NUMBERS, manualOcr);
    }
}
