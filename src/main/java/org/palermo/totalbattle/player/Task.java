package org.palermo.totalbattle.player;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.ImageUtil;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Task {

    private static MyRobot robot = new MyRobot();
    
    private static Map<String, Player> players = new HashMap<>();
    static {
        players.put("Palermo", Player.builder()
                .name("Palermo")
                .profileFolder("chrome-profiles/fp2268@gmail.com")
                .username("fp2268@gmail.com")
                .password("Alemanha79")
                .build());
        players.put("Elanin", Player.builder()
                .name("Elanin")
                .profileFolder("chrome-profiles/edupalermo_03@gmail.com")
                .username("edupalermo+03@gmail.com")
                .password("Alemanha79")
                .build());
    }


    public static void main(String[] args) {
        
        Player player = players.get("Elanin");

        WebDriver driver = null;
        try {
            driver = openBrowser(player);
            login(player);
            attackArena(Point.of(100, 100));
            collectChests();
            helpClanMembers();
            
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
        robot.sleep(1000);

        screen = robot.captureScreen();
        BufferedImage titleMyClan = ImageUtil.loadResource("player/title_my_clan.png");
        Point titleMyClanPoint = ImageUtil.searchSurroundings(titleMyClan, screen, 0.1, 20).orElse(null);

        if (titleMyClanPoint == null) {
            ImageUtil.write(screen, "error_screen.png");
            ImageUtil.write(iconHelpAllies, "error_image.png");
            throw new RuntimeException("Couldn't My Clan title!");
        }

        robot.leftClick(Point.of(titleMyClanPoint, Point.of(963, 325), Point.of(611, 497)));
        robot.sleep(1000);

        robot.leftClick(Point.of(titleMyClanPoint, Point.of(963, 325), Point.of(1353, 861)));

        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
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
        BufferedImage screen = robot.captureScreen();
        Point linkLoginPoint = ImageUtil.searchSurroundings(linkLoginImage, screen, 0.1, 20).orElse(null);

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
            Point point = ImageUtil.searchSurroundings(labelClan, screen, 0.1, 20).orElse(null);
            if (point != null) {
                found = true;
            }
            else {
                screen = robot.captureScreen();
                Point buttonClosepoint = ImageUtil.searchSurroundings(buttonBonusSalesClose, screen, 0.1, 20).orElse(null);
                if (buttonClosepoint != null) {
                    robot.leftClick(buttonClosepoint, buttonBonusSalesClose);
                    robot.sleep(500);
                }
                else {
                    System.out.println("Trying to hit scape to close initial pop-ups");
                    robot.type(KeyEvent.VK_ESCAPE);
                    robot.sleep(500);
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
        BufferedImage iconMusicOn = ImageUtil.loadResource("player/icon_music_on.png");
        screen = robot.captureScreen();
        Point iconMusicOnPoint = ImageUtil.searchSurroundings(iconMusicOn, screen, 0.1, 20).orElse(null);
        if (iconMusicOnPoint != null) {
            robot.leftClick(iconMusicOnPoint, iconMusicOn);
        }
        
        // Turn sound off            
        BufferedImage iconSoundOn = ImageUtil.loadResource("player/icon_sound_on.png");
        screen = robot.captureScreen();
        Point iconSoundOnPoint = ImageUtil.searchSurroundings(iconSoundOn, screen, 0.1, 20).orElse(null);
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

        robot.leftClick(Point.of(959, 563)); // Click in the center Should depend on the zoom level
        robot.sleep(1000);

        BufferedImage labelArena = ImageUtil.loadResource("player/label_arena.png");
        Point labelArenaPoint = waitImage(labelArena, "Label rena", 3000).orElse(null);
        if (labelArenaPoint == null) {
            System.out.println("Arena doesn't exist anymore!");
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return false;
        }

        BufferedImage iconCheckmark = ImageUtil.loadResource("player/icon_checkmark.png");
        BufferedImage screen = robot.captureScreen();
        Point iconCheckmarkPoint = ImageUtil.search(iconCheckmark, screen, Area.of(labelArenaPoint, Point.of(971, 322), Point.of(865, 705), Point.of(901, 739)), 0.1)
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
                Thread.sleep(500); // wait 1 second
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
}
