package org.palermo.totalbattle.util;

import lombok.Builder;
import org.apache.commons.lang3.ObjectUtils;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Optional;

public class Navigate {
    
    private Area area;
    private BufferedImage searchImage;
    
    private Long waitLimit;
    private boolean pressEscapeWhileWaiting = false;
    private Double comparationLimit;
    
    private boolean debug = false;

    private BufferedImage lastScreen;
    private MyRobot robot = MyRobot.INSTANCE;
    
    private Point point = null;

    @Builder
    public Navigate(Area area, 
                    String areaName, 
                    String resourceName,
                    BufferedImage searchImage,
                    Long waitLimit, 
                    Boolean pressEscapeWhileWaiting, 
                    Double comparationLimit,
                    Boolean debug) {
        lastScreen = robot.captureScreen();
        if (areaName != null) {
            this.area = RegionSelector.selectArea(areaName, lastScreen);
        }
        else {
            if (area != null) {
                this .area = area;
            }
            else {
                this.area = Area.of(0, 0, lastScreen.getWidth(), lastScreen.getHeight());
            }
        }
        if (resourceName != null) {
            this.searchImage = ImageUtil.loadResource(resourceName);
        }
        else {
            this.searchImage = searchImage;
        }
        this.waitLimit = ObjectUtils.firstNonNull(waitLimit, 0L);
        if (pressEscapeWhileWaiting != null) {
            this.pressEscapeWhileWaiting = pressEscapeWhileWaiting.booleanValue();
        }
        this.comparationLimit = ObjectUtils.firstNonNull(comparationLimit, 0.1);
        
        if (debug != null) {
            this.debug =  debug.booleanValue();
        }
    }

    public Optional<Point> search() {
        if (point != null) {
            return Optional.of(point);
        }
        
        long start = System.currentTimeMillis();
        do {
            lastScreen = robot.captureScreen();
            point = ImageUtil.searchSurroundings(searchImage, lastScreen, area, comparationLimit, 20).orElse(null);
            if (point == null) {
                if (this.pressEscapeWhileWaiting) {
                    robot.type(KeyEvent.VK_ESCAPE);
                }
                robot.sleep(200);
            }
        } while (point == null && (System.currentTimeMillis() - start < waitLimit));
        return Optional.ofNullable(point);
    }

    public boolean exist() {
        if (point == null) {
            point = search().orElse(null);
        }
        return point != null;
    }

    public Point ensureExistence() {
        if (!this.exist()) {
            if (debug) {
                ImageUtil.showImageAndWait(lastScreen, area);
            }
            throw new RuntimeException("Could not find mandatory resource");
        }
        return this.point;
    }

    public void leftClick() {
        if (point == null) {
            point = search().orElse(null);
        }
        if (point == null) {
            throw new RuntimeException("Could not find the given resource");
        }
        robot.leftClick(point, searchImage);
        robot.sleep(500);
    }

    public void clickIfExists() {
        if (point == null) {
            point = search().orElse(null);
        }
        if (point != null) {
            robot.leftClick(point, searchImage);
            robot.sleep(500);
        }
    }

    public static class NavigateBuilder {
        public NavigateBuilder waitLimit(int value) {
            this.waitLimit = (long) value;
            return this;
        }
        
        public NavigateBuilder waitLimit(long value) {
            this.waitLimit = value;
            return this;
        }
    }    
}
