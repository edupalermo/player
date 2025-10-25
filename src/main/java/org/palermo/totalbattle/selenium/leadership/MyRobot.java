package org.palermo.totalbattle.selenium.leadership;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MyRobot {
    
    private final Robot robot;
    private final Rectangle screenBounds;

    public MyRobot() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();

            // Get the bounds of the first screen (usually the primary)
            screenBounds = screens[0].getDefaultConfiguration().getBounds();

            this.robot = new Robot(screens[0]);
            robot.setAutoDelay(2); // Small delay between actions
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }
    
    private long getDelayBetweenTasks() {
        return 150;
    }

    public void leftClick(int x, int y) {
        try {
            robot.mouseMove(x, y);
            Thread.sleep(getDelayBetweenTasks());

            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(getDelayBetweenTasks());
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(getDelayBetweenTasks());
            robot.mouseMove(x, y);
            Thread.sleep(getDelayBetweenTasks());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void leftClick(Point point) {
        leftClick(point.getX(), point.getY());
    }

    public void leftClick(Point point, BufferedImage image) {
        leftClick(point.getX() + (image.getWidth() / 2), point.getY() + (image.getHeight() / 2));
    }

    public void mouseMove(Point point) {
        try {
            robot.mouseMove(point.getX(), point.getY());
            Thread.sleep(getDelayBetweenTasks());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void mouseDrag(Point point, int dx, int dy) {
        try {
            robot.mouseMove(point.getX(), point.getY());
            Thread.sleep(getDelayBetweenTasks());
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(getDelayBetweenTasks());

            int steps = Math.max(Math.abs(dx), Math.abs(dy)); // Number of steps = max distance
            for (int i = 1; i <= steps; i++) {
                int newX = point.getX() + (i * dx) / steps;
                int newY = point.getY() + (i * dy) / steps;
                robot.mouseMove(newX, newY);
                Thread.sleep(5); // Optional: control speed of drag
            }
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(getDelayBetweenTasks());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void mouseWheel(Point point, int notches) {
        try {
            robot.mouseMove(point.getX(), point.getY());
            Thread.sleep(getDelayBetweenTasks());

            robot.mouseWheel(notches);
            Thread.sleep(getDelayBetweenTasks());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage captureScreen() {
        return robot.createScreenCapture(screenBounds);
    }
    
    public BufferedImage captureScreen(Area area) {
        return robot.createScreenCapture(area.toRectangle());
    }

    public void activateWindowUnderMouse() {
        try {
            // Step 1: Get mouse location and window ID
            Process locProc = Runtime.getRuntime().exec("xdotool getmouselocation --shell");
            BufferedReader reader = new BufferedReader(new InputStreamReader(locProc.getInputStream()));

            String line;
            String windowId = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("WINDOW=")) {
                    windowId = line.substring("WINDOW=".length()).trim();
                    break;
                }
            }

            // Step 2: Activate the window
            if (windowId != null) {
                Process activateProc = Runtime.getRuntime().exec(new String[]{
                        "xdotool", "windowactivate", "--sync", windowId
                });
                activateProc.waitFor();
                System.out.println("Activated window ID: " + windowId);
            } else {
                System.out.println("No window found under mouse.");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void clearText() {
        // Select all
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        
        sleep(200);

        // Delete selection
        robot.keyPress(KeyEvent.VK_DELETE);
        robot.keyRelease(KeyEvent.VK_DELETE);
    }


    public void typeString(String text) {
        for (char c : text.toCharArray()) {
            typeChar(c);
        }
    }

    public void type(int key) {
        robot.keyPress(key);
        robot.delay(10);
        robot.keyRelease(key);
        robot.delay(50);
    }

    private void typeChar(char c) {
        try {
            boolean upperCase = Character.isUpperCase(c);
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);

            if (keyCode == KeyEvent.VK_UNDEFINED) {
                throw new RuntimeException("Cannot type character: " + c);
            }


            switch (c) {
                case '+':
                    robot.keyPress(KeyEvent.VK_PLUS);
                    robot.keyRelease(KeyEvent.VK_PLUS);
                    break;
                case '@':
                    robot.keyPress(KeyEvent.VK_ALT_GRAPH);
                    robot.keyPress(KeyEvent.VK_Q);
                    robot.keyRelease(KeyEvent.VK_Q);
                    robot.keyRelease(KeyEvent.VK_ALT_GRAPH);
                    break;
                case '*':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_PLUS);
                    robot.keyRelease(KeyEvent.VK_PLUS);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                default:
                    if (upperCase || isShiftRequired(c)) {
                        robot.keyPress(KeyEvent.VK_SHIFT);
                    }

                    robot.keyPress(keyCode);
                    robot.keyRelease(keyCode);

                    if (upperCase || isShiftRequired(c)) {
                        robot.keyRelease(KeyEvent.VK_SHIFT);
                    }
            }
            
            

            robot.delay(50); // optional: delay between keystrokes
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isShiftRequired(char c) {
        // Add more characters if needed
        return "~!@#$%^&*()_+{}|:\"<>?".indexOf(c) >= 0;
    }    
    
    public void sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
