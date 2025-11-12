package org.palermo.totalbattle.player;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.palermo.totalbattle.player.bean.SpeedUpBean;
import org.palermo.totalbattle.player.task.SummoningCircle;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.ImageUtil;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Task {

    private static MyRobot robot = SharedData.INSTANCE.robot;
    
    private static Map<String, Player> players = new HashMap<>();
    static {
        players.put("Palermo", Player.builder()
                .name("Palermo")
                .profileFolder("chrome-profiles/fp2268@gmail.com")
                .username("fp2268@gmail.com")
                .password("Alemanha79")
                .build());
        players.put("Peter II", Player.builder()
                .name("Peter II")
                .profileFolder("chrome-profiles/peter_ii")
                .username("edupalermo@gmail.com")
                .password("Alemanha79")
                .build());
        players.put("Mightshaper", Player.builder()
                .name("Mightshaper")
                .profileFolder("chrome-profiles/mightshaper")
                .username("edupalermo+01@gmail.com")
                .password("Alemanha79")
                .build());
        players.put("Grirana", Player.builder()
                .name("Grirana")
                .profileFolder("chrome-profiles/grirana")
                .username("edupalermo+02@gmail.com")
                .password("Alemanha79")
                .build());
        players.put("Elanin", Player.builder()
                .name("Elanin")
                .profileFolder("chrome-profiles/elanin")
                .username("edupalermo+03@gmail.com")
                .password("Alemanha79")
                .build());
        
    }


    public static void main(String[] args) {
        
        Player player = players.get("Palermo");
        
        WebDriver driver = null;
        try {
            driver = openBrowser(player);
            login(player);
            
            // attackArena(Point.of(351, 447));
            
            // collectChests(); // Retrieve chests
            
            //quests(player); // Retrieve open chests

            (new SummoningCircle(robot, player)).evaluate();
            
            // buildArmy(player);
            
            //freeSale(player);
            
            //helpClanMembers();
            
            waitUntilWindowIsClosed(driver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
    

    public static void buildArmy(Player player) {
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
                timeLeft = ImageUtil.toGrayscale(timeLeft);
                timeLeft = ImageUtil.invertGrayscale(timeLeft);
                timeLeft = ImageUtil.linearNormalization(timeLeft);
                String timeLeftAsText = ImageUtil.ocr(timeLeft, ImageUtil.WHITELIST_FOR_COUNTDOWN, ImageUtil.SINGLE_LINE_MODE);
                System.out.println("Time Left: " + timeLeftAsText);

                LocalDateTime nextLocalDateTime = calculateNext(timeLeftAsText).orElse(null);
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
    
    
    private static final List<SpeedUpBean> speedUps = new ArrayList<>();
    static {
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
    
    
    private static void speedUp(LocalDateTime dateTime) {
        
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
    
    
    private static void chooseTroopToBuild(Player player, Point titleBarracksPoint) {

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
    
    private static void train(Point titleBarracksPoint, Unit unit, long quantity) {

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
    
    private static void fillSilver(Point point) {
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

            stillHasSavedResources = buttonUsePoint != null;
            
        } while(stillHasSavedResources);

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
    
    private static void selectUnit(Point titleBarracksPoint, Unit unit) {
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

    private static long getCurrentUnitNumber(Point titleBarracksPoint, Unit unit) {
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
            String quantityAsString = ImageUtil.ocr(quantityImage, ImageUtil.WHITELIST_FOR_ONLY_NUMBERS, ImageUtil.SINGLE_WORD_MODE);
            System.out.println("Quantity of " + unit.name() + " - " + quantityAsString);

            quantity = Long.parseLong(quantityAsString);
        } catch (NumberFormatException e) {
            ImageUtil.showImageFor5Seconds(quantityImage, "Fail to get numbers from it");
            throw new RuntimeException(e);
        }

        return quantity;
    }


    public static void quests(Player player) {
        BufferedImage screen = robot.captureScreen();

        BufferedImage labelQuestes = ImageUtil.loadResource("player/label_quests.png");
        Point labelQuestesPoint = ImageUtil.searchSurroundings(labelQuestes, screen, 0.1, 20).orElse(null);

        if (labelQuestesPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(labelQuestes, "error_image.png");
            throw new RuntimeException("Couldn't find quests label!");
        }

        // Click on the Quests icon
        robot.leftClick(labelQuestesPoint.move(14, -30));
        robot.sleep(1000);

        // Tem que checar se tem ouro
        if (!SharedData.INSTANCE.shouldWait(player, Scenario.QUESTS_TRY_FULL_CHESTS))  {

            List<Point> chests = new ArrayList<Point>();
            chests.add(Point.of(910, 620));
            chests.add(Point.of(990, 620));
            chests.add(Point.of(1068, 620));
            chests.add(Point.of(1144, 620));
            chests.add(Point.of(1220, 620));
            chests.add(Point.of(1304, 620));

            for (Point point : chests) {
                robot.leftClick(point);
                robot.sleep(450);
            }
            robot.sleep(3500); // Wait toast to disappear
            SharedData.INSTANCE.setWait(player, Scenario.QUESTS_TRY_FULL_CHESTS, LocalDateTime.now().plusHours(2));
        }

        screen = robot.captureScreen();
        BufferedImage weeklyReward = ImageUtil.loadResource("player/label_weekly_reward.png");
        Area weeklyRewardArea = RegionSelector.selectArea("QUESTS_DAILY_QUESTS_WEEKLY_REWARD", screen);
        Point weeklyRewardPoint = ImageUtil.searchSurroundings(weeklyReward, screen, weeklyRewardArea, 0.1, 20).orElse(null);

        if (weeklyRewardPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(labelQuestes, "error_image.png");
            throw new RuntimeException("Couldn't find weekly reward label!");
        }

        Area claimArea = Area.of(weeklyRewardPoint, Point.of(1022, 366), Point.of(1238, 750), Point.of(1293, 770));
        BufferedImage buttonClaim = ImageUtil.loadResource("player/button_wr_claim.png");
        Point buttonClaimPoint = ImageUtil.search(buttonClaim, screen, claimArea, 0.1).orElse(null);

        if (buttonClaimPoint != null) {
            robot.leftClick(buttonClaimPoint, buttonClaim);
        }

        // Daily Jobs Tab
        robot.leftClick(weeklyRewardPoint.move(-310, 65));
        robot.sleep(300);

        screen = robot.captureScreen();
        BufferedImage refDailyJobs = ImageUtil.loadResource("player/ref_daily_jobs.png");
        Point refDailyJobsPoint = ImageUtil.searchSurroundings(refDailyJobs, screen, 0.1, 20).orElse(null);

        if (refDailyJobsPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(refDailyJobs, "error_image.png");
            throw new RuntimeException("Couldn't find Daily Jobs reference!");
        }


        Area topButtonArea = Area.of(refDailyJobsPoint, Point.of(980, 320), Point.of(1218, 391), Point.of(1298, 416));
        BufferedImage claimButton = ImageUtil.loadResource("player/button_dj_claim.png");
        Point claimButtonPoint = ImageUtil.search(claimButton, screen, topButtonArea, 0.1).orElse(null);

        if (claimButtonPoint != null) {
            robot.leftClick(claimButtonPoint, claimButton);
            robot.sleep(500);
        }

        screen = robot.captureScreen();
        BufferedImage speedUpButton = ImageUtil.loadResource("player/button_dj_speed_up.png");
        Point speedUpButtonPoint = ImageUtil.search(speedUpButton, screen, topButtonArea, 0.1).orElse(null);
        if (speedUpButtonPoint == null) {

            Area area = Area.of(refDailyJobsPoint, Point.of(980, 320), Point.of(1237, 496), Point.of(1297, 517));
            BufferedImage claimStart = ImageUtil.loadResource("player/button_dj_start.png");
            Point startButtonPoint = ImageUtil.search(claimStart, screen, area, 0.1).orElse(null);

            if (startButtonPoint != null) {
                robot.leftClick(startButtonPoint, claimStart);
                robot.sleep(500);
            }
        }

        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
    
    public static void freeSale(Player player) {
        BufferedImage screen = robot.captureScreen();

        BufferedImage logoTotalBattle = ImageUtil.loadResource("player/logo_total_battle.png");
        Point logoTotalBattlePoint = ImageUtil.searchSurroundings(logoTotalBattle, screen, 0.1, 20).orElse(null);

        if (logoTotalBattlePoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(logoTotalBattle, "error_image.png");
            throw new RuntimeException("Couldn't find english label!");
        }

        // Click on the Free Sale Icon
        robot.leftClick(logoTotalBattlePoint.move(1870 - logoTotalBattlePoint.getX(), 73));
        robot.sleep(1000);

        screen = robot.captureScreen();

        BufferedImage refBonusSales = ImageUtil.loadResource("player/ref_bonus_sales.png");
        Point refBonusSalesPoint = ImageUtil.searchSurroundings(refBonusSales, screen, 0.1, 20).orElse(null);

        if (refBonusSalesPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(refBonusSales, "error_image.png");
            throw new RuntimeException("Couldn't Bonus Sales reference!");
        }
        
        
        List<Point> tabs = new ArrayList<>();
        tabs.add(Point.of(78, 60));
        tabs.add(Point.of(78, 111));
        tabs.add(Point.of(78, 166));
        tabs.add(Point.of(78, 218));
        tabs.add(Point.of(78, 271));
        tabs.add(Point.of(78, 321));
        tabs.add(Point.of(78, 374));
        
        // Area area = Area.of(refBonusSalesPoint, Point.of(66, 404), Point.of(377, 873), Point.of(425, 896));
        Area area = Area.of(refBonusSalesPoint, Point.of(66, 404), Point.of(341, 873), Point.of(798, 896));

        BufferedImage buttonFree = ImageUtil.loadResource("player/button_bs_free.png");
        BufferedImage iconHourglass = ImageUtil.loadResource("player/icon_bs_hourglass.png");

        for (Point move: tabs) {
            robot.leftClick(refBonusSalesPoint.move(move.getX(), move.getY()));
            robot.sleep(750);

            screen = robot.captureScreen();

            Point buttonFreePoint = ImageUtil.search(buttonFree, screen, area, 0.1).orElse(null);

            if (buttonFreePoint != null) {
                robot.leftClick(buttonFreePoint, buttonFree);
                break;
            }

            Point iconHourglassPoint = ImageUtil.search(iconHourglass, screen, area, 0.1).orElse(null);
            if (iconHourglassPoint != null) {
                BufferedImage next = ImageUtil.crop(screen, Area.of(iconHourglassPoint.getX() + 18, iconHourglassPoint.getY(), 100, 18));
                next = ImageUtil.toGrayscale(next);
                next = ImageUtil.invertGrayscale(next);
                next = ImageUtil.linearNormalization(next);
                String nextAsText = ImageUtil.ocr(next, ImageUtil.WHITELIST_FOR_COUNTDOWN, ImageUtil.LINE_OF_PRINTED_TEXT);
                LocalDateTime nextLocalDateTime = calculateNext(nextAsText).orElse(null);
                if (nextLocalDateTime != null) {
                    SharedData.INSTANCE.setWait(player, Scenario.BONUS_SALES_FREE, nextLocalDateTime);
                    break;
                }
            }
        }

        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
    
    private static Optional<LocalDateTime> calculateNext(String input) {
        Pattern pattern = Pattern.compile("(\\d+)h[:]?([\\d+]+)m");
        Matcher matcher = pattern.matcher(input.trim());

        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        
        boolean parsed = false;

        if (matcher.matches()) {
            hours = Integer.parseInt(matcher.group(1));
            minutes = Integer.parseInt(matcher.group(2));
            parsed = true;
        }
        
        if (!parsed) {
            pattern = Pattern.compile("(\\d+)m[:]?([\\d+]+)5");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                minutes = Integer.parseInt(matcher.group(1));
                seconds = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)m[:]?([\\d+]+)s");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                minutes = Integer.parseInt(matcher.group(1));
                seconds = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)d[:]?([\\d+]+)h");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                days = Integer.parseInt(matcher.group(1));
                hours = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }
        
        if (!parsed) {
            throw new RuntimeException("Impossible to parse " + input);
        }
        
        LocalDateTime answer = LocalDateTime.now()
                .plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);
        
        return Optional.of(answer);
    }
    
    public static void collectChests() {

        BufferedImage screen = robot.captureScreen();
        
        BufferedImage iconHelpAllies = ImageUtil.loadResource("player/label_clan.png");
        Point iconHelpAlliesPoint = ImageUtil.searchSurroundings(iconHelpAllies, screen, 0.1, 20).orElse(null);

        if (iconHelpAlliesPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(iconHelpAllies, "error_image.png");
            throw new RuntimeException("Couldn't find clan image!");
        }

        // Click on the clan Icon
        robot.leftClick(iconHelpAlliesPoint.move(14, -30));
        robot.sleep(1250);

        screen = robot.captureScreen();
        BufferedImage titleMyClan = ImageUtil.loadResource("player/my_clan/title_my_clan.png");
        Point titleMyClanPoint = ImageUtil.searchSurroundings(titleMyClan, screen, 0.1, 20).orElse(null);

        if (titleMyClanPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(iconHelpAllies, "error_image.png");
            throw new RuntimeException("Couldn't My Clan title!");
        }

        // Click on Gifts (left tab)
        robot.leftClick(Point.of(titleMyClanPoint, Point.of(963, 325), Point.of(611, 497)));
        robot.sleep(500);

        
        for (int i = 0; i < 2; i++ ) {
            if (i == 0) {
                // Click on Gifts  (top tab)
                robot.leftClick(Point.of(titleMyClanPoint, Point.of(963, 325), Point.of(833, 399)));
                robot.sleep(750);
            }
            else {
                // Click on Triumphal Gifts  (top tab)
                robot.leftClick(Point.of(titleMyClanPoint, Point.of(963, 325), Point.of(1074, 399)));
                robot.sleep(750);
            }

            // Click on open button while we have it
            Area buttonOpenArea = Area.of(titleMyClanPoint, Point.of(963, 325), Point.of(1358, 485), Point.of(1409, 505));
            BufferedImage buttonOpen = ImageUtil.loadResource("player/my_clan/button_open.png");
            Point buttonOpenPoint;
            do {
                screen = robot.captureScreen();
                buttonOpenPoint = ImageUtil.search(buttonOpen, screen,buttonOpenArea, 0.1).orElse(null);

                if (buttonOpenPoint != null) {
                    robot.leftClick(buttonOpenPoint, buttonOpen);
                    robot.sleep(300);
                }

            } while(buttonOpenPoint != null);
        }

        robot.sleep(500);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(500);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
    
    public static WebDriver openBrowser(Player player) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--user-data-dir=" + new java.io.File(player.getProfileFolder()).getAbsolutePath());
        options.addArguments("--profile-directory=Default"); // Default profile in that dir
        //options.addArguments("--kiosk");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);
        driver.get(getAddress());

        waitPageToBeLoaded(driver);

        System.out.println("Page has loaded");

        robot.sleep(2000);
        return driver;
    }
    
    private static String getAddress() {
        LocalDate today = LocalDate.now();
        int weekdayNumber = today.getDayOfWeek().getValue();
        
        switch (weekdayNumber) {
            case 1:
                return "https://totalbattle.com/en/?present=gold";
            case 2:
                return "https://totalbattle.com/en/?present=xp";
            case 3:
                return "https://totalbattle.com/en/?present=tar";
            case 4:
                return "https://totalbattle.com/en/?present=march25";
            case 5:
                return "https://totalbattle.com/en/?present=gold500";
            case 6:
                return "https://totalbattle.com/en/?present=speedups15";
            case 7:
                return "https://totalbattle.com/en/?present=speedups3";
        }
        throw new RuntimeException("Week day problem!");
    }
    
    public static void login(Player player) {
        BufferedImage linkLoginImage = ImageUtil.loadResource("player/link_login.png");
        Area linkLoginImageArea = Area.fromTwoPoints(347, 459, 591, 548);
        BufferedImage screen = robot.captureScreen();
        Point linkLoginPoint = ImageUtil.searchSurroundings(linkLoginImage, screen, linkLoginImageArea,0.1, 20).orElse(null);

        if (linkLoginPoint != null) {
            System.out.println("Login link found");
            login(player, linkLoginImage, linkLoginPoint);
        }
        robot.sleep(5000);
        
        System.out.println("User already logged");

        BufferedImage labelClan = ImageUtil.loadResource("player/label_clan.png");
        BufferedImage buttonBonusSalesClose = ImageUtil.loadResource("player/button_bonus_sales_close.png");
        long start = System.currentTimeMillis();
        boolean found = false;
        do {
            screen = robot.captureScreen();
            Area labelClanArea = Area.fromTwoPoints(Point.of(989, 1012), Point.of(1074, 1035));
            Point point = ImageUtil.searchSurroundings(labelClan, screen, labelClanArea, 0.12, 20).orElse(null);
            if (point != null) {
                found = true;
            }
            else {
                screen = robot.captureScreen();
                Point buttonClosepoint = ImageUtil.searchSurroundings(buttonBonusSalesClose, screen, 0.1, 20).orElse(null);
                if (buttonClosepoint != null) {
                    robot.leftClick(buttonClosepoint, buttonBonusSalesClose);
                    robot.sleep(750);
                }
                else {
                    System.out.println("Trying to hit scape to close initial pop-ups");
                    robot.type(KeyEvent.VK_ESCAPE);
                    robot.sleep(750);
                }
            }
        } while (!found && (System.currentTimeMillis() - start) < 60000);
        if (!found) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(labelClan, "error_image.png");
            throw new RuntimeException("Not found image!");
        }
        
        System.out.println("Press scape twice to close random pop ups");
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);


        // Turn music off
        Area controlsArea = Area.fromTwoPoints(1797, 1046, 1917, 1079);
        BufferedImage iconMusicOn = ImageUtil.loadResource("player/icon_music_on.png");
        screen = robot.captureScreen();
        Point iconMusicOnPoint = ImageUtil.searchSurroundings(iconMusicOn, screen, controlsArea, 0.1, 20).orElse(null);
        if (iconMusicOnPoint != null) {
            robot.leftClick(iconMusicOnPoint, iconMusicOn);
        }
        
        // Turn sound off            
        BufferedImage iconSoundOn = ImageUtil.loadResource("player/icon_sound_on.png");
        screen = robot.captureScreen();
        Point iconSoundOnPoint = ImageUtil.searchSurroundings(iconSoundOn, screen, controlsArea, 0.1, 20).orElse(null);
        if (iconSoundOnPoint != null) {
            robot.leftClick(iconSoundOnPoint, iconSoundOn);
        }
        
        robot.sleep(1500); // The help icon is not appearing
    }
    
    public static boolean attackArena(Point arenaLocation) {
        BufferedImage labelMap = ImageUtil.loadResource("player/label_map.png");
        Point labelMapPoint = waitMandatoryImage(labelMap, "Map label", 5000);
        robot.leftClick(labelMapPoint.move(12, -31));
        robot.sleep(2000);

        // When we switch to map, the Bonus Sales appear again
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(2000);

        // Click on the magnifier icon
        BufferedImage iconMagnifier = ImageUtil.loadResource("player/icon_magnifier.png");
        Point iconMagnifierPoint = waitMandatoryImage(iconMagnifier, "Magnifier icon", 10000);
        robot.leftClick(iconMagnifierPoint, iconMagnifier);
        robot.sleep(1000);

        BufferedImage buttonGo = ImageUtil.loadResource("player/button_go.png");
        Point buttonGoPoint = waitMandatoryImage(buttonGo, "Go Button", 5000);

        robot.leftClick(Point.of(buttonGoPoint, Point.of(981, 617), Point.of(1022, 580)));
        robot.clearText();
        robot.sleep(200);
        robot.typeString(Integer.toString(arenaLocation.getX()));

        robot.leftClick(Point.of(buttonGoPoint, Point.of(981, 617), Point.of(1127, 580)));
        robot.clearText();
        robot.sleep(200);
        robot.typeString(Integer.toString(arenaLocation.getY()));

        robot.leftClick(buttonGoPoint, buttonGo);
        robot.sleep(1000);

        // robot.leftClick(Point.of(959, 563)); // Click in the center Should depend on the zoom level
        robot.leftClick(Point.of(965, 563)); // Click in the center Should depend on the zoom level
        robot.sleep(1000);

        BufferedImage screen = robot.captureScreen();
        BufferedImage labelArena = ImageUtil.loadResource("player/label_arena.png");
        Area labelArenaArea = Area.fromTwoPoints(896, 305, 1034, 338);
        Point labelArenaPoint = ImageUtil.search(labelArena, screen, labelArenaArea, 0.1).orElse(null);
        
        if (labelArenaPoint == null) {
            System.out.println("Arena doesn't exist anymore!");
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return false;
        }

        screen = robot.captureScreen();
        BufferedImage iconCheckmark = ImageUtil.loadResource("player/icon_checkmark.png");
        Area areaForCheckmark = Area.of(labelArenaPoint, Point.of(971, 322), Point.of(865, 705), Point.of(901, 739));
        // ImageUtil.showImageAndWait(ImageUtil.crop(screen, areaForCheckmark));
        Point iconCheckmarkPoint = ImageUtil.search(iconCheckmark, screen, areaForCheckmark, 0.1)
                .orElse(null);

        if (iconCheckmarkPoint == null) {
            System.out.println("Hero is not available");
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
        
        return true; // Atacou!
    }
    
    public static void helpClanMembers() {
        BufferedImage screen = robot.captureScreen();
        BufferedImage iconHelpAllies = ImageUtil.loadResource("player/icon_help_allies.png");
        
        Point point = ImageUtil.searchSurroundings(iconHelpAllies, screen, 0.1, 20).orElse(null);
        
        if (point == null) {
            System.out.println("No help allies icon found");
        }
        else {
            System.out.println("Clicked on help allies icon");
            robot.leftClick(point, iconHelpAllies);
            robot.sleep(2500);
        }
    }

    private static Optional<Point> waitImage(BufferedImage image, String name, long timeout) {
        long start = System.currentTimeMillis();
        Point point;

        BufferedImage screen;
        do {
            screen = robot.captureScreen();
            point = ImageUtil.searchSurroundings(image, screen, 0.1, 20).orElse(null);
            if (point == null && (System.currentTimeMillis() - start < timeout)) {
                System.out.println("Sleeping 300 ms waiting for " + name);
                robot.sleep(500);
            }
        } while (point == null && (System.currentTimeMillis() - start < timeout));

        if (point == null) {
            ImageUtil.write(screen, "error_last_screen.png");
            return Optional.empty();
        }

        return Optional.of(point);
    }

    private static Point waitMandatoryImage(BufferedImage image, String name, long timeout) {
        Point point = waitImage(image, name, timeout).orElse(null);
        if (point == null) {
            ImageUtil.write(image, "error_image.png");
            throw new RuntimeException("Image didn't disappear!");
        }
        return point;
    }


    private static void waitImageDisappear(BufferedImage image, String name, long timeout) {
        long start = System.currentTimeMillis();
        Point point;

        BufferedImage screen;
        do {
            screen = robot.captureScreen();
            point = ImageUtil.searchSurroundings(image, screen, 0.1, 20).orElse(null);
            if (point != null && (System.currentTimeMillis() - start < timeout)) {
                System.out.println("Sleeping 300 ms waiting for " + name + " to disappear");
                robot.sleep(500);
            }
        } while (point != null && (System.currentTimeMillis() - start < timeout));

        if (point != null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(image, "error_image.png");
            throw new RuntimeException("Iamge didn't disappear!");
        }
    }


    private static void login(Player player, BufferedImage linkLogin, Point linkLoginPoint) {
        // Search and click accept all cookies button
        searchAndClick("player/button_accept_cookies.png");

        // Click on Login link
        robot.leftClick(linkLoginPoint, linkLogin);
        robot.sleep(350);

        // Provide username
        robot.leftClick(Point.of(linkLoginPoint, Point.of(450, 515), Point.of(358, 494)));
        robot.clearText();
        robot.typeString(player.getUsername());

        // Provide password
        robot.leftClick(Point.of(linkLoginPoint, Point.of(450, 515), Point.of(358, 564)));
        robot.clearText();
        robot.typeString(player.getPassword());

        // Click on Login Button
        robot.leftClick(Point.of(linkLoginPoint, Point.of(450, 515), Point.of(438, 650)));
        robot.sleep(3000);
        
        // Search and click 
        searchAndClick("player/button_chrome_save.png");
    }
    
    private static boolean searchAndClick(String resource) {
        BufferedImage image = ImageUtil.loadResource(resource);
        BufferedImage screen = robot.captureScreen();
        Point point = ImageUtil.search(image, screen, 0.1).orElse(null);

        if (point != null) {
            robot.leftClick(point, image);
            robot.sleep(350);
            return true;
        }
        return false;
    }
    
    public static void waitUntilWindowIsClosed(WebDriver driver) {
        System.out.println("Waiting browser to be closed...");
        while (true) {
            try {
                driver.getTitle(); // check if window is still open
                Thread.sleep(1000); // wait 1 second
            } catch (org.openqa.selenium.NoSuchSessionException e) {
                System.out.println("Browser closed by user. Exiting program.");
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private static void waitPageToBeLoaded(WebDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                (ExpectedCondition<Boolean>) wd ->
                        ((JavascriptExecutor) wd)
                                .executeScript("return document.readyState")
                                .equals("complete")
        );
    }


    /**
     * Displays a blocking dialog with a single button.
     * Execution will pause until the button is clicked.
     *
     * @param message the message to show in the dialog
     */
    public static void showPauseDialog(String message) {
        // Use invokeAndWait to ensure dialog runs on the Event Dispatch Thread
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                createAndShowDialog(message);
            } else {
                SwingUtilities.invokeAndWait(() -> createAndShowDialog(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createAndShowDialog(String message) {
        final JDialog dialog = new JDialog((Frame) null, "Paused", true); // true = modal (blocks)
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JLabel(message, SwingConstants.CENTER), BorderLayout.CENTER);

        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(e -> dialog.dispose());
        dialog.add(continueButton, BorderLayout.SOUTH);

        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true); // this call blocks until the dialog is closed
    }
    
    
}
