package org.palermo.totalbattle.player.task;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.player.task.shared.Resource;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;
import org.palermo.totalbattle.util.OcrUtil;
import org.palermo.totalbattle.util.WhatsappUtil;

import java.awt.event.KeyEvent;
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
        try {
            doEverything();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);

            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }
    
    private void doEverything() {
        gatherCommonTarAmount();

        gatherLumberAmount();
        gatherIronAmount();
        gatherStoneAmount();
        gatherSilverAmount();


        if (playerStateService.getState(player).getResourcesTarget() != null) {
            if (playerStateService.getState(player).getResourcesTarget().getLumber() > playerStateService.getState(player).getLumber()) {
                robot.mouseMove(Point.of(500, 500));
                selectTopMenuIcon(Resource.LUMBER, Operation.CLICK);
                robot.mouseMove(Point.of(500, 500));
                fillToTarget(Resource.LUMBER);
            }

            if (playerStateService.getState(player).getResourcesTarget().getIron() > playerStateService.getState(player).getIron()) {
                robot.mouseMove(Point.of(500, 500));
                selectTopMenuIcon(Resource.IRON, Operation.CLICK);
                robot.mouseMove(Point.of(500, 500));
                fillToTarget(Resource.IRON);
            }

            if (playerStateService.getState(player).getResourcesTarget().getStone() > playerStateService.getState(player).getStone()) {
                robot.mouseMove(Point.of(500, 500));
                selectTopMenuIcon(Resource.STONE, Operation.CLICK);
                robot.mouseMove(Point.of(500, 500));
                fillToTarget(Resource.STONE);
            }

            if (playerStateService.getState(player).getLumber() >= playerStateService.getState(player).getResourcesTarget().getLumber() &&
                    playerStateService.getState(player).getIron() >= playerStateService.getState(player).getResourcesTarget().getIron() &&
                    playerStateService.getState(player).getStone() >= playerStateService.getState(player).getResourcesTarget().getStone() &&
                    shouldSendMessage()
            ) {
                WhatsappUtil.send(String.format("Player %s has the resources to upgrade a building", player.getName()));
            }
        }

        // Fill all dragon coins... it cannot be stolen!
        selectTopMenuIcon(Resource.DRAGON_COIN, Operation.CLICK);
        robot.mouseMove(Point.of(500, 500));
        fillComplete(Resource.DRAGON_COIN);

        selectTopMenuIcon(Resource.SILVER, Operation.NOTHING);

        if (playerStateService.getState(player).getResourcesTarget() != null) {
            log.info("Lumber {}, Iron {}, Stone {}",
                    String.format("%,d %.0f%%", playerStateService.getState(player).getLumber(), (100d * (double) playerStateService.getState(player).getLumber() / (double) playerStateService.getState(player).getResourcesTarget().getLumber())),
                    String.format("%,d %.0f%%", playerStateService.getState(player).getIron(), (100d * (double) playerStateService.getState(player).getIron() / (double) playerStateService.getState(player).getResourcesTarget().getIron())),
                    String.format("%,d %.0f%%", playerStateService.getState(player).getStone(), (100d * (double) playerStateService.getState(player).getStone() / (double) playerStateService.getState(player).getResourcesTarget().getStone())));

            log.info("Tar {}, Silver {}",
                    String.format("%,d", playerStateService.getState(player).getCommonTar()),
                    String.format("%,d  %.0f%%", playerStateService.getState(player).getSilver(), (100d * (double) playerStateService.getState(player).getSilver() / (double) playerStateService.getState(player).getResourcesTarget().getSilver())));
        }
        else {
            log.info("Lumber {}, Iron {}, Stone {}",
                    String.format("%,d", playerStateService.getState(player).getLumber()),
                    String.format("%,d", playerStateService.getState(player).getIron()),
                    String.format("%,d", playerStateService.getState(player).getStone()));

            log.info("Tar {}, Silver {}",
                    String.format("%,d", playerStateService.getState(player).getCommonTar()),
                    String.format("%,d", playerStateService.getState(player).getSilver()));
            
        }
    }
    
    private void gatherCommonTarAmount() {
        Navigate iconCommonTar = selectTopMenuIcon(Resource.COMMON_TAR, Operation.HOVER);
        BufferedImage screen = robot.captureScreen();
        Area commonTarAmountArea = Area.of(iconCommonTar.getPoint(), Point.of(767, 190), Point.of(858,281), Point.of(931, 300));
        playerStateService.getState(player).setCommonTar(ocr(ImageUtil.crop(screen, commonTarAmountArea)));
    }

    private void gatherSilverAmount() {
        Navigate iconSilver = selectTopMenuIcon(Resource.SILVER, Operation.HOVER);
        BufferedImage screen = robot.captureScreen();
        Area silverAmountArea = Area.of(iconSilver.getPoint(), Point.of(1141, 179), Point.of(1222,271), Point.of(1316, 290));
        playerStateService.getState(player).setSilver(ocr(ImageUtil.crop(screen, silverAmountArea)));
    }

    private void gatherLumberAmount() {
        Navigate iconSilver = selectTopMenuIcon(Resource.LUMBER, Operation.HOVER);
        BufferedImage screen = robot.captureScreen();
        Area area = Area.of(iconSilver.getPoint(), Point.of(764, 170), Point.of(849,261), Point.of(944, 281));
        playerStateService.getState(player).setLumber(ocr(ImageUtil.crop(screen, area)));
    }

    private void fillComplete(Resource resource) {
        fill(resource, true);
    }

    private void fillToTarget(Resource resource) {
        fill(resource, false);
    }


    private void fill(Resource resource, boolean complete) {
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
                setResourceAmount(resource, current);
            }

        } while ((complete || getTargetAmount(resource) > getResourceAmount(resource))
                && useButton.searchAgain().isPresent());

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }

    private int getResourceAmount(Resource resource) {
        switch (resource) {
            case LUMBER:
                return playerStateService.getState(player).getLumber();
            case IRON:
                return playerStateService.getState(player).getIron();
            case STONE:
                return playerStateService.getState(player).getStone();
            case SILVER:
                return playerStateService.getState(player).getSilver();
            case DRAGON_COIN:
                return 0; // Not relevant
            default:
                throw new RuntimeException("Not implemented");
        }
    }

    private int getTargetAmount(Resource resource) {
        if (playerStateService.getState(player).getResourcesTarget() == null) {
            return 0;
        } 
        switch (resource) {
            case LUMBER:
                return playerStateService.getState(player).getResourcesTarget().getLumber();
            case IRON:
                return playerStateService.getState(player).getResourcesTarget().getIron();
            case STONE:
                return playerStateService.getState(player).getResourcesTarget().getStone();
            case SILVER:
                return playerStateService.getState(player).getResourcesTarget().getSilver();
            case DRAGON_COIN:
                return 0; // Not relevant
            default:
                throw new RuntimeException("Not implemented");
        }
    }

    private void setResourceAmount(Resource resource, int amount) {
        switch (resource) {
            case LUMBER:
                playerStateService.getState(player).setLumber(amount);
                return;
            case IRON:
                playerStateService.getState(player).setIron(amount);
                return;
            case STONE:
                playerStateService.getState(player).setStone(amount);
                return;
            case SILVER:
                playerStateService.getState(player).setSilver(amount);
                return;
            case DRAGON_COIN:
                // Do nothing!
                return;
            default:
                throw new RuntimeException("Not implemented");
        }
    }


    private void gatherIronAmount() {
        Navigate iconSilver = selectTopMenuIcon(Resource.IRON, Operation.HOVER);
        BufferedImage screen = robot.captureScreen();
        Area area = Area.of(iconSilver.getPoint(), Point.of(859, 170), Point.of(943,261), Point.of(1035, 281));
        playerStateService.getState(player).setIron(ocr(ImageUtil.crop(screen, area)));
    }

    private void gatherStoneAmount() {
        Navigate iconSilver = selectTopMenuIcon(Resource.STONE, Operation.HOVER);
        BufferedImage screen = robot.captureScreen();
        Area area = Area.of(iconSilver.getPoint(), Point.of(953, 170), Point.of(1046,261), Point.of(1129, 281));
        playerStateService.getState(player).setStone(ocr(ImageUtil.crop(screen, area)));
    }

    private static enum Operation {
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
    
    private boolean shouldSendMessage() {
        for (Player it: Player.values()) {
            if (it.getPriority() >= player.getPriority()) {
                continue;
            }
            PlayerState playerState = playerStateService.getState(it);
            if (playerState != null && playerState.getResourcesTarget() != null) {
                if (playerState.getResourcesTarget().getLumber() < playerState.getLumber() ||
                        playerState.getResourcesTarget().getIron() < playerState.getIron() ||
                        playerState.getResourcesTarget().getStone() < playerState.getStone()) {
                    return false;
                }
            }
        }
        return true;
    }
}
