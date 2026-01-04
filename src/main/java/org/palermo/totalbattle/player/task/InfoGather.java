package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.entity.PlayerEntity;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.task.shared.Resource;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.service.player.PlayerService;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;
import org.palermo.totalbattle.util.OcrUtil;
import org.palermo.totalbattle.util.WhatsappUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

@Slf4j
@Service
public class InfoGather {

    private final MyRobot robot = MyRobot.INSTANCE;

    private static final GameStateService gameStateService = new GameStateService();
 
    @Autowired
    private PlayerService playerService;

    public void evaluate(PlayerEntity playerEntity) {
        try {
            doEverything(playerEntity);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);

            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }
    
    private void doEverything(PlayerEntity playerEntity) {
        gatherCommonTarAmount(playerEntity);

        gatherLumberAmount(playerEntity);
        gatherIronAmount(playerEntity);
        gatherStoneAmount(playerEntity);
        gatherSilverAmount(playerEntity);


        if (playerEntity.getTargetLumber() > playerEntity.getLumber()) {
            robot.mouseMove(Point.of(500, 500));
            selectTopMenuIcon(Resource.LUMBER, Operation.CLICK);
            robot.mouseMove(Point.of(500, 500));
            fillToTarget(playerEntity, Resource.LUMBER);
        }

        if (playerEntity.getTargetIron() > playerEntity.getIron()) {
            robot.mouseMove(Point.of(500, 500));
            selectTopMenuIcon(Resource.IRON, Operation.CLICK);
            robot.mouseMove(Point.of(500, 500));
            fillToTarget(playerEntity, Resource.IRON);
        }

        if (playerEntity.getTargetStone() > playerEntity.getStone()) {
            robot.mouseMove(Point.of(500, 500));
            selectTopMenuIcon(Resource.STONE, Operation.CLICK);
            robot.mouseMove(Point.of(500, 500));
            fillToTarget(playerEntity, Resource.STONE);
        }
        
        //TODO: Should also fill with Silver!

        if (playerEntity.getLumber() >= playerEntity.getTargetLumber() &&
                playerEntity.getIron() >= playerEntity.getTargetIron() &&
                playerEntity.getStone() >= playerEntity.getTargetStone() &&
                shouldSendMessage(playerEntity)
        ) {
            WhatsappUtil.send(String.format("Player %s has the resources to upgrade a building", playerEntity.getPlayerName().name()));
        }

        // Fill all dragon coins... it cannot be stolen!
        selectTopMenuIcon(Resource.DRAGON_COIN, Operation.CLICK);
        robot.mouseMove(Point.of(500, 500));
        fillComplete(playerEntity, Resource.DRAGON_COIN);

        selectTopMenuIcon(Resource.SILVER, Operation.NOTHING);

        log.info("Lumber {}, Iron {}, Stone {}",
                String.format("%,d %.0f%%", playerEntity.getLumber(), (100d * (double) playerEntity.getLumber() / (double) playerEntity.getTargetLumber())),
                String.format("%,d %.0f%%", playerEntity.getIron(), (100d * (double) playerEntity.getIron() / (double) playerEntity.getTargetIron())),
                String.format("%,d %.0f%%", playerEntity.getStone(), (100d * (double) playerEntity.getStone() / (double) playerEntity.getTargetStone())));

        log.info("Tar {}, Silver {}",
                String.format("%,d", playerEntity.getCommonTar()),
                String.format("%,d %.0f%%", playerEntity.getSilver(), (100d * (double) playerEntity.getSilver() / (double) playerEntity.getSilver())));
    }
    
    private void gatherCommonTarAmount(PlayerEntity playerEntity) {
        Navigate iconCommonTar = selectTopMenuIcon(Resource.COMMON_TAR, Operation.HOVER);
        BufferedImage screen = robot.captureScreen();
        Area commonTarAmountArea = Area.of(iconCommonTar.getPoint(), Point.of(767, 190), Point.of(858,281), Point.of(931, 300));
        playerEntity.setCommonTar(ocr(ImageUtil.crop(screen, commonTarAmountArea)));
    }

    private void gatherSilverAmount(PlayerEntity playerEntity) {
        Navigate iconSilver = selectTopMenuIcon(Resource.SILVER, Operation.HOVER);
        BufferedImage screen = robot.captureScreen();
        Area silverAmountArea = Area.of(iconSilver.getPoint(), Point.of(1141, 179), Point.of(1222,271), Point.of(1316, 290));
        playerEntity.setSilver(ocr(ImageUtil.crop(screen, silverAmountArea)));
    }

    private void gatherLumberAmount(PlayerEntity playerEntity) {
        Navigate iconSilver = selectTopMenuIcon(Resource.LUMBER, Operation.HOVER);
        BufferedImage screen = robot.captureScreen();
        Area area = Area.of(iconSilver.getPoint(), Point.of(764, 170), Point.of(849,261), Point.of(944, 281));
        playerEntity.setLumber(ocr(ImageUtil.crop(screen, area)));
    }

    private void fillComplete(PlayerEntity playerEntity, Resource resource) {
        fill(playerEntity, resource, true);
    }

    private void fillToTarget(PlayerEntity playerEntity, Resource resource) {
        fill(playerEntity, resource, false);
    }


    private void fill(PlayerEntity playerEntity, Resource resource, boolean complete) {
        Navigate navigate = Navigate.builder()
                .resourceName(resource.getResource())
                .areaName("TOP_UP_SILVER_SILVER_ICON")
                .waitLimit(5000)
                .build().ensureExistence();

        Transformation transformation = Transformation.builder()
                .real(navigate.getPoint())
                .reference(Point.of(780, 399))
                .build();
        
        Navigate useButton = Navigate.builder()
                .area(transformation.transform(Point.of(1079, 534), Point.of(1234, 578)))
                .resourceName("player/button_use.png")
                .waitLimit(1000)
                .build();

        int current = 0; // Not important here

        do {
            if (useButton.leftClickIfExists()) {
                
                // Subsequent button
                Navigate.builder()
                        .areaName("TOP_UP_SILVER_SLIDE_SUBSEQUENT_USE_BUTTON")
                        .resourceName("player/button_use.png")
                        .build()
                        .leftClick();

                current = ocrWithMultiplier(robot.captureScreen(transformation.transform(Point.of(788, 424), Point.of(828, 436))));
                setResourceAmount(playerEntity, resource, current);
            }

        } while ((complete || getTargetAmount(playerEntity, resource) > getResourceAmount(playerEntity, resource))
                && useButton.searchAgain().isPresent());

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }

    private long getResourceAmount(PlayerEntity playerEntity, Resource resource) {
        switch (resource) {
            case LUMBER:
                return playerEntity.getLumber();
            case IRON:
                return playerEntity.getIron();
            case STONE:
                return playerEntity.getStone();
            case SILVER:
                return playerEntity.getSilver();
            case DRAGON_COIN:
                return 0; // Not relevant
            default:
                throw new RuntimeException("Not implemented");
        }
    }

    private long getTargetAmount(PlayerEntity playerEntity, Resource resource) {
        switch (resource) {
            case LUMBER:
                return playerEntity.getTargetLumber();
            case IRON:
                return playerEntity.getTargetIron();
            case STONE:
                return playerEntity.getTargetStone();
            case SILVER:
                return playerEntity.getTargetSilver();
            case DRAGON_COIN:
                return 0; // Not relevant
            default:
                throw new RuntimeException("Not implemented");
        }
    }

    private void setResourceAmount(PlayerEntity playerEntity, Resource resource, int amount) {
        switch (resource) {
            case LUMBER:
                playerEntity.setLumber(amount);
                return;
            case IRON:
                playerEntity.setIron(amount);
                return;
            case STONE:
                playerEntity.setStone(amount);
                return;
            case SILVER:
                playerEntity.setSilver(amount);
                return;
            case DRAGON_COIN:
                // Do nothing!
                return;
            default:
                throw new RuntimeException("Not implemented");
        }
    }


    private void gatherIronAmount(PlayerEntity playerEntity) {
        Navigate iconSilver = selectTopMenuIcon(Resource.IRON, Operation.HOVER);
        BufferedImage screen = robot.captureScreen();
        Area area = Area.of(iconSilver.getPoint(), Point.of(859, 170), Point.of(943,261), Point.of(1035, 281));
        playerEntity.setIron(ocr(ImageUtil.crop(screen, area)));
    }

    private void gatherStoneAmount(PlayerEntity playerEntity) {
        Navigate iconSilver = selectTopMenuIcon(Resource.STONE, Operation.HOVER);
        BufferedImage screen = robot.captureScreen();
        Area area = Area.of(iconSilver.getPoint(), Point.of(953, 170), Point.of(1046,261), Point.of(1129, 281));
        playerEntity.setStone(ocr(ImageUtil.crop(screen, area)));
    }

    private enum Operation {
        HOVER, CLICK, NOTHING
    }

    private static Navigate selectTopMenuIcon(Resource resource, Operation operation) {
        Navigate iconNext = Navigate.builder()
                .areaName(Area.MAIN_TOP_MENU)
                .resourceName("player/top_menu/icon_next.png")
                .build()
                .ensureExistence();

        Navigate resourceNavigation = Navigate.builder()
                .areaName(Area.MAIN_TOP_MENU)
                .resourceName(resource.getIcon())
                .build();

        while (!resourceNavigation.exist()) {
            iconNext.leftClick();
        }

        switch (operation) {
            case HOVER:
                resourceNavigation.mouseHover();
                break;
            case CLICK:
                resourceNavigation.leftClick();
                break;
        }
        return resourceNavigation;
    }
    
    private static int ocr(BufferedImage input) {
        BufferedImage image = ImageUtil.toGrayscale(input, new String[] {"FFF7BF"});
        image = ImageUtil.linearNormalization(image);
        image =ImageUtil.cropText(image);
        image = ImageUtil.linearNormalization(image);
        if (image.getHeight() < OcrUtil.OCR_HEIGHT) {
            image = ImageUtil.resize(image, OcrUtil.OCR_HEIGHT);
        }

        boolean manualOcr = gameStateService.getPropertyAsBoolean(GameStateService.PROPERTY_MANUAL_OCR);
        String asString = OcrUtil.ocr(image, OcrUtil.WHITELIST_FOR_NUMBERS_WITH_THOUSAND_SEPARATOR, OcrUtil.PATTERN_FOR_NUMBERS_WITH_THOUSAND_SEPARATOR, manualOcr);
        asString = asString.replace(",", "");
        return Integer.parseInt(asString);
    }

    private static int ocrWithMultiplier(BufferedImage input) {
        BufferedImage image = ImageUtil.toGrayscale(input, new String[] {"FFF7BF"});
        image = ImageUtil.linearNormalization(image);
        image =ImageUtil.cropText(image);
        image = ImageUtil.linearNormalization(image);
        if (image.getHeight() < OcrUtil.OCR_HEIGHT) {
            image = ImageUtil.resize(image, OcrUtil.OCR_HEIGHT);
        }

        boolean manualOcr = gameStateService.getPropertyAsBoolean(GameStateService.PROPERTY_MANUAL_OCR);
        String asString = OcrUtil.ocr(image, OcrUtil.WHITELIST_FOR_NUMBERS_AND_MULTIPLIER, OcrUtil.PATTERN_FOR_NUMBERS_WITH_MULTIPLIER, manualOcr);

        return toNumberWithMultiplier(asString);
        
    }
    
    private static int toNumberWithMultiplier(String input) {
        int multiplier = 1;
        
        switch (input.charAt(input.length() - 1)) {
            case 'K':
                multiplier = 1_000;
                input = input.substring(0, input.length() - 1);
                break;
            case 'M':
                multiplier = 1_000_000;
                input = input.substring(0, input.length() - 1);
                break;
        }

        return (int) Math.round(multiplier * Double.parseDouble(input));
    }
    
    private boolean shouldSendMessage(PlayerEntity playerEntity) {
        List<PlayerEntity> list = playerService.findAll();
        for (PlayerEntity it: list) {
            if (it.getPriority() >= playerEntity.getPriority()) { // will also exclude the current player
                continue;
            }
            if (it.getTargetLumber() < it.getLumber() ||
                    it.getTargetIron() < it.getIron() ||
                    it.getTargetStone() < it.getStone()) {
                return false;
            }
        }
        return true;
    }
}
