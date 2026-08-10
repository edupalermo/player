package org.palermo.totalbattle.player.task;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.ArmyService;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.TimeLeftUtil;
import org.palermo.totalbattle.player.bean.SpeedUpBean;
import org.palermo.totalbattle.player.state.TroopQuantity;
import org.palermo.totalbattle.player.task.shared.SpeedUp;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.selenium.stacking.Captain;
import org.palermo.totalbattle.selenium.stacking.Pool;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.server.model.Player;
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
    
    private Unit lastSelected = null;

    public BuildArmy(Player player) {
        this.player = player;
    }

    public void buildArmy() {
        this.lastSelected = null;
        
        if (lockService.isLocked(player, Scenario.FINISHED_TRAINING_ALL_TROOPS)) {
            log.info("Building Army is locked because player is ready to ATTACK!");
            return;
        }

        if (!armyService.shouldBuildArmy(player)) {
            return;
        }
        
        try {
            internalBuildArmy();
        } catch(Exception e) {
            log.error(e.getMessage(), e);

            robot.sleep(300);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }

    private void chooseTroopToBuild(Point titleBarracksPoint) {

        List<TroopQuantity> list = armyService.getProductionList(player);

        boolean trainedSomething = false;

        if (!TEST) {
            // I don't think I should check every thing.
            for (int i = 0; i < list.size(); i++) {
                TroopQuantity troopQuantity = list.get(i);
                System.out.println("Trying " + troopQuantity.getUnit().name());
                int currentSize = getCurrentUnitNumber(titleBarracksPoint, troopQuantity.getUnit());
                armyService.setCurrentTroopQuantity(player, troopQuantity.getUnit(), currentSize);

                if (currentSize < troopQuantity.getTarget()) {
                    if (troopQuantity.getUnit().getPool() == Pool.DOMINANCE) {
                        lockService.lock(player, Scenario.FINISHED_TRAINING_NON_MONSTERS, LocalDateTime.now().plusHours(1));
                        log.info("Player finished to train Guardsmen and Specialists");
                        if (player == Player.PALERMO) {
                            WhatsappUtil.send(player.getName() + " has finished building the Guardsman");
                        }
                    }
                    train(titleBarracksPoint, troopQuantity.getUnit(), troopQuantity.getTarget() - currentSize);
                    trainedSomething = true;
                    break;
                }
                else {
                    log.info("Troop {} is not needed {} / {}", troopQuantity.getUnit().name(), currentSize, troopQuantity.getTarget());
                }
            }

            if (!trainedSomething) {
                lockService.lock(player, Scenario.FINISHED_TRAINING_NON_MONSTERS, LocalDateTime.now().plusHours(1));
                lockService.lock(player, Scenario.FINISHED_TRAINING_ALL_TROOPS, LocalDateTime.now().plusHours(1));
                log.info("Player is ready to ATTACK!");
                WhatsappUtil.send(player.getName() + " has finished building the army");
            }
        }
        else {
            //train(titleBarracksPoint, Unit.DRAGON_VIII, 1);
            //train(titleBarracksPoint, Unit.ELEMENTAL_VIII, 1);
            //train(titleBarracksPoint, Unit.GIANT_VIII, 1);
            train(titleBarracksPoint, Unit.BEAST_VIII, 1);
        }
    }

    public void internalBuildArmy() {
        if (lockService.isLocked(player, Scenario.FINISHED_TRAINING_ALL_TROOPS)) {
            log.info("Building Army is locked because player is ready to ATTACK!");
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

        Point labelArmyPoint = findArmyLabel();

        // Click on Army Label
        robot.leftClick(labelArmyPoint.move(12, -30));
        robot.sleep(1000);
        
        playBarracksPopUp();

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(150);
    }
    
    private Point findArmyLabel() {
        BufferedImage screen = robot.captureScreen();
        
        Area area = Area.fromTwoPoints(927, 1018, 998, 1038);
        Navigate navigate = Navigate.builder()
                .resourceName("player/barracks/label_army.png")
                .area(area)
                .waitLimit(3000)
                .build();

        if (navigate.exist()) {
            return navigate.getPoint();
        }

        BufferedImage labelArmy = ImageUtil.loadResource("player/barracks/label_army_02.png");
        Point labelArmyPoint = ImageUtil.searchSurroundings(labelArmy, screen, area, 0.1, 20).orElse(null);

        if (labelArmyPoint != null) {
            return labelArmyPoint;
        }

        ImageUtil.write(ImageUtil.crop(screen, area), "error_screen.png");
        ImageUtil.write(labelArmy, "error_image.png");
        throw new RuntimeException("Couldn't find Army label!");
    }
    
    private void playBarracksPopUp() {
        BufferedImage screen = robot.captureScreen();
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
                // Click of the speed-up button
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1174, 390)));
                robot.sleep(350);

                playSpeedUpPopup(24);
            }

            if (navigateHourglass.searchAgain().isEmpty() && 
                    navigateHelpButton.searchAgain().isEmpty()) {
                Navigate.builder()
                        .resourceName("player/barracks/button_complete.png")
                        .area(buttonArea)
                        .waitLimit(1500)
                        .build()
                        .leftClickIfExists();

                chooseTroopToBuild(titleBarracksPoint);
            }
        }
    }
    
    private void updateTroopQuantities(Point titleBarracksPoint) {
        for (TroopQuantity troopQuantity : armyService.getProductionList(player)) {
            int currentSize = getCurrentUnitNumber(titleBarracksPoint, troopQuantity.getUnit());
            armyService.setCurrentTroopQuantity(player, troopQuantity.getUnit(), currentSize);
        }
    }
    
    private LocalDateTime getTimeLeft(Navigate navigateHourglass) {
        BufferedImage timeLeft = robot.captureScreen(Area.of(navigateHourglass.getPoint(), 18, -2, 92, 18));
        String timeLeftAsText = treatTimeLeft(timeLeft);
        System.out.println("Time Left: " + timeLeftAsText);
        LocalDateTime nextLocalDateTime = TimeLeftUtil.parse(timeLeftAsText).orElse(null);
        if (nextLocalDateTime == null) {
            throw new RuntimeException("Failed to parse time left: " + timeLeftAsText);
        }
        return nextLocalDateTime;
    }
    
    private String treatTimeLeft(BufferedImage input) {
        BufferedImage limit = ImageUtil.loadResource("player/speed_up/limit.png");
        Point limitPoint = ImageUtil.search(limit, input, 0.01).orElse(null);
        if (limitPoint != null) {
            input = ImageUtil.crop(input, Area.of(0, 0, limitPoint.getX() ,input.getHeight()));
        }

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

    private int treatCurrentInputFieldValue(BufferedImage input) {
        BufferedImage timeLeft = ImageUtil.toGrayscale(input, new String[] {"4C2727"});
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        timeLeft = ImageUtil.cropText(timeLeft);
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        if (timeLeft.getHeight() < OcrUtil.OCR_HEIGHT) {
            timeLeft = ImageUtil.resize(timeLeft, OcrUtil.OCR_HEIGHT);
        }
        // ImageUtil.showImageAndWait(timeLeft);
        boolean manualOcr = gameStateService.getPropertyAsBoolean(GameStateService.PROPERTY_MANUAL_OCR);
        String temporary = OcrUtil.ocr(timeLeft, OcrUtil.WHITELIST_FOR_NUMBERS_WITH_THOUSAND_SEPARATOR_AND_PLUS, manualOcr);
        temporary = temporary.replaceAll("\\+", "");
        temporary = temporary.replaceAll(",", "");
        temporary = temporary.trim();
        return Integer.parseInt(temporary);
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

    
    public void playSpeedUpPopup(int turns) {
        Navigate speedUpsTitle = Navigate.builder()
                .resourceName("player/speed_up/title_speed_ups.png")
                .area(Area.fromTwoPoints(910, 325, 1066, 361))
                .waitLimit(5000)
                .build();

        Set<String> exclusionSet = new HashSet<>();
        
        long doubleCheck = -1;

        for (int r = 0; r < turns; r++) {
            
            if (r == 0) {
                speedUpsTitle.ensureExistence();
            }
            else if (speedUpsTitle.searchAgain().isEmpty()) {
                return;
            }

            Navigate navigateHourglass = Navigate.builder()
                    .area(Area.of(speedUpsTitle.getPoint(), Point.of(958, 346), Point.of(1079, 406), Point.of(1202, 434)))
                    .resourceName("player/barracks/icon_hourglass.png")
                    .waitLimit(1500)
                    .build();

            LocalDateTime dateTime = getTimeLeft(navigateHourglass);
            long seconds = Duration.between(LocalDateTime.now(), dateTime).getSeconds();

            if (doubleCheck == -1) {
                doubleCheck = seconds;
            }
            if (doubleCheck != -1 && seconds > doubleCheck) {
                log.info("Spped up cannot go up, never!");
                continue;
            }
            doubleCheck = seconds;
            
            clickOnSpeedUp(speedUpsTitle, seconds, exclusionSet);
        }

        if (speedUpsTitle.searchAgain().isPresent()) {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }

    private SpeedUpBean clickOnSpeedUp(Navigate speedUpsTitle, long seconds, Set<String> exclusionSet) {

        SpeedUpBean bestSpeedUp = findBestSpeedUp(seconds, exclusionSet).orElse(null);

        if (bestSpeedUp == null) {
            log.info("Shouldn't use speed ups! Seconds: " + seconds);
            return null;
        }

        while (!SpeedUp.clickOnSpeedUp(bestSpeedUp, speedUpsTitle.getPoint())) {
            if (speedUpsTitle.searchAgain().isEmpty()) { // Alguem doou e janela sumiu
                return null;
            }

            exclusionSet.add(bestSpeedUp.getLabel());
            bestSpeedUp = findBestSpeedUp(seconds, exclusionSet).orElse(null);

            if (bestSpeedUp == null) {
                return null;
            }
        }
        return bestSpeedUp;
    }
    
    private static Optional<SpeedUpBean> findBestSpeedUp(long seconds, Set<String> exclusionSet) {
        SpeedUpBean bestSpeedUp = null;
        for (SpeedUpBean bean : SpeedUp.speedUps) {
            if (exclusionSet.contains(bean.getLabel())) {
                continue;
            }
            if (bean.getSeconds() < seconds) {
                if ((bestSpeedUp == null) ||  (bean.getSeconds() > bestSpeedUp.getSeconds())) {
                    bestSpeedUp = bean;
                }
            }
        }
        if (bestSpeedUp != null) {
            return Optional.of(bestSpeedUp);
        }

        for (SpeedUpBean bean : SpeedUp.speedUps) {
            if (exclusionSet.contains(bean.getLabel())) {
                continue;
            }
            if (bean.getSeconds() <= Duration.ofHours(1).getSeconds()) {
                if ((bestSpeedUp == null) ||  (bean.getSeconds() < bestSpeedUp.getSeconds())) {
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

    private void train(Point titleBarracksPoint, Unit unit, int quantity) {

        selectUnit(titleBarracksPoint, unit);


        Area inputArea = null;


        Point inputPoint = null;
        Area silverArea = null;
        Area foodArea = null;
        Point trainButtonPoint = null;

        Point silverPoint = null;
        Point foodPoint = null;

        int shift = 0;

        Transformation transformation = Transformation.builder()
                .reference(titleBarracksPoint)
                .real(Point.of(961, 324))
                .build();
        
        switch (unit) {
            case G1_RANGED, G2_RANGED, G3_RANGED, G4_RANGED, G5_RANGED, S1_SWORDSMAN, S2_SWORDSMAN, S3_SWORDSMAN, S4_SWORDSMAN, S5_SWORDSMAN,
                 G5_GRIFFIN, G6_GRIFFIN, G7_GRIFFIN,
                 G6_MOUNTED, G7_MOUNTED, G8_MOUNTED,
                 DRAGON_III, ELEMENTAL_III, GIANT_III, BEAST_III,
                 DRAGON_VI, ELEMENTAL_VI, GIANT_VI, BEAST_VI,
                 EC1_ENGINEER, EC2_ENGINEER, EC3_ENGINEER, EC4_ENGINEER, EC5_ENGINEER,
                 S5_LION_RIDER,
                 S6_SPY, S6_MOUNTED:
                if (Sets.newHashSet(Unit.DRAGON_VI, Unit.ELEMENTAL_VI, Unit.GIANT_VI, Unit.BEAST_VI, Unit.G8_MOUNTED).contains(unit)) {
                    shift = 41;
                }
                inputArea = transformation.transform(Point.of(817, 711 + shift), Point.of(914, 730 + shift));
                inputPoint = transformation.transform(Point.of(822, 719 + shift));
                silverArea = transformation.transform(Point.of(790, 775 + shift), Point.of(798, 783 + shift));
                foodArea = transformation.transform(Point.of(790, 775 + 35 + shift), Point.of(798, 818 + shift));
                trainButtonPoint = transformation.transform(Point.of(864, 814 + shift));
                silverPoint = transformation.transform(Point.of(745, 780 + shift));
                foodPoint = transformation.transform(Point.of(745, 814 + shift));
                break;
            case G1_MELEE, G2_MELEE, G3_MELEE, G4_MELEE, G5_MELEE,
                 S1_SPY, S2_SPY, S3_SPY, S4_SPY, S5_SPY,
                 DRAGON_IV, ELEMENTAL_IV, GIANT_IV, BEAST_IV,
                 DRAGON_VII, ELEMENTAL_VII, GIANT_VII, BEAST_VII,
                 G6_RANGED, G7_RANGED, G8_RANGED, G8_COURAX,
                 S5_VULTURE, S6_FLYING,
                 EC6_ENGINEER, EC7_ENGINEER:
                if (Sets.newHashSet(Unit.DRAGON_VII, Unit.ELEMENTAL_VII, Unit.GIANT_VII, Unit.BEAST_VII, Unit.G8_COURAX).contains(unit)) {
                    shift = 41;
                }
                inputArea = transformation.transform(Point.of(817 + 261, 711 + shift), Point.of(914 + 261, 730 + shift));
                inputPoint = transformation.transform(Point.of(822 + 261, 719 + shift));
                silverArea = transformation.transform(Point.of(790 + 261, 775 + shift), Point.of(798 + 261, 783 + shift));
                foodArea = transformation.transform(Point.of(790 + 261, 775 + 35 + shift), Point.of(798 + 261, 783 + 35 + shift));
                trainButtonPoint = transformation.transform(Point.of(864 + 261, 814 + shift));
                silverPoint = transformation.transform(Point.of(1005, 780 + shift));
                foodPoint = transformation.transform(Point.of(1005, 814 + shift));
                break;
            case G1_MOUNTED, G2_MOUNTED, G3_MOUNTED, G4_MOUNTED, G5_MOUNTED,
                 G6_MELEE, G7_MELEE, G8_MELEE,
                 DRAGON_V, ELEMENTAL_V, GIANT_V, BEAST_V,
                 S5_DEADSHOT, S6_RANGED, S6_MELEE, DRAGON_VIII, ELEMENTAL_VIII, GIANT_VIII, BEAST_VIII:

                if (Sets.newHashSet(Unit.DRAGON_VIII, Unit.ELEMENTAL_VIII, Unit.GIANT_VIII, Unit.BEAST_VIII).contains(unit)) {
                    shift = 41;
                }
                inputArea = transformation.transform(Point.of(817 + 523, 711 + shift), Point.of(914 + 523, 730 + shift));
                inputPoint = transformation.transform(Point.of(822 + 523, 719 + shift));
                silverArea = transformation.transform(Point.of(790 + 522, 775 + shift), Point.of(798 + 522, 783 + shift));
                foodArea = transformation.transform(Point.of(790 + 522, 775 + 35 + shift), Point.of(798 + 522, 783 + 35 + shift));
                trainButtonPoint = transformation.transform(Point.of(864 + 522, 814 + shift));
                silverPoint = transformation.transform(Point.of(1268, 780 + shift));
                foodPoint = transformation.transform(Point.of(1268, 814 + shift));
                break;

            default:
                throw new RuntimeException("Not implemented for unit " + unit.name());
        }
        
        if (treatCurrentInputFieldValue(robot.captureScreen(inputArea)) > quantity) {
            robot.leftClick(inputPoint);
            robot.sleep(250);
            robot.typeString(String.valueOf(quantity));
            robot.sleep(1000);
        }

        // handleResourcesIfNeeded(unit, silverPoint, silverArea, foodPoint, foodArea);
        robot.mouseMove(titleBarracksPoint);
        if (!isResourceEnough(silverArea)) {
            robot.leftClick(silverPoint);
            fillResource();
        }
        robot.mouseMove(titleBarracksPoint);
        if (!isResourceEnough(foodArea)) {
            robot.leftClick(foodPoint);
            fillResource();
        }

        int target;
        int counter = 0;
        boolean continueTrying = true;

        do {
            target = Math.max((int) Math.round(quantity / Math.pow(2, counter)), 1);

            if (treatCurrentInputFieldValue(robot.captureScreen(inputArea)) > target) {
                robot.leftClick(titleBarracksPoint); // I need to take the focus from the text area
                robot.leftClick(inputPoint);
                robot.sleep(250);
                robot.typeString(String.valueOf(target));
                robot.sleep(1000);
            }
            
            counter = counter + 1;

            robot.mouseMove(titleBarracksPoint);
            if (isResourceEnough(silverArea) && isResourceEnough(foodArea)) {
                
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
                lockService.lock(player, Scenario.FINISHED_TRAINING_ALL_TROOPS, LocalDateTime.now().plusHours(1));
            }
            
        } while(continueTrying);
    }

    private boolean isResourceEnough(Area area) {
        BufferedImage screen = robot.captureScreen();

        BufferedImage colorOkImage = ImageUtil.loadResource("player/barracks/color_ok.png");

        //ImageUtil.showImageAndWait(screen, area);
        
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
                        .waitLimit(2000)
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


    public void fillResource() {

        Navigate navigateTitle = Navigate.builder()
                .resourceName("player/barracks/title_detail_fill_rss.png")
                .areaName("BARRACKS_FILL_RSS_DETAIL_TITLE")
                .waitLimit(5000)
                .build()
                .ensureExistence();

        Transformation transformation = Transformation.builder()
                .reference(navigateTitle.getPoint())
                .real(Point.of(973, 340))
                .build();
        
        int c = 0;
        
        do {
            Navigate navigateUseButton = Navigate.builder()
                    .resourceName("player/button_use.png")
                    .area(transformation.transform(Point.of(1135, 535), Point.of(1181, 566)))
                    .build();

            if (!navigateUseButton.exist()) {
                robot.type(KeyEvent.VK_ESCAPE);
                robot.sleep(300);

                System.out.println("No resource available!");
                return;
            }

            navigateUseButton.leftClick();
            
            playOpenBoostersPopUp();
            
            c++;
            
            if (c >= 50) {
                throw new RuntimeException("Stuck?");
            }
            
        } while(navigateTitle.searchAgain().isPresent());
    }

    public void playOpenBoostersPopUp() {
        Navigate navigateOpenBoostersTitle = Navigate.builder()
                .resourceName("player/barracks/title_open_boosters.png")
                .areaName("BARRACKS_OPEN_BOOSTERS_TITLE")
                .build();
        
        if (!navigateOpenBoostersTitle.exist()) {
            return;
        }
        
        Transformation transformation = Transformation.builder()
                .reference(navigateOpenBoostersTitle.getPoint())
                .real(Point.of(939, 422))
                .build();
        
        Area area = transformation.transform(Point.of(974, 654), Point.of(1034, 677));
        int resourceAmount = OcrUtil.ocrWithMultiplier(robot.captureScreen(area), "4C2727");
        
        if (resourceAmount < 1000000) {
            robot.leftClick(transformation.transform(1142, 722)); // Use all!
        }
        
        robot.leftClick(transformation.transform(1054, 768)); // Click on USse Button
    }


    private void selectUnit(Point titleBarracksPoint, Unit unit) {
        
        if (this.lastSelected == unit) {
            return;
        }
        
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
    
                    // It seems it has to click twice ?1?1?
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 454)));
                    robot.sleep(wait);
    
                    // Click on Tier
                    tierPos = 458 + ((unit.getTier() - 1) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
                    robot.sleep(wait);
                    break;
    
                case G6_RANGED, G7_RANGED:
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
    
                    // It seems it has to click twice ?1?1?
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 454)));
                    robot.sleep(wait);
    
                    // Click on Tier
                    tierPos = 458 + ((unit.getTier() - 1) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(951, tierPos)));
                    robot.sleep(wait);
                    break;
                    
                case G6_MELEE, G7_MELEE:
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
    
                    // It seems it has to click twice ?1?1?
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 454)));
                    robot.sleep(wait);
    
                    // Click on Tier
                    tierPos = 458 + ((unit.getTier() - 1) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1212, tierPos)));
                    robot.sleep(wait);
                    break;
                
                case G6_MOUNTED, G7_MOUNTED:
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

                case G8_MOUNTED:
                    // Click on Guardsman left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                    robot.sleep(wait);

                    // Scroll to the correct position
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 861)));
                    robot.sleep(wait);

                    // Click on Tier
                    tierPos = 502 + ((unit.getTier() - 8) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
                    robot.sleep(wait);
                    break;

                case G8_COURAX:
                    // Click on Guardsman left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                    robot.sleep(wait);

                    // Scroll to the correct position
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 861)));
                    robot.sleep(wait);

                    // Click on Tier
                    tierPos = 502 + ((unit.getTier() - 8) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(951, tierPos)));
                    robot.sleep(wait);
                    break;

                case G8_RANGED:
                    // Click on Guardsman left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                    robot.sleep(wait);

                    // Scroll to the correct position
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 734)));
                    robot.sleep(wait);

                    // Click on Tier
                    tierPos = 458 + ((unit.getTier() - 8) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(951, tierPos)));
                    robot.sleep(wait);
                    break;

                case G8_MELEE:
                    // Click on Guardsman left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                    robot.sleep(wait);

                    // Scroll to the correct position
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 734)));
                    robot.sleep(wait);

                    // Click on Tier
                    tierPos = 458 + ((unit.getTier() - 8) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1212, tierPos)));
                    robot.sleep(wait);
                    break;

                case G5_GRIFFIN, G6_GRIFFIN, G7_GRIFFIN:
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
                    
                case S5_DEADSHOT, S6_RANGED:
                    // Click on Specialists left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 435)));
                    robot.sleep(wait);
    
                    // Click on Tier
                    tierPos = 458 + ((unit.getTier() - 5) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1212, tierPos)));
                    robot.sleep(wait);
                    break;
    
                case S5_LION_RIDER, S6_MOUNTED:
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
    
                case S6_MELEE:
                    // Click on Specialists left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 435)));
                    robot.sleep(wait);
    
                    // Scroll to second line
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 607)));
                    robot.sleep(wait);
    
                    // Click on Tier
                    tierPos = 458 + ((unit.getTier() - 6) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1212, tierPos)));
                    robot.sleep(wait);
                    break;
                    
    
                case S5_VULTURE, S6_FLYING:
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
    
                case S6_SPY:
                    // Click on Specialists left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 435)));
                    robot.sleep(wait);
    
    
                    // Scroll to second line
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 734)));
                    robot.sleep(wait);
    
                    // Click on Tier
                    tierPos = 458 + ((unit.getTier() - 6) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
                    robot.sleep(wait);
                    break;
                    
                case DRAGON_III, DRAGON_IV, DRAGON_V:
                    // Click on Dragons left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 538)));
                    robot.sleep(wait);
    
                    // Scroll up (player G6 needs to scroll up)
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 454)));
                    robot.sleep(wait);
                    break;
    
                case ELEMENTAL_III, ELEMENTAL_IV, ELEMENTAL_V:
                    // Click on Elementals left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 590)));
                    robot.sleep(wait);
    
                    // Scroll up (player G6 needs to scroll up)
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 454)));
                    robot.sleep(wait);
                    break;
    
                case GIANT_III, GIANT_IV, GIANT_V:
                    // Click on Giants left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 642)));
                    robot.sleep(wait);
    
                    // Scroll up (player G6 needs to scroll up)
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 454)));
                    robot.sleep(wait);
                    break;
    
                case BEAST_III, BEAST_IV, BEAST_V:
                    // Click on Beats left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 694)));
                    robot.sleep(wait);
    
                    // Scroll up (player G6 needs to scroll up)
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 454)));
                    robot.sleep(wait);
                    break;
    
                case DRAGON_VI, DRAGON_VII, DRAGON_VIII :
                    // Click on Dragons left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 538)));
                    robot.sleep(wait);
                    break;
    
                case ELEMENTAL_VI, ELEMENTAL_VII, ELEMENTAL_VIII:
                    // Click on Elementals left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 590)));
                    robot.sleep(wait);
                    break;
    
                case GIANT_VI, GIANT_VII, GIANT_VIII:
                    // Click on Giants left tab
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 642)));
                    robot.sleep(wait);
                    break;
    
                case BEAST_VI, BEAST_VII, BEAST_VIII:
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
                    
                case EC6_ENGINEER, EC7_ENGINEER:
                    // Click on Engineer corps
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 486)));
                    robot.sleep(wait);
    
                    // Click on Tier
                    tierPos = 458 + ((unit.getTier() - 6) * 26);
                    robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(951, tierPos)));
                    robot.sleep(wait);
                    break;
                default:
                    throw new RuntimeException("Not implemented for unit " + unit.name());
            }
            this.lastSelected = unit;
    }

    private int getCurrentUnitNumber(Point titleBarracksPoint, Unit unit) {
        Area area;

        selectUnit(titleBarracksPoint, unit);
        
        int shift = 0;

        switch (unit) {
            case G1_RANGED, G2_RANGED, G3_RANGED, G4_RANGED, G5_RANGED,
                 S1_SWORDSMAN, S2_SWORDSMAN, S3_SWORDSMAN, S4_SWORDSMAN, S5_SWORDSMAN,
                 G6_MOUNTED, G7_MOUNTED, G8_MOUNTED,
                 DRAGON_III, ELEMENTAL_III, GIANT_III, BEAST_III,
                 DRAGON_VI, ELEMENTAL_VI, GIANT_VI, BEAST_VI,
                 G5_GRIFFIN, G6_GRIFFIN, G7_GRIFFIN,
                 EC1_ENGINEER, EC2_ENGINEER, EC3_ENGINEER, EC4_ENGINEER, EC5_ENGINEER,
                 S5_LION_RIDER, S6_MOUNTED, S6_SPY:
                
                if (Sets.newHashSet(Unit.DRAGON_VI, Unit.ELEMENTAL_VI, Unit.GIANT_VI, Unit.BEAST_VI, Unit.G8_MOUNTED).contains(unit)) {
                    shift = 41;
                }
                
                area = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(852, 677 + shift), Point.of(912, 699 + shift));
                break;
            case G1_MELEE, G2_MELEE, G3_MELEE, G4_MELEE, G5_MELEE,
                 S1_SPY, S2_SPY, S3_SPY, S4_SPY, S5_SPY,
                 G6_RANGED, G7_RANGED, G8_RANGED,
                 DRAGON_IV, ELEMENTAL_IV, GIANT_IV, BEAST_IV,
                 DRAGON_VII, ELEMENTAL_VII, GIANT_VII, BEAST_VII,
                 S5_VULTURE, S6_FLYING, EC6_ENGINEER, EC7_ENGINEER, 
                 G8_COURAX:
                
                if (Sets.newHashSet(Unit.DRAGON_VII, Unit.ELEMENTAL_VII, Unit.GIANT_VII, Unit.BEAST_VII, Unit.G8_COURAX).contains(unit)) {
                    shift = 41;
                }
                area = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(852 + 261, 677 + shift), Point.of(912 + 261, 699 + shift));
                break;
            case G1_MOUNTED, G2_MOUNTED, G3_MOUNTED, G4_MOUNTED, G5_MOUNTED,
                 G6_MELEE, G7_MELEE, G8_MELEE,
                 DRAGON_V, ELEMENTAL_V, GIANT_V, BEAST_V,
                 S5_DEADSHOT, S6_RANGED, S6_MELEE,
                 DRAGON_VIII, ELEMENTAL_VIII, GIANT_VIII, BEAST_VIII :
                if (Sets.newHashSet(Unit.DRAGON_VIII, Unit.ELEMENTAL_VIII, Unit.GIANT_VIII, Unit.BEAST_VIII).contains(unit)) {
                    shift = 41;
                }
                area = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(852 + 522, 677 + shift), Point.of(912 + 522, 699 + shift));
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
