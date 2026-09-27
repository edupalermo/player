package org.palermo.totalbattle;

import org.junit.jupiter.api.Test;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.image.BufferedImage;

public class Telescope {
    
    @Test
    public void test() {

        BufferedImage telescopeOn = ImageUtil.loadResource("player/icon_telescope.png");
        BufferedImage telescopeOnScreen = ImageUtil.loadResource("tolerance/telescope_off.png");

        for (int i = 0; i < 10; i++) {
            double tolerance = 0.01 + (0.01 * i);
            Point point = ImageUtil.search(telescopeOn, telescopeOnScreen, tolerance).orElse(null);
            if (point == null) {
                System.out.println(String.format("%.2f not found!", tolerance));
            }
            else {
                System.out.println(String.format("%.2f %d %d", tolerance, point.getX(), point.getY()));
            }
        }
    }
}
