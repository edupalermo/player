package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.ArmyService;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.TimeLeftUtil;
import org.palermo.totalbattle.player.bean.SpeedUpBean;
import org.palermo.totalbattle.player.state.TroopQuantity;
import org.palermo.totalbattle.player.task.shared.SpeedUp;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Captain;
import org.palermo.totalbattle.selenium.stacking.Pool;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;
import org.palermo.totalbattle.util.OcrUtil;
import org.palermo.totalbattle.util.WhatsappUtil;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class BuildArmy {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;
    
    private final ArmyService armyService = new ArmyService();
    private final PlayerStateService playerStateService = new PlayerStateService();
    private final LockService lockService = new LockService();
    private final GameStateService gameStateService = new GameStateService();
    
    private final boolean TEST = false; 

    public BuildArmy(Player player) {
        this.player = player;
    }
    
    public void buildArmy() {
        if (lockService.isLocked(player, Scenario.FINISHED_TRAINING_ALL_TROOPS)) {
            return;
        }

        if (!armyService.shouldBuildArmy(player)) {
            return;
        }

        if (!TEST) {
            Captain captain = player.isHasHelen() ? Captain.HELEN : Captain.XI_GUIYING;
            if (!playerStateService.hasCaptain(player, captain)) {
                (new CaptainSelector(player)).select(captain);
            }
        }
        
        BufferedImage screen = robot.captureScreen();
        BufferedImage labelArmy = ImageUtil.loadResource("player/barracks/label_army.png");
        Area labelArmyArea = Area.fromTwoPoints(927, 1018, 998, 1038);
        Point labelQuestesPoint = ImageUtil.searchSurroundings(labelArmy, screen, labelArmyArea, 0.1, 20).orElse(null);

        if (labelQuestesPoint == null) {
            ImageUtil.write(ImageUtil.crop(screen, labelArmyArea), "error_screen.png");
            ImageUtil.write(labelArmy, "error_image.png");
            throw new RuntimeException("Couldn't find Army label!");
        }

        robot.leftClick(labelQuestesPoint.move(12, -30));
        robot.sleep(1000);

        screen = robot.captureScreen();
        BufferedImage titleBarracks = ImageUtil.loadResource("player/barracks/title_barracks.png");
        Area titleBarracksArea = Area.fromTwoPoints(920, 306, 1044, 338);
        Point titleBarracksPoint = ImageUtil.searchSurroundings(titleBarracks, screen, titleBarracksArea, 0.1, 20).orElse(null);

        if (titleBarracksPoint == null) {
            ImageUtil.write(ImageUtil.crop(screen, titleBarracksArea), "error_screen.png");
            ImageUtil.write(titleBarracks, "error_image.png");
            throw new RuntimeException("Couldn't find Barracks title!");
        }

        Area buttonArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(1076, 377), Point.of(1184, 404));         
        Navigate navigateHelpButton = Navigate.builder()
                .resourceName("player/barracks/button_help.png")
                .area(buttonArea)
                .waitLimit(1500)
                .build();
        
        if (navigateHelpButton.exist()) {
            navigateHelpButton.leftClick();
            robot.sleep(200);
        }
        else {
            Navigate navigateHourglass = Navigate.builder()
                    .area(Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(919, 376), Point.of(995, 397)))
                    .resourceName("player/barracks/icon_hourglass.png")
                    .waitLimit(1500)
                    .build();
            
            if (navigateHourglass.exist()) {
                BufferedImage timeLeft = ImageUtil.crop(screen, Area.of(navigateHourglass.getPoint(), 18, -2, 92, 18));

                try {
                    String timeLeftAsText = treatTimeLeft(timeLeft);
                    System.out.println("Time Left: " + timeLeftAsText);

                    LocalDateTime nextLocalDateTime = TimeLeftUtil.parse(timeLeftAsText).orElse(null);
                    if (nextLocalDateTime == null) {
                        throw new RuntimeException("Failed to parse time left: " + timeLeftAsText);
                    }

                    // Click of the speed-up button
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1174, 390)));
                    robot.sleep(350);

                    speedUp(15, nextLocalDateTime);
                } catch (RuntimeException e) {
                    // Click of the speed-up button
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1174, 390)));
                    robot.sleep(350);


                    speedUp(1, LocalDateTime.now().plusMinutes(20));
                }
            }
            
            if (navigateHelpButton.searchAgain().isEmpty() &&
                    navigateHourglass.searchAgain().isEmpty()) {
                
                Navigate.builder()
                        .resourceName("player/barracks/button_complete.png")
                        .area(buttonArea)
                        .waitLimit(1500)
                        .build()
                        .leftClickIfExists();

                chooseTroopToBuild(titleBarracksPoint);
            }
            
        }

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(150);
    }
    
    private void updateTroopQuantities(Point titleBarracksPoint) {
        for (TroopQuantity troopQuantity : armyService.getProductionList(player)) {
            int currentSize = getCurrentUnitNumber(titleBarracksPoint, troopQuantity.getUnit());
            armyService.setCurrentTroopQuantity(player, troopQuantity.getUnit(), currentSize);
        }
    }
    
    private String treatTimeLeft(BufferedImage input) {
        BufferedImage timeLeft = ImageUtil.toGrayscale(input, new String[] {"FFF7BF"});
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        timeLeft = ImageUtil.cropText(timeLeft);
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        if (timeLeft.getHeight() < OcrUtil.OCR_HEIGHT) {
            timeLeft = ImageUtil.resize(timeLeft, OcrUtil.OCR_HEIGHT);
        }
        // ImageUtil.showImageAndWait(timeLeft);
        boolean manualOcr = gameStateService.getPropertyAsBoolean(GameStateService.PROPERTY_MANUAL_OCR);
        return OcrUtil.ocr(timeLeft, OcrUtil.WHITELIST_FOR_COUNTDOWN, OcrUtil.PATTERN_FOR_COUNTDOWN, manualOcr);
    }
    
    public void testSpeedUps() {
        BufferedImage screen = robot.captureScreen();
        BufferedImage speedUpsTitle = ImageUtil.loadResource("player/speed_up/title_speed_ups.png");
        Area speedUpsTitleArea = Area.fromTwoPoints(910, 325, 1066, 361);
        Point speedUpsTitlePoint = ImageUtil.search(speedUpsTitle, screen, speedUpsTitleArea, 0.1).orElse(null);
        if (speedUpsTitlePoint == null) {
            throw new RuntimeException("Could not find speed up title");
        }
        
        for (SpeedUpBean speedUpBean : SpeedUp.speedUps) {
            robot.leftClick(Point.of(speedUpsTitlePoint, Point.of(958, 346), Point.of(1258, 494)));
            robot.sleep(500);

            SpeedUp.clickOnSpeedUp(speedUpBean, speedUpsTitlePoint);
        }
    }


    private void speedUp(int turns, LocalDateTime dateTime) {

        long seconds = Duration.between(LocalDateTime.now(), dateTime).getSeconds();

        Navigate speedUpsTitle = Navigate.builder()
                .resourceName("player/speed_up/title_speed_ups.png")
                .area(Area.fromTwoPoints(910, 325, 1066, 361))
                .waitLimit(1000)
                .build();
        
        Set<String> exclusionSet = new HashSet<>();

        outer: for (int r = 0; r < turns; r++) {
            
            if (r == 0) {
                speedUpsTitle.ensureExistence();
            }
            else if (speedUpsTitle.searchAgain().isEmpty()) {
                return;
            }

            if (r != 0) {
                // Scroll up
                robot.leftClick(Point.of(speedUpsTitle.getPoint(), Point.of(958, 346), Point.of(1258, 494)));
                robot.sleep(500);
            }

            SpeedUpBean bestSpeedUp = findBestSpeedUp(seconds, exclusionSet).orElse(null);

            if (bestSpeedUp == null) {
                System.out.println("Shouldn't use speed ups!");
                break;
            }

            // log.info("Best: {} Remaining: {}", bestSpeedUp.getLabel(), secondsToReadable(seconds));
            
            while (!SpeedUp.clickOnSpeedUp(bestSpeedUp, speedUpsTitle.getPoint())) {
                if (speedUpsTitle.searchAgain().isEmpty()) { // Alguem doou e janela sumiu
                    break outer;
                }

                exclusionSet.add(bestSpeedUp.getLabel());
                bestSpeedUp = findBestSpeedUp(seconds, exclusionSet).orElse(null);
            }
            
            seconds = seconds - bestSpeedUp.getSeconds();
        }

        if (speedUpsTitle.searchAgain().isPresent()) {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }

    private static Optional<SpeedUpBean> findBestSpeedUp(long seconds, Set<String> exclusionSet) {
        SpeedUpBean bestSpeedUp = null;
        for (SpeedUpBean bean : SpeedUp.speedUps) {
            if (exclusionSet.contains(bean.getLabel())) {
                continue;
            }
            if (bean.getSeconds() < seconds) {
                if (bestSpeedUp == null) {
                    bestSpeedUp = bean;
                }
                else if (bean.getSeconds() > bestSpeedUp.getSeconds()) {
                    bestSpeedUp = bean;
                }
            }
        }
        return Optional.ofNullable(bestSpeedUp);
    }

    private int findIndex(SpeedUpBean speedUpBean) {
        for (int i = 0; i <= speedUpBean.getSeconds(); i++) {
            SpeedUpBean it = SpeedUp.speedUps.get(i);
            if (it.getLabel().equals(speedUpBean.getLabel())) {
                return i;
            }
        }
        throw new RuntimeException("Speed up not found!");
    }
    
    private String secondsToReadable(long input) {
        
        if (input < 60) {
            return input + "s";
        }
        else if (input < 60 * 60) {
            return (input / 60) + "m" + (input % 60) + "s";
        }
        else if (input < 60 * 60 * 24) {
            return (input / (60 * 60)) + "h" + ((input % (60*60)) / 60) + "m" + (input % 60) + "s";
        }
        else {
            return (input / (60 * 60 * 24)) + "d" + ((input % (60 * 60 * 24)) / (60 * 60)) + "h" + ((input % (60 * 60)) / 60) + "m" + (input % 60) + "s";
        }
    }

    private void chooseTroopToBuild(Point titleBarracksPoint) {

        List<TroopQuantity> list = armyService.getProductionList(player);

        boolean trainedSomething = false;
        
        if (!TEST) {
            // I don't think I should check every thing.
            for (int i = 0; i < list.size(); i++) {
                TroopQuantity troopQuantity = list.get(i);
                int currentSize = getCurrentUnitNumber(titleBarracksPoint, troopQuantity.getUnit());
                armyService.setCurrentTroopQuantity(player, troopQuantity.getUnit(), currentSize);

                if (currentSize < troopQuantity.getTarget()) {
                    if (troopQuantity.getUnit().getPool() == Pool.DOMINANCE) {
                        lockService.lock(player, Scenario.FINISHED_TRAINING_NON_MONSTERS, LocalDateTime.now().plusHours(1));
                        if (player == Player.PALERMO) {
                            WhatsappUtil.send(player.getName() + " has finished building the Guardsman");
                        }
                    }
                    train(titleBarracksPoint, troopQuantity.getUnit(), troopQuantity.getTarget() - currentSize);
                    trainedSomething = true;
                    break;
                }
            }

            if (!trainedSomething) {
                lockService.lock(player, Scenario.FINISHED_TRAINING_NON_MONSTERS, LocalDateTime.now().plusHours(1));
                lockService.lock(player, Scenario.FINISHED_TRAINING_ALL_TROOPS, LocalDateTime.now().plusHours(1));
                WhatsappUtil.send(player.getName() + " has finished building the army");
            }
        }
        else {
            //train(titleBarracksPoint, Unit.G4_RANGED, 1);
            train(titleBarracksPoint, Unit.G5_MELEE, 1);
            // train(titleBarracksPoint, Unit.G3_MELEE, 1);
            // train(titleBarracksPoint, Unit.G3_MOUNTED, 1);
            // train(titleBarracksPoint, Unit.S5_SWORDSMAN, 1);
            // train(titleBarracksPoint, Unit.S5_DEADSHOT, 1);
            // train(titleBarracksPoint, Unit.S5_SPY, 1);
            //train(titleBarracksPoint, Unit.S5_LION_RIDER, 1);
            //train(titleBarracksPoint, Unit.S5_VULTURE, 1);
            //train(titleBarracksPoint, Unit.G6_RANGED, 1);
            //train(titleBarracksPoint, Unit.G6_MELEE, 1);
            //train(titleBarracksPoint, Unit.G6_MOUNTED, 1);
            //train(titleBarracksPoint, Unit.G6_GRIFFIN, 1);
        }
    }

    private void train(Point titleBarracksPoint, Unit unit, int quantity) {

        selectUnit(titleBarracksPoint, unit);

        Point textPoint = null;
        Area silverArea = null;
        Area foodArea = null;
        Point trainButtonPoint = null;

        Point silverPoint = null;
        Point foodPoint = null;


        switch (unit) {
            case G1_RANGED, G2_RANGED, G3_RANGED, G4_RANGED, G5_RANGED, S1_SWORDSMAN, S2_SWORDSMAN, S3_SWORDSMAN, S4_SWORDSMAN, S5_SWORDSMAN, 
                 G5_GRIFFIN, G6_GRIFFIN,
                 G6_MOUNTED,
                 EMERALD_DRAGON, WATER_ELEMENTAL, STONE_GARGOYLE, BATTLE_BOAR,
                 EC1_ENGINEER, EC2_ENGINEER, EC3_ENGINEER, EC4_ENGINEER, EC5_ENGINEER,
                 S5_LION_RIDER:
                textPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(822, 719));
                silverArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790, 775), Point.of(798, 783));
                foodArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790, 775 + 35), Point.of(798, 783 + 35));
                trainButtonPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(864, 814));
                silverPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(745, 780));
                foodPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(745, 814));
                break;
            case G1_MELEE, G2_MELEE, G3_MELEE, G4_MELEE, G5_MELEE,
                    S1_SPY, S2_SPY, S3_SPY, S4_SPY, S5_SPY,
                    MAGIC_DRAGON, ICE_PHOENIX, MANY_ARMED_GUARDIAN, GORGON_MEDUSA,
                 G6_RANGED,
                 S5_VULTURE:
                textPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(822 + 261, 719));
                silverArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790 + 261, 775), Point.of(798 + 261, 783));
                foodArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790 + 261, 775 + 35), Point.of(798 + 261, 783 + 35));
                trainButtonPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(864 + 261, 814));
                silverPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1005, 780));
                foodPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1005, 814));
                break;
            case G1_MOUNTED, G2_MOUNTED, G3_MOUNTED, G4_MOUNTED, G5_MOUNTED,
                 G6_MELEE,
                    DESERT_VANQUISER, FLAMING_CENTAUR, ETTIN, FEARSOME_MANTICORE,
                 S5_DEADSHOT:
                textPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(822 + 523, 719));
                silverArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790 + 522, 775), Point.of(798 + 522, 783));
                foodArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790 + 522, 775 + 35), Point.of(798 + 522, 783 + 35));
                trainButtonPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(864 + 522, 814));
                silverPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1268, 780));
                foodPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1268, 814));
                break;

            default:
                throw new RuntimeException("Not implemented for unit " + unit.name());
        }

        robot.leftClick(textPoint);
        robot.typeString(String.valueOf(quantity));
        robot.sleep(250);

        handleResourcesIfNeeded(unit, silverPoint, silverArea, foodPoint, foodArea);

        int target;
        int counter = 0;
        boolean continueTrying = true;

        do {
            target = Math.max((int) Math.round(quantity / Math.pow(2, counter)), 1);


            robot.leftClick(titleBarracksPoint); // I need to take the focus from the text area
            robot.leftClick(textPoint);
            robot.sleep(250);
            robot.typeString(String.valueOf(target));
            robot.sleep(500);
            
            counter = counter + 1;
            
            boolean isSilverEnough = isResourceEnough(silverArea); 
            
            if (isSilverEnough && isResourceEnough(foodArea)) {
                
                log.info("Training {}: {}", unit.name(), target);
                
                // Click on train button
                robot.leftClick(trainButtonPoint);
                robot.sleep(1500);

                // Click on help button
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1174, 390)));
                robot.sleep(350);

                continueTrying = false;
            }
            
            if (target == 1) {
                continueTrying = false;
                log.info("User {} doesnt have resources for one {}" , player.name(), unit.name());
            }
            
        } while(continueTrying);
    }

    private boolean isResourceEnough(Area area) {
        BufferedImage screen = robot.captureScreen();

        BufferedImage colorOkImage = ImageUtil.loadResource("player/barracks/color_ok.png");

        return ImageUtil.search(colorOkImage, screen, area, 0.1).isPresent();
    }

    private void handleResourcesIfNeeded(Unit unit, Point silverPoint, Area silverArea, Point foodPoint, Area foodArea) {

        if (!isResourceEnough(silverArea)) {
            fillSilver(silverPoint);
        }

        if (!isResourceEnough(foodArea)) {
            if (unit.getPool() == Pool.LEADERSHIP) {
                fillFood(foodPoint);
            }
            else {
                System.out.println("Not enough dragon coins!");
            }
        }
    }

    private void fillFood(Point point) {
        robot.leftClick(point);
        robot.sleep(500);

        BufferedImage screen = robot.captureScreen();
        Area iconFoodArea = RegionSelector.selectArea("TOP_UP_SILVER_SILVER_ICON", screen);
        BufferedImage iconFood = ImageUtil.loadResource("player/icon_food.png");
        Point iconSilverPoint = ImageUtil.searchSurroundings(iconFood, screen, iconFoodArea, 0.1, 20).orElse(null);

        if (iconSilverPoint == null) {
            throw new RuntimeException("Icon silver not found!");
        }

        boolean stillHasSavedResources;

        do {
            screen = robot.captureScreen();
            Area buttonUseArea = RegionSelector.selectArea("TOP_UP_SILVER_FIRST_USE_BUTTON", screen);
            BufferedImage buttonUse = ImageUtil.loadResource("player/button_use.png");
            Point buttonUsePoint = ImageUtil.searchSurroundings(buttonUse, screen, buttonUseArea, 0.1, 20).orElse(null);

            if (buttonUsePoint != null) {
                robot.leftClick(buttonUsePoint, buttonUse);
                robot.sleep(300);

                Navigate.builder()
                        .areaName("TOP_UP_SILVER_SLIDE_SUBSEQUENT_USE_BUTTON")
                        .resourceName("player/button_use.png")
                        .build()
                        .leftClick();
            }


            stillHasSavedResources = buttonUsePoint != null;

        } while(stillHasSavedResources);


        Navigate navigateIconFood = Navigate.builder()
                .areaName("TOP_UP_SILVER_SILVER_ICON")
                .resourceName("player/icon_food.png")
                .waitLimit(1000)
                .build();

        if (navigateIconFood.exist()) {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }


    private void fillSilver(Point point) {
        robot.leftClick(point);
        robot.sleep(500);

        BufferedImage screen = robot.captureScreen();
        Area iconSilverArea = RegionSelector.selectArea("TOP_UP_SILVER_SILVER_ICON", screen);
        BufferedImage iconSilver = ImageUtil.loadResource("player/icon_silver.png");
        Point iconSilverPoint = ImageUtil.searchSurroundings(iconSilver, screen, iconSilverArea, 0.1, 20).orElse(null);

        if (iconSilverPoint == null) {
            throw new RuntimeException("Icon silver not found!");
        }

        boolean stillHasSavedResources;

        do {
            screen = robot.captureScreen();
            Area buttonUseArea = RegionSelector.selectArea("TOP_UP_SILVER_FIRST_USE_BUTTON", screen);
            BufferedImage buttonUse = ImageUtil.loadResource("player/button_use.png");
            Point buttonUsePoint = ImageUtil.searchSurroundings(buttonUse, screen, buttonUseArea, 0.1, 20).orElse(null);

            if (buttonUsePoint != null) {
                robot.leftClick(buttonUsePoint, buttonUse);
                robot.sleep(300);

                Navigate.builder()
                        .areaName("TOP_UP_SILVER_SLIDE_SUBSEQUENT_USE_BUTTON")
                        .resourceName("player/button_use.png")
                        .build()
                        .leftClick();
            }

            stillHasSavedResources = buttonUsePoint != null;

        } while(stillHasSavedResources);

        Navigate silverIcon = Navigate.builder()
                .areaName("TOP_UP_SILVER_SILVER_ICON")
                .resourceName("player/icon_silver.png")
                .waitLimit(1000)
                .build();

        if (silverIcon.exist()) {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }

    private void selectUnit(Point titleBarracksPoint, Unit unit) {
        long tierPos;

        long wait = 350;

        switch (unit) {
            case G1_RANGED, G2_RANGED, G3_RANGED, G4_RANGED, G5_RANGED:
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                robot.sleep(wait);
                
                // Scroll up (player G6 needs to scroll up)
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 454)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 1) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
                robot.sleep(wait);
                break;

            case G6_RANGED:
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                robot.sleep(wait);

                // Scroll to the correct position
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 607)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 6) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(951, tierPos)));
                robot.sleep(wait);
                break;
                
            case G1_MELEE, G2_MELEE, G3_MELEE, G4_MELEE, G5_MELEE :
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                robot.sleep(wait);

                // Scroll up (player G6 needs to scroll up)
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 454)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 1) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(951, tierPos)));
                robot.sleep(wait);
                break;
                
            case G6_MELEE:
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));

                // Scroll to the correct position
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 607)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 6) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1212, tierPos)));
                robot.sleep(wait);
                break;
                
            case G1_MOUNTED, G2_MOUNTED, G3_MOUNTED, G4_MOUNTED, G5_MOUNTED :
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));

                // Scroll up (player G6 needs to scroll up)
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 454)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 1) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1212, tierPos)));
                robot.sleep(wait);
                break;
            
            case G6_MOUNTED:
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                robot.sleep(wait);

                // Scroll to the correct position
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 734)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 6) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
                robot.sleep(wait);
                break;
                
            case G5_GRIFFIN, G6_GRIFFIN:
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                robot.sleep(wait);

                // Scroll to the correct position
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 607)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 5) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
                robot.sleep(wait);
                break;

            case S1_SWORDSMAN, S2_SWORDSMAN, S3_SWORDSMAN, S4_SWORDSMAN, S5_SWORDSMAN:
                // Click on Specialists left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 435)));
                robot.sleep(wait);
                
                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 1) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
                robot.sleep(wait);
                break;

            case S1_SPY, S2_SPY, S3_SPY, S4_SPY, S5_SPY:
                // Click on Specialists left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 435)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 1) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(951, tierPos)));
                robot.sleep(wait);
                break;
                
            case S5_DEADSHOT:
                // Click on Specialists left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 435)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 5) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(951, tierPos)));
                robot.sleep(wait);
                break;

            case S5_LION_RIDER:
                // Click on Specialists left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 435)));
                robot.sleep(wait);
                
                // Scroll to second line
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 607)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 5) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
                robot.sleep(wait);
                break;

            case S5_VULTURE:
                // Click on Specialists left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 435)));
                robot.sleep(wait);

                // Scroll to second line
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 607)));
                robot.sleep(wait);
                
                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 5) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(951, tierPos)));
                robot.sleep(wait);
                break;
                
            case EMERALD_DRAGON, MAGIC_DRAGON, DESERT_VANQUISER:
                // Click on Dragons left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 538)));
                robot.sleep(wait);
                break;

            case WATER_ELEMENTAL, ICE_PHOENIX, FLAMING_CENTAUR:
                // Click on Elementals left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 590)));
                robot.sleep(wait);
                break;

            case STONE_GARGOYLE, MANY_ARMED_GUARDIAN, ETTIN:
                // Click on Giants left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 642)));
                robot.sleep(wait);
                break;

            case BATTLE_BOAR, GORGON_MEDUSA, FEARSOME_MANTICORE:
                // Click on Beats left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 694)));
                robot.sleep(wait);
                break;
            case EC1_ENGINEER, EC2_ENGINEER, EC3_ENGINEER, EC4_ENGINEER, EC5_ENGINEER:
                // Click on Engineer corps
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 486)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 1) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
                robot.sleep(wait);
                break;
            default:
                throw new RuntimeException("Not implemented for unit " + unit.name());
        }
    }

    private int getCurrentUnitNumber(Point titleBarracksPoint, Unit unit) {
        Area area;

        selectUnit(titleBarracksPoint, unit);

        switch (unit) {
            case G1_RANGED, G2_RANGED, G3_RANGED, G4_RANGED, G5_RANGED, 
                 S1_SWORDSMAN, S2_SWORDSMAN, S3_SWORDSMAN, S4_SWORDSMAN, S5_SWORDSMAN,
                 G6_MOUNTED,
                    EMERALD_DRAGON, WATER_ELEMENTAL, STONE_GARGOYLE, BATTLE_BOAR, 
                 G5_GRIFFIN, G6_GRIFFIN,
                    EC1_ENGINEER, EC2_ENGINEER, EC3_ENGINEER, EC4_ENGINEER, EC5_ENGINEER,
                    S5_LION_RIDER:
                area = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(852, 677), Point.of(912, 699));
                break;
            case G1_MELEE, G2_MELEE, G3_MELEE, G4_MELEE, G5_MELEE,
                 S1_SPY, S2_SPY, S3_SPY, S4_SPY, S5_SPY,
                 G6_RANGED,
                 MAGIC_DRAGON, ICE_PHOENIX, MANY_ARMED_GUARDIAN, GORGON_MEDUSA, 
                 S5_VULTURE:
                area = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(852 + 261, 677), Point.of(912 + 261, 699));
                break;
            case G1_MOUNTED, G2_MOUNTED, G3_MOUNTED, G4_MOUNTED, G5_MOUNTED,
                 G6_MELEE,
                    DESERT_VANQUISER, FLAMING_CENTAUR, ETTIN, FEARSOME_MANTICORE,
                    S5_DEADSHOT:
                area = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(852 + 522, 677), Point.of(912 + 522, 699));
                break;
            default:
                throw new RuntimeException("Not Implemented for "+ unit.name());
        }

        robot.sleep(200);
        BufferedImage screen = robot.captureScreen();
        BufferedImage quantityImage = ImageUtil.crop(screen, area);
        quantityImage = ImageUtil.toGrayscale(quantityImage, new String[] {"FFF7BF"});
        quantityImage = ImageUtil.linearNormalization(quantityImage);
        quantityImage = ImageUtil.cropText(quantityImage);
        quantityImage = ImageUtil.linearNormalization(quantityImage);
        if (quantityImage.getHeight() < 70) {
            quantityImage = ImageUtil.resize(quantityImage, 70);
        }

        boolean manualOcr = gameStateService.getPropertyAsBoolean(GameStateService.PROPERTY_MANUAL_OCR);
        String quantityAsString = OcrUtil.ocr(quantityImage, OcrUtil.WHITELIST_FOR_ONLY_NUMBERS, OcrUtil.PATTERN_FOR_ONLY_NUMBERS, manualOcr);
        System.out.println("Quantity of " + unit.name() + " - " + quantityAsString);

        return Integer.parseInt(quantityAsString);
    }
}
