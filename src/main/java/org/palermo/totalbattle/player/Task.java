package org.palermo.totalbattle.player;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.palermo.totalbattle.internalservice.ArmyService;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.state.Army;
import org.palermo.totalbattle.player.state.ArmyTarget;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.player.state.Resources;
import org.palermo.totalbattle.player.state.location.Citadel;
import org.palermo.totalbattle.player.state.location.Crypt;
import org.palermo.totalbattle.player.task.*;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.util.CdpUtil;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.Navigate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class Task {

    private static final ArmyService armyService = new ArmyService();
    private static final PlayerStateService playerStateService = new PlayerStateService();
    private static final GameStateService gameStateService = new GameStateService();
    private static final LockService lockService = new LockService();

    private static MyRobot robot = MyRobot.INSTANCE;


    public static void main(String[] args) {
        play(Player.PALERMO);
        //play(Player.GRIRANA);
    }


    public static void play(Player player) {
        
        playerStateService.getState(Player.PALERMO).getArmy().setTarget(ArmyTarget.builder()
                        .leadership(30613)
                        .dominance(7576)
                        .authority(14715)
                        .goal("any")
                        .waves(3)
                .build());

        playerStateService.getState(Player.PALERMO).setResourcesTarget(Resources.builder()
                .lumber(19_000_000)
                .stone(19_000_000)
                .iron(19_000_000)
                .silver(2_000_000)
                .build());
        
        playerStateService.getState(Player.LORVEN).setResourcesTarget(Resources.builder()
                .lumber(250_000)
                .stone(250_000)
                .iron(250_000)
                .build());

        /*
        gameStateService.add(Citadel.builder()
                .level(15)
                .position(Point.of(341, 523))
                .build());
         */

        gameStateService.add(Crypt.builder()
                .level(15)
                .position(Point.of(396, 510))
                .build());

        lockService.lock(Player.GRIRANA, Scenario.FINISHED_TRAINING_NON_MONSTERS, LocalDateTime.now().plusHours(1));

        Process process = null;
        try {
            process = openOrdinaryBrowser(player);
            
            robot.sleep(3000);
            CdpUtil.closeAllTabsExceptOne();
            
            login(player);

            (new InfoGather(player)).evaluate();
            // (new Quests(player)).evaluate();

            
            waitUntilProcessIsRunning(process);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (process != null && process.isAlive()) {
                process.destroy();

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
    
    public static WebDriver openBrowser(Player player) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--user-data-dir=" + new java.io.File(player.getProfileFolder()).getAbsolutePath());
        options.addArguments("--profile-directory=Default"); // Default profile in that dir
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);
        driver.get(AddressSelector.select(player));

        waitPageToBeLoaded(driver);

        System.out.println("Page has loaded");

        return driver;
    }

    public static Process openOrdinaryBrowser(Player player) {
        try {
            String chromePath;
            if (isLinux()) {
                chromePath = "/usr/bin/google-chrome"; // Linux example
            }
            else {
                chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
            }

            String userDataDir = new File(player.getProfileFolder()).getAbsolutePath();
            String url = AddressSelector.select(player);

            ProcessBuilder pb = new ProcessBuilder(
                    chromePath,
                    "--start-maximized",
                    "--no-default-browser-check",
                    "--no-first-run",
                    "--disable-extensions",
                    "--disable-default-apps",
                    "--disable-popup-blocking",
                    "--disable-session-crashed-bubble",
                    "--restore-last-session=false",
                    "--remote-debugging-port=9222",
                    //"--new-window",
                    "--user-data-dir=" + userDataDir,
                    // "--profile-directory=Default",
                    url
            );
            
            return pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }


    public static void login(Player player) {
        Navigate linkLogin = Navigate.builder()
                .resourceName("player/link_login.png")
                .area(Area.fromTwoPoints(347, 459, 591, 548))
                .waitLimit(3000)
                .build();
        
        if (linkLogin.exist()) {
            System.out.println("Login link found");
            login(player, linkLogin);
        }
        
        robot.sleep(5000);
        
        System.out.println("User already logged");

        BufferedImage labelClan = ImageUtil.loadResource("player/label_clan.png");
        BufferedImage buttonBonusSalesClose = ImageUtil.loadResource("player/button_bonus_sales_close.png");
        BufferedImage screen = robot.captureScreen();
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
                Navigate buttonCloseNavigate = Navigate.builder()
                        .resourceName("player/button_bonus_sales_close.png")
                        .areaName(Area.BONUS_SALE_BUTTON_CLOSE)
                        .build();
                if (buttonCloseNavigate.exist()) { // Sometimes the left click on close doesn't work!
                    buttonCloseNavigate.leftClick();
                    robot.sleep(500);
                    robot.type(KeyEvent.VK_ESCAPE);
                    robot.sleep(300);
                }
                else {
                    System.out.println("Trying to hit scape to close initial pop-ups");
                    robot.type(KeyEvent.VK_ESCAPE);
                    robot.sleep(1000);
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


    private static void login(Player player, Navigate linkLogin) {
        // Search and click accept all cookies button
        Navigate.builder()
                .resourceName("player/button_accept_cookies.png")
                .areaName("ACCEPT_COOKIES")
                .waitLimit(2000)
                .build()
                .leftClickIfExists();

        // Click on Login link
        linkLogin.leftClick();
        robot.sleep(350);

        // Provide username
        robot.leftClick(Point.of(linkLogin.getPoint(), Point.of(450, 515), Point.of(358, 494)));
        robot.clearText();
        robot.typeString(player.getUsername());

        // Provide password
        robot.leftClick(Point.of(linkLogin.getPoint(), Point.of(450, 515), Point.of(358, 564)));
        robot.clearText();
        robot.typeString(player.getPassword());

        // Click on Login Button
        robot.leftClick(Point.of(linkLogin.getPoint(), Point.of(450, 515), Point.of(438, 650)));
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
    
    public static void waitUntilProcessIsRunning(Process process) {
        System.out.println("Waiting browser to be closed...");
        while (true) {
            try {
                process.isAlive();
                Thread.sleep(1000); // wait 1 second
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
