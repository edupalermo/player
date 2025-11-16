package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.*;
import org.palermo.totalbattle.player.bean.SpeedUpBean;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class BuildArmy {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public BuildArmy(Player player) {
        this.player = player;
    }

    public void buildArmy() {
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

        BufferedImage buttonHelp = ImageUtil.loadResource("player/barracks/button_help.png");
        Point buttonHelpPoint = ImageUtil.search(buttonHelp, screen, buttonArea, 0.1).orElse(null);
        if (buttonHelpPoint != null) {
            robot.leftClick(buttonHelpPoint, buttonHelp);
            robot.sleep(200);
        }
        else {
            BufferedImage iconHourglass = ImageUtil.loadResource("player/barracks/icon_hourglass.png");
            Area iconHourglassArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(919, 376), Point.of(995, 397));
            Point iconHourglassPoint = ImageUtil.searchSurroundings(iconHourglass, screen, iconHourglassArea, 0.1, 20).orElse(null);

            if (iconHourglassPoint != null) {
                BufferedImage timeLeft = ImageUtil.crop(screen, Area.of(iconHourglassPoint, 18, -2, 92, 18));
                String timeLeftAsText = treatTimeLeft(timeLeft);
                System.out.println("Time Left: " + timeLeftAsText);

                LocalDateTime nextLocalDateTime = TimeLeftUtil.parse(timeLeftAsText).orElse(null);
                if (nextLocalDateTime == null) {
                    throw new RuntimeException("Failed to parse time left: " + timeLeftAsText);
                }

                // Click of the speed-up button
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1174, 390)));
                robot.sleep(350);

                speedUp(nextLocalDateTime);
            }
            else { // No Hourglass and No Help button
                BufferedImage buttonComplete = ImageUtil.loadResource("player/barracks/button_complete.png");
                Point buttonCompletePoint = ImageUtil.search(buttonComplete, screen, buttonArea, 0.1).orElse(null);

                if (buttonCompletePoint != null) {
                    robot.leftClick(buttonCompletePoint, buttonComplete);
                    robot.sleep(200);
                }

                chooseTroopToBuild(player, titleBarracksPoint);
            }
        }



        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(150);
    }
    
    private String treatTimeLeft(BufferedImage input) {
        BufferedImage timeLeft = ImageUtil.toGrayscale(input);
        timeLeft = ImageUtil.invertGrayscale(timeLeft);
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        if (timeLeft.getHeight() < 50) {
            timeLeft = ImageUtil.resize(timeLeft, 50);
        }
        
        return ImageUtil.ocrBestMethod(timeLeft, ImageUtil.WHITELIST_FOR_COUNTDOWN);
    }


    private final List<SpeedUpBean> speedUps = new ArrayList<>();
    {
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/1m.png"))
                .seconds(Duration.ofMinutes(1).getSeconds())
                .label("1m")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/15m.png"))
                .seconds(Duration.ofMinutes(15).getSeconds())
                .label("15m")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/1h.png"))
                .seconds(Duration.ofHours(1).getSeconds())
                .label("1h")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/3h.png"))
                .seconds(Duration.ofHours(3).getSeconds())
                .label("3h")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/8h.png"))
                .seconds(Duration.ofHours(8).getSeconds())
                .label("8h")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/15h.png"))
                .seconds(Duration.ofHours(15).getSeconds())
                .label("15h")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/1d.png"))
                .seconds(Duration.ofDays(1).getSeconds())
                .label("1d")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/3d.png"))
                .seconds(Duration.ofDays(3).getSeconds())
                .label("3d")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/7d.png"))
                .seconds(Duration.ofDays(7).getSeconds())
                .label("7d")
                .build());
        speedUps.add(SpeedUpBean.builder()
                .image(ImageUtil.loadResource("player/speed_up/30d.png"))
                .seconds(Duration.ofDays(30).getSeconds())
                .label("30d")
                .build());
    }


    private void speedUp(LocalDateTime dateTime) {

        long seconds = Duration.between(LocalDateTime.now(), dateTime).getSeconds();

        BufferedImage screen = robot.captureScreen();
        BufferedImage speedUpsTitle = ImageUtil.loadResource("player/speed_up/title_speed_ups.png");
        Area speedUpsTitleArea = Area.fromTwoPoints(910, 325, 1066, 361);
        Point speedUpsTitlePoint = ImageUtil.search(speedUpsTitle, screen, speedUpsTitleArea, 0.1).orElse(null);
        if (speedUpsTitlePoint == null) {
            throw new RuntimeException("Could not find speed up title");
        }

        final int turns = 3;

        for (int r = 0; r < turns; r++) {

            if (r != 0) {
                robot.leftClick(Point.of(speedUpsTitlePoint, Point.of(958, 346), Point.of(1258, 494)));
                robot.sleep(500);
            }

            SpeedUpBean bestSpeedUp = null;

            for (SpeedUpBean bean : speedUps) {
                if (bean.getSeconds() < seconds) {
                    if (bestSpeedUp == null) {
                        bestSpeedUp = bean;
                    }
                    else if (bean.getSeconds() > bestSpeedUp.getSeconds()) {
                        bestSpeedUp = bean;
                    }
                }
            }

            if (bestSpeedUp == null) {
                System.out.println("Shouldn't use speed ups!");
                break;
            }


            Area searchArea = Area.of(speedUpsTitlePoint, Point.of(958, 346), Point.of(749, 463), Point.of(797, 780));
            BufferedImage buttonUse = ImageUtil.loadResource("player/speed_up/button_use.png");

            Point scrollPoint = Point.of(speedUpsTitlePoint, Point.of(958, 346), Point.of(1258, 494));

            log.info("Searching for {}", bestSpeedUp.getLabel());

            for (int i = 0; i < 4; i++) {
                screen = robot.captureScreen();
                Point speedUpPoint = ImageUtil.search(bestSpeedUp.getImage(), screen, searchArea, 0.05).orElse(null);
                if (speedUpPoint != null) {
                    Area useButtonArea = Area.of(speedUpPoint, 376, 42, 54, 26);
                    Point buttonUsePoint = ImageUtil.search(buttonUse, screen, useButtonArea, 0.1).orElse(null);
                    if (buttonUsePoint == null) {
                        log.info("Speed up {} not available", bestSpeedUp.getLabel());
                        return;
                    }
                    log.info("Speed up {} is available!!", bestSpeedUp.getLabel());
                    robot.leftClick(buttonUsePoint, buttonUse);
                    robot.sleep(200);

                    seconds = seconds - bestSpeedUp.getSeconds();
                    break;

                }
                else {
                    robot.mouseDrag(scrollPoint, 0, 150);
                    robot.sleep(150);
                    scrollPoint = scrollPoint.move(0, 150);
                }
            }
        }

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }


    private void chooseTroopToBuild(Player player, Point titleBarracksPoint) {

        Map<Unit, Long> map = SharedData.INSTANCE.getTroopTarget(player);

        boolean trainedSomething = false;

        for (Map.Entry<Unit, Long> entry : map.entrySet()) {
            try {
                long currentSize = getCurrentUnitNumber(titleBarracksPoint, entry.getKey());
                if (currentSize < entry.getValue().longValue()) {
                    train(titleBarracksPoint, entry.getKey(), entry.getValue().longValue() - currentSize);
                    trainedSomething = true;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                train(titleBarracksPoint, entry.getKey(), 1);
                trainedSomething = true;
                break;
            }
        }

        if (!trainedSomething) {
            SharedData.INSTANCE.setWait(player, Scenario.TRAIN_TROOPS, LocalDateTime.now().plusHours(1));
        }
    }

    private void train(Point titleBarracksPoint, Unit unit, long quantity) {

        selectUnit(titleBarracksPoint, unit);

        Point textPoint = null;
        Area silverArea = null;
        Area foodArea = null;
        Point trainButtonPoint = null;

        Point silverPoint = null;
        Point foodPoint = null;


        switch (unit) {
            case G1_RANGED, G2_RANGED, G3_RANGED, G4_RANGED, G5_RANGED, S1_SWORDSMAN, S2_SWORDSMAN, S3_SWORDSMAN, S4_SWORDSMAN, G5_GRIFFIN,
                    EMERALD_DRAGON, WATER_ELEMENTAL, STONE_GARGOYLE, BATTLE_BOAR:
                textPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(822, 719));
                silverArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790, 775), Point.of(798, 783));
                foodArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790, 775 + 35), Point.of(798, 783 + 35));
                trainButtonPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(864, 814));
                silverPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(745, 780));
                break;
            case G1_MELEE, G2_MELEE, G3_MELEE, G4_MELEE, G5_MELEE,
                    MAGIC_DRAGON, ICE_PHOENIX, MANY_ARMED_GUARDIAN, GORGON_MEDUSA:
                textPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(822 + 261, 719));
                silverArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790 + 261, 775), Point.of(798 + 261, 783));
                foodArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790 + 261, 775 + 35), Point.of(798 + 261, 783 + 35));
                trainButtonPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(864 + 261, 814));
                silverPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1005, 780));
                break;
            case G1_MOUNTED, G2_MOUNTED, G3_MOUNTED, G4_MOUNTED, G5_MOUNTED,
                    DESERT_VANQUISER, FLAMING_CENTAUR, ETTIN, FEARSOME_MANTICORE:
                textPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(822 + 523, 719));
                silverArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790 + 522, 775), Point.of(798 + 522, 783));
                foodArea = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(790 + 522, 775 + 35), Point.of(798 + 522, 783 + 35));
                trainButtonPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(864 + 522, 814));
                silverPoint = Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1268, 780));
                break;

            default:
                throw new RuntimeException("Not implemented for unit " + unit.name());
        }

        if (textPoint == null) {
            throw new RuntimeException("Not implemented for " + unit.name());
        }

        robot.leftClick(textPoint);
        robot.typeString(String.valueOf(quantity));
        robot.sleep(250);

        BufferedImage screen = robot.captureScreen();

        BufferedImage colorOkImage = ImageUtil.loadResource("player/barracks/color_ok.png");

        if (ImageUtil.search(colorOkImage, screen, silverArea, 0.1).isEmpty()) {
            fillSilver(silverPoint);
            return;
        }

        if (ImageUtil.search(colorOkImage, screen, foodArea, 0.1).isEmpty()) {
            System.out.println("Not enough food!");
            return;
        }

        // Click on train button
        robot.leftClick(trainButtonPoint);
        robot.sleep(1500);

        // Click on help button
        robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1174, 390)));
        robot.sleep(350);
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
            }

            Navigate.builder()
                    .areaName("TOP_UP_SILVER_SLIDE_SUBSEQUENT_USE_BUTTON")
                    .resourceName("player/button_use.png")
                    .build()
                    .leftClick();

            stillHasSavedResources = buttonUsePoint != null;

        } while(stillHasSavedResources);

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }

    private void selectUnit(Point titleBarracksPoint, Unit unit) {
        long tierPos;

        long wait = 350;

        switch (unit) {
            case G1_RANGED, G2_RANGED, G3_RANGED, G4_RANGED, G5_RANGED :
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 1) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
                robot.sleep(wait);
                break;
            case G1_MELEE, G2_MELEE, G3_MELEE, G4_MELEE, G5_MELEE :
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 1) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(951, tierPos)));
                robot.sleep(wait);
                break;
            case G1_MOUNTED, G2_MOUNTED, G3_MOUNTED, G4_MOUNTED, G5_MOUNTED :
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 1) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1212, tierPos)));
                robot.sleep(wait);
                break;

            case G5_GRIFFIN:
                // Click on Guardsman left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 382)));
                robot.sleep(wait);

                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(1463, 607)));
                robot.sleep(wait);
                break;

            case S1_SWORDSMAN, S2_SWORDSMAN, S3_SWORDSMAN, S4_SWORDSMAN:
                // Click on Specialists left tab
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(579, 435)));
                robot.sleep(wait);

                // Click on Tier
                tierPos = 458 + ((unit.getTier() - 1) * 26);
                robot.leftClick(Point.of(titleBarracksPoint, Point.of(961, 324), Point.of(689, tierPos)));
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
            default:
                throw new RuntimeException("Not implemented!");
        }
    }

    private long getCurrentUnitNumber(Point titleBarracksPoint, Unit unit) {
        Area area = null;

        selectUnit(titleBarracksPoint, unit);

        switch (unit) {
            case G1_RANGED, G2_RANGED, G3_RANGED, G4_RANGED, G5_RANGED, S1_SWORDSMAN, S2_SWORDSMAN, S3_SWORDSMAN, S4_SWORDSMAN,
                    EMERALD_DRAGON, WATER_ELEMENTAL, STONE_GARGOYLE, BATTLE_BOAR, G5_GRIFFIN:
                area = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(852, 677), Point.of(912, 699));
                break;
            case G1_MELEE, G2_MELEE, G3_MELEE, G4_MELEE, G5_MELEE,
                    MAGIC_DRAGON, ICE_PHOENIX, MANY_ARMED_GUARDIAN, GORGON_MEDUSA:
                area = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(852 + 261, 677), Point.of(912 + 261, 699));
                break;
            case G1_MOUNTED, G2_MOUNTED, G3_MOUNTED, G4_MOUNTED, G5_MOUNTED,
                    DESERT_VANQUISER, FLAMING_CENTAUR, ETTIN, FEARSOME_MANTICORE:
                area = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(852 + 522, 677), Point.of(912 + 522, 699));
                break;
                /*
            case MAGIC_DRAGON:
                area = Area.of(titleBarracksPoint, Point.of(961, 324), Point.of(862 + 261, 677), Point.of(912 + 261, 699));
                break;
                 */
            default:
                throw new RuntimeException("Not Implemented!");
        }

        if (area == null) {
            throw new RuntimeException("Not implemented for " + unit.name());
        }

        robot.sleep(200);
        BufferedImage screen = robot.captureScreen();
        BufferedImage quantityImage = ImageUtil.crop(screen, area);
        quantityImage = ImageUtil.toGrayscale(quantityImage);
        quantityImage = ImageUtil.invertGrayscale(quantityImage);
        quantityImage = ImageUtil.linearNormalization(quantityImage);

        long quantity = 0;
        try {
            String quantityAsString = ImageUtil.ocrBestMethod(quantityImage, ImageUtil.WHITELIST_FOR_ONLY_NUMBERS);
            System.out.println("Quantity of " + unit.name() + " - " + quantityAsString);

            quantity = Long.parseLong(quantityAsString);
        } catch (NumberFormatException e) {
            ImageUtil.showImageFor5Seconds(quantityImage, "Fail to get numbers from it");
            throw new RuntimeException(e);
        }

        return quantity;
    }
}
