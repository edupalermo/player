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
import java.util.Optional;

@Slf4j
public class Task {
    
    private static final ArmyService armyService = new ArmyService();

    private static MyRobot robot = MyRobot.INSTANCE;
    
    public static void main(String[] args) {
        
        Player player = Player.PETER;
        
        WebDriver driver = null;
        try {
            driver = openBrowser(player);
            login(player);

            //Task.showPauseDialog("Click on the button to continue");

            // (new ClanContribution(player)).helpClanMembers();
            // (new ClanContribution(player)).collectChests();

            // (new Announce()).playPlayerName(player);

            

            // (new SummoningCircle(robot, player)).evaluate();

            // (new CaptainSelector(player)).updatePlayerState();

            //new ClanContribution(player).helpClanMembers();
            //new ClanContribution(player).collectChests();

            
            /*
            (new CaptainSelector(player)).select(CaptainSelector.CARTER);
            (new CaptainSelector(player)).select(CaptainSelector.TRAINER);
            (new CaptainSelector(player)).select(CaptainSelector.STROR);
             */
            

            // (new Telescope(player)).evaluate();

            // (new AttackArena(player)).attackArena();
            // (new MineSilver(player)).mine();
                // attackArena(SharedData.INSTANCE.getArena().get());

            // (new SummoningCircle(robot, player)).evaluate();

            // (new FreeSale(player)).freeSale();
            
            //(new BuildArmy(player)).buildArmy();
            // (new Telescope(player)).findArena();
            // (new Telescope(player)).findSilverMines();
            //(new BuildArmy(player)).testSpeedUps();
            
            

            //(new Telescope(player)).findArena();
            //(new Telescope(player)).findSilverMines();
            //(new AttackArena(player)).attackArena();

            //(new Telescope(player)).findCitadels();
            (new AttackCitadel(player)).attack();
            
            //(new Telescope(player)).findCrypts();

            // (new InfoGather(player)).evaluate();
            
//             (new PayTaxes(player)).pay();
            //(new DonateSilver(player)).donate();
            
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
                Navigate buttonCloseNavigate = Navigate.builder()
                        .resourceName("player/button_bonus_sales_close.png")
                        .areaName(Area.BONUS_SALE_BUTTON_CLOSE)
                        .build();
                if (buttonCloseNavigate.exist()) {
                    buttonCloseNavigate.leftClick();
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


    private static void login(Player player, BufferedImage linkLogin, Point linkLoginPoint) {
        // Search and click accept all cookies button
        Navigate.builder()
                .resourceName("player/button_accept_cookies.png")
                .areaName("ACCEPT_COOKIES")
                .waitLimit(2000)
                .build()
                .leftClickIfExists();

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
