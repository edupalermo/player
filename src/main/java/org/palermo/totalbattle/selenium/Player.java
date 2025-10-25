package org.palermo.totalbattle.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.ImageUtil;
import org.palermo.totalbattle.selenium.leadership.MyOcr;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;

import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Player {
    
    private static WebDriver driver;
    private static MyRobot robot = new MyRobot();


    public static void main2(String[] args) {
        // Optionally set path to chromedriver if not in system PATH
        // System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");

        WebDriverManager.chromedriver().setup();


        String profileDir = "chrome-profiles/fp2268@gmail.com" ;
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--user-data-dir=" + new java.io.File(profileDir).getAbsolutePath());
        options.addArguments("--profile-directory=Default"); // Default profile in that dir
        options.addArguments("--kiosk");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        // options.setCapability("start-maximized", true);

        driver = new ChromeDriver(options); // Launch Chrome browser

        driver.get("https://totalbattle.com/en"); // Open Google
        // driver.manage().window().maximize();

        sleep(60000);
    }


    public static void main(String[] args) {
        // Optionally set path to chromedriver if not in system PATH
        // System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");

        WebDriverManager.chromedriver().setup();


        String profileDir = "chrome-profiles/fp2268@gmail.com" ;
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--user-data-dir=" + new java.io.File(profileDir).getAbsolutePath());
        options.addArguments("--profile-directory=Default"); // Default profile in that dir
        options.addArguments("--kiosk");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        // options.setCapability("start-maximized", true);

        driver = new ChromeDriver(options); // Launch Chrome browser

        try {
            driver.get("https://totalbattle.com/en"); // Open Google
            // driver.manage().window().maximize();

            sleep(1500);


            WebElement canvas = driver.findElement(By.id("unityCanvas"));

            org.openqa.selenium.Point location = canvas.getLocation(); // top-left corner in viewport
            Dimension size = canvas.getSize();     // width and height

            int x = location.getX();
            int y = location.getY();
            int width = size.getWidth();
            int height = size.getHeight();

            System.out.printf("Canvas is at (%d, %d) with size %dx%d%n", x, y, width, height);
            
            // waitSplashScreenToDisappear(); // TODO this is doing nothing
            closeInitialOfferScreen();
            sendHeroToArena();
            // I probably should check for a second close


            
            
            /*
            for(;;) {
                System.out.println("Click on lupa menos");
                click(1751, 905);
                sleep(3000);
                if (false) {
                    break;
                }
            }
             */
            
                    
            sleep(60000);
            
        } catch (Exception e) {
            ImageUtil.write(takeGrayscaleScreenshot(), "exception.png");
            e.printStackTrace();
        } finally {
            driver.quit(); // Close the browser
        }
    }

    private static BufferedImage takeGrayscaleScreenshot() {
        try {
            String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Screenshot);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes)) {
                return ImageUtil.toGrayscale(ImageIO.read(bis));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static BufferedImage takeGrayscaleScreenshot(int x, int y, int width, int height) {
        return takeGrayscaleScreenshot().getSubimage(x, y, width, height);
    }
    
    private static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void waitSplashScreenToDisappear() {
        BufferedImage logo = ImageUtil.loadResource("player/initial_logo.png");
        boolean retry = true;
        while (retry) {
            BufferedImage screen = takeGrayscaleScreenshot(899, 561, 51, 39);
            retry = ImageUtil.compare(logo, screen, 0.01);
            if (retry) {
                System.out.println("Game logo is being displayed.");
                sleep(1000);
            }
        }
    }
    
    private static void closeInitialOfferScreen() {
        // Point probablePosition = Point.of(1792, 38); //
        Area area = Area.of(1855, 78, 30, 30);
        BufferedImage buttonClose = ImageUtil.loadResource("player/button_close.png");
        Point location = null;
        for (;;) {
            BufferedImage screen = takeGrayscaleScreenshot();
            ImageUtil.write(screen, "area_screen.png");
            ImageUtil.write(ImageUtil.crop(screen, area), "area.png");
            location = ImageUtil.search(buttonClose, screen, area, 0.01d).orElse(null);
            if (location != null) {
                System.out.println("Close button FOUND with success!");
                break;
            }
            else {
                System.out.println("Close button not found!");
                sleep(1000);
            }
        }


        for (;;) {
            // new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            robot.leftClick(location.toTheMiddleOf(buttonClose));
            sleep(1000);
            
            BufferedImage screen = takeGrayscaleScreenshot();
            location = ImageUtil.search(buttonClose, screen, area, 0.01d).orElse(null);
            if (location == null) {
                break;
            }
            else {
                System.out.println("Button is still there");
            }
        }

        System.out.println("Close button is not there anymore!");

        robot.type(KeyEvent.VK_ESCAPE);
        System.out.println("Sent scape in order to close possible special offer popup");

        robot.type(KeyEvent.VK_ESCAPE);
        System.out.println("Sent scape in order to close possible second special offer popup");

    }


    private static void sendHeroToArena() {
        
        BufferedImage buttonMap = ImageUtil.loadResource("player/button_map.png");
        checkMilestone(Area.of(622, 995, 80, 75), buttonMap);

        
        Area probableTelescopeArea = Area.of(670, 958, 80, 80);
        BufferedImage buttonTelescope = ImageUtil.loadResource("player/button_telescope.png");
        
        Point telescopePosition = ImageUtil.search(buttonTelescope, takeGrayscaleScreenshot(), probableTelescopeArea, 0.015)
                .orElseThrow(() -> new RuntimeException("Cannot find telescope. Is the telescope enabled?"));
        
        robot.leftClick(telescopePosition.toTheMiddleOf(buttonTelescope));
        sleep(600);

        // Check for the title of the pop up window - Watchtower
        checkMilestone(Area.fromTwoPoints(905, 255, 1013, 278), ImageUtil.loadResource("player/title_watchtower.png"));
        
        robot.leftClick(680, 490); // Click on Crypts and Arenas
        sleep(300);


        BufferedImage screen = takeGrayscaleScreenshot();
        Area area = Area.fromTwoPoints(788, 360, 1312, 420);

        switchState(screen, area, ImageUtil.loadResource("player/label_common_enabled.png"));
        switchState(screen, area, ImageUtil.loadResource("player/label_rare_enabled.png"));
        switchState(screen, area, ImageUtil.loadResource("player/label_epic_enabled.png"));
        switchState(screen, area, ImageUtil.loadResource("player/label_others_enabled.png"));
        switchState(screen, area, ImageUtil.loadResource("player/label_arenas_disabled.png"));


        BufferedImage slide = ImageUtil.loadResource("player/slide.png");
        ImageUtil.write(takeGrayscaleScreenshot(), "last_screen.png");
        Area slidesArea = Area.of(857, 433, 437, 25);
        slideToValue(slide, 5, slidesArea, 0);
        slideToValue(slide, 5, slidesArea, 1);
        
        // Click go Button
        robot.leftClick(1214, 531);
        
        // Send Escape to close possible pop up
        robot.type(KeyEvent.VK_ESCAPE);

        // Click on the center screen
        
        // Get x,y of the arena
        
        // send Hero if enabled
        
        
        
        
    }
    
    private static void slideToValue(BufferedImage slide, int targetValue, Area slidesArea, int index) {

        BufferedImage screen = takeGrayscaleScreenshot();
        ImageUtil.write(ImageUtil.crop(screen, slidesArea), "slides_area.png");
        
        double slipeTolerance = 0.021;

        List<Point> slideLocations = ImageUtil.searchMultiple(slide, screen, slidesArea, slipeTolerance);
        if (slideLocations.size() != 2) {
            throw new RuntimeException("Found slides: " + slideLocations.size());
        }
        slideLocations = sortHorizontally(slideLocations);
        Point position = slideLocations.get(index);
        
        int direction = Integer.signum(targetValue - getSlideValue(position));
        if (direction == 0) {
            return;
        }
        
        final int width = 18;

        for (int i = 1; i < 100; i++) {
            robot.leftClick(position.toTheMiddleOf(slide));
            robot.mouseDrag(position.toTheMiddleOf(slide), width * direction, 0);

            screen = takeGrayscaleScreenshot();
            slideLocations = ImageUtil.searchMultiple(slide, screen, slidesArea, slipeTolerance);
            slideLocations = sortHorizontally(slideLocations);
            position = slideLocations.get(index);

            
            int currentValue = getSlideValue(position); 
            if (currentValue == targetValue) {
                return;
            } else {
                direction = Integer.signum(targetValue - getSlideValue(position));
                System.out.println(String.format("Slide value %d targetValue %d", currentValue, targetValue));
            }
        }

        throw new RuntimeException("Fail to set value " + targetValue);
    }
    
    //private static int slideCount = 0;
    private static int getSlideValue(Point point) {
        BufferedImage currentValue = takeGrayscaleScreenshot(point.getX() + 5, point.getY() - 21, 19, 17);
        ImageUtil.write(currentValue, "slide_current.png");
        // String stringValue = ImageUtil.ocr(normalizedValue, "1234567890", ImageUtil.SINGLE_LINE_MODE, ImageUtil.LANGUAGE_TB);
        String stringValue = MyOcr.decode(currentValue, "font02");
        return Integer.parseInt(stringValue);
    }

    private static List<Point> sortHorizontally(List<Point> locations) {
        List<Point> result = new ArrayList<>();
        for (Point point : locations) {
            if (result.isEmpty()) {
                result.add(point);
                continue;
            }
            for (int i = 0; i < result.size(); i++) {
                if (point.getX() < result.get(i).getX()) {
                    result.add(i , point);
                    break;
                }
                if (i == (result.size() - 1)) {
                    result.add(point);
                    break;
                }
            }
        }
        return result;
    }


    private static Point getOnTheLeft(List<Point> locations) {
        int best = -1;
        
        for (int i = 0; i < locations.size(); i++) {
            if (best == -1 || locations.get(i).getX() < locations.get(best).getX()) {
                best = i;
            }
        }
        return locations.get(best);
    }

    private static Point getOnTheRight(List<Point> locations) {
        int best = Integer.MAX_VALUE;

        for (int i = 0; i < locations.size(); i++) {
            if (best == Integer.MAX_VALUE || locations.get(i).getX() > locations.get(best).getX()) {
                best = i;
            }
        }
        return locations.get(best);
    }

    private static Point checkMilestone(Area area, BufferedImage item) {
        for(int i = 0; i < 10; i++) {
            BufferedImage screen = takeGrayscaleScreenshot();
            Point buttonMapPosition = ImageUtil.search(item, screen, area, 0.01).orElse(null);
            if (buttonMapPosition != null) {
                return buttonMapPosition;
            }
            sleep(1000);
        }
        throw new RuntimeException("Fail to find milestone");
    }
    
    private static void switchState(BufferedImage screen, Area area, BufferedImage enabledButton) {
        Point location = ImageUtil.search(enabledButton, screen, area, 0.01).orElse(null);
        if (location == null) {
            return;
        }
        robot.leftClick(location.toTheMiddleOf(enabledButton));
        sleep(300);
    }

        /*
    private static void click(BufferedImage element, int x, int y) {
        int clickX = (int) Math.round(x + (element.getWidth() / 2d));
        int clickY = (int) Math.round(y + (element.getHeight() / 2d));
        click(clickX, clickY);
    }
    
    private static void click(BufferedImage element, Point position) {
        click(element, position.getX(), position.getY());
    }
    
    private static void click(int x, int y) {
        robot.leftClick(getConvertToRealCoordinate(Point.of(x, y)));
    }

    private static void drag(Point initialPoint, Point finalPoint) {
        drag(initialPoint.getX(), initialPoint.getY(), finalPoint.getX(), finalPoint.getY());
    }

    private static void drag(int initialX, int initialY, int finalX, int finalY) {
        PointerInput mouse = new PointerInput(PointerInput.Kind.MOUSE, "default mouse");
        Sequence drag = new Sequence(mouse, 0)
                //.addAction(mouse.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), initialX, initialY))
                .addAction(mouse.createPointerMove(Duration.ofMillis(100), PointerInput.Origin.viewport(), initialX, initialY))
                .addAction(mouse.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(mouse.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))
                
                .addAction(mouse.createPointerMove(Duration.ofMillis(100), PointerInput.Origin.viewport(), initialX, initialY))
                .addAction(mouse.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(mouse.createPointerMove(Duration.ofMillis(350), PointerInput.Origin.viewport(), finalX, finalY))
                .addAction(mouse.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        ((RemoteWebDriver) driver).perform(Collections.singletonList(drag));
    }


    public static Point getConvertToRealCoordinate(Point pointInsideCanvas) {
        try {
            WebElement canvas = driver.findElement(By.id("unityCanvas"));

            // 1. Get canvas position relative to viewport
            org.openqa.selenium.Point canvasViewportPosition = canvas.getLocation();  // Top-left in viewport

            // 2. Get browser chrome offset using JS
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Number windowX = (Number) js.executeScript("return window.screenX + (window.outerWidth - window.innerWidth);");
            Number windowY = (Number) js.executeScript("return window.screenY + (window.outerHeight - window.innerHeight);");

            // 3. Add canvas offset and point inside canvas
            int screenX = windowX.intValue() + canvasViewportPosition.getX() + pointInsideCanvas.getX();
            int screenY = windowY.intValue() + canvasViewportPosition.getY() + pointInsideCanvas.getY();

            return Point.of(screenX, screenY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    */
}
