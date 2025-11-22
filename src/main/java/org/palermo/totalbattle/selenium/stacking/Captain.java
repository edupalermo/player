package org.palermo.totalbattle.selenium.stacking;

import lombok.Getter;
import org.palermo.totalbattle.util.ImageUtil;

import java.awt.image.BufferedImage;

@Getter
public enum Captain {
    
    EMPTY,
    UNKNOW,
    CARTER("carter"),
    HELEN("helen"),
    XI_GUIYING("xi_guiying"),
    STROR("stror");
    
    boolean real;
    BufferedImage image66;
    BufferedImage image72;

    Captain() {
        boolean real = false;
    }
    
    Captain(String name) {
        real = true;
        image66 = ImageUtil.loadResource("player/captain/" + name + "_66.png");
        image72 = ImageUtil.loadResource("player/captain/" + name + "_72.png");
    }
    
}
