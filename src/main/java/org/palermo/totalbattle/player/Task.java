package org.palermo.totalbattle.player;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.palermo.totalbattle.player.task.*;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.Navigate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class Task {

    private static MyRobot robot = MyRobot.INSTANCE;
    
    private static Map<String, Player> players = new HashMap<>();
    static {
        players.put("Palermo", Player.builder()
                .name("Palermo")
                .profileFolder("chrome-profiles/fp2268@gmail.com")
                .username("fp2268@gmail.com")
                .password("Alemanha79")
                .hasHelen(true)
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
        
        Player player = players.get(Player.MIGHTSHAPER);
        
        WebDriver driver = null;
        try {
            driver = openBrowser(player);
            login(player);
            
            
            // collectChests(); // Retrieve chests
            
            //quests(player); // Retrieve open chests

            // (new SummoningCircle(robot, player)).evaluate();

            /*
            (new CaptainSelector(player)).select(CaptainSelector.CARTER);
            (new CaptainSelector(player)).select(CaptainSelector.TRAINER);
            (new CaptainSelector(player)).select(CaptainSelector.STROR);
             */
            
            (new BuildArmy(player)).buildArmy();

            // (new Telescope(player)).evaluate();

            //if (SharedData.INSTANCE.getArena().isPresent()) {
                // attackArena(SharedData.INSTANCE.getArena().get());
            //}

            //(new SummoningCircle(robot, player)).evaluate();
            
            //freeSale(player);
            
            //helpClanMembers();

            //(new Telescope(player)).findSilverMines();
            
            
            waitUntilWindowIsClosed(driver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (driver != null) {
                driver.quit();

                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    try {
                        new ProcessBuilder("taskkill", "/IM", "chrome.exe", "/F").start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                // new ProcessBuilder("pkill", "chrome").start();
            }
        }
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

            chests.add(Point.of(958, 455));
            chests.add(Point.of(1088, 455));
            chests.add(Point.of(1222, 455));
            
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
                LocalDateTime nextLocalDateTime = TimeLeftUtil.parse(nextAsText).orElse(null);
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
                    robot.sleep(180);
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
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);
        driver.get(getAddress());

        waitPageToBeLoaded(driver);

        System.out.println("Page has loaded");

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
        Navigate labelMap = Navigate.builder()
                .resourceName("player/label_map.png")
                .areaName("BOTTOM_MENU_MAP_LABEL")
                .waitLimit(4000L)
                .build();
        if (labelMap.exist()) {
            robot.leftClick(labelMap.search().get().move(12, -31));
            robot.sleep(2000);
        }

        // When we switch to map, the Bonus Sales appear again
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(2000);

        // Zoom in

        Navigate iconZoomMinus = Navigate.builder()
                .resourceName("player/icon_zoom_minus.png")
                .area(Area.fromTwoPoints(1791, 1003, 1836, 1044))
                .build();
        for (int i = 0; i < 4; i++) {
            iconZoomMinus.leftClick();
        }

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

        // Try to click in the arena in the center of the screen
        BufferedImage arena = ImageUtil.loadResource("player/arena/arena_type_i.png");
        Point arenaPoint = ArenaUtil.identifyCenterArena();
        robot.leftClick(arenaPoint, arena);
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
        Area iconHelpAlliesArea = RegionSelector.selectArea("HELP_CLAN_SHAKING_HANDS", screen);
        
        Point point = ImageUtil.searchSurroundings(iconHelpAllies, screen, iconHelpAlliesArea, 0.1, 20).orElse(null);
        
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
