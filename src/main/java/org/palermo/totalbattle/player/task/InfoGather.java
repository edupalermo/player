package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.image.BufferedImage;

@Slf4j
public class InfoGather {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    private static final GameStateService gameStateService = new GameStateService();
    private static final PlayerStateService playerStateService = new PlayerStateService();

    public InfoGather(Player player) {
        this.player = player;
    }

    public void evaluate() {
        gatherCommonTarAmount();
        gatherLumberAmount();
        gatherIronAmount();
        gatherStoneAmount();
        gatherSilverAmount();

        log.info("Lumber {}, Iron {}, Stone {}", playerStateService.getState(player).getLumber(),
                playerStateService.getState(player).getIron(),
                playerStateService.getState(player).getStone());

        log.info("Tar {}, Silver {}", playerStateService.getState(player).getCommonTar(),
                playerStateService.getState(player).getSilver());
    }
    
    private void gatherCommonTarAmount() {
        Navigate iconCommonTar = selectTopMenuIcon("player/top_menu/icon_common_tar.png", true);
        BufferedImage screen = robot.captureScreen();
        Area commonTarAmountArea = Area.of(iconCommonTar.getPoint(), Point.of(767, 190), Point.of(858,281), Point.of(931, 300));
        playerStateService.getState(player).setCommonTar(Integer.parseInt(ocr(ImageUtil.crop(screen, commonTarAmountArea))));
    }

    private void gatherSilverAmount() {
        Navigate iconSilver = selectTopMenuIcon("player/top_menu/icon_silver.png", true);
        BufferedImage screen = robot.captureScreen();
        Area silverAmountArea = Area.of(iconSilver.getPoint(), Point.of(1141, 179), Point.of(1222,271), Point.of(1316, 290));
        playerStateService.getState(player).setSilver(Integer.parseInt(ocr(ImageUtil.crop(screen, silverAmountArea))));
    }

    private void gatherLumberAmount() {
        Navigate iconSilver = selectTopMenuIcon("player/top_menu/icon_lumber.png", true);
        BufferedImage screen = robot.captureScreen();
        Area area = Area.of(iconSilver.getPoint(), Point.of(764, 170), Point.of(849,261), Point.of(944, 281));
        playerStateService.getState(player).setLumber(Integer.parseInt(ocr(ImageUtil.crop(screen, area))));
    }

    private void gatherIronAmount() {
        Navigate iconSilver = selectTopMenuIcon("player/top_menu/icon_iron.png", true);
        BufferedImage screen = robot.captureScreen();
        Area area = Area.of(iconSilver.getPoint(), Point.of(859, 170), Point.of(943,261), Point.of(1035, 281));
        playerStateService.getState(player).setIron(Integer.parseInt(ocr(ImageUtil.crop(screen, area))));
    }

    private void gatherStoneAmount() {
        Navigate iconSilver = selectTopMenuIcon("player/top_menu/icon_stone.png", true);
        BufferedImage screen = robot.captureScreen();
        Area area = Area.of(iconSilver.getPoint(), Point.of(953, 170), Point.of(1046,261), Point.of(1129, 281));
        playerStateService.getState(player).setStone(Integer.parseInt(ocr(ImageUtil.crop(screen, area))));
    }

    private static Navigate selectTopMenuIcon(String resourceName, boolean hover) {
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

        if (hover) {
            resource.mouseHover();
        }
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
