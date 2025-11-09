package org.palermo.totalbattle.player.bean;

import lombok.Builder;
import lombok.Getter;

import java.awt.image.BufferedImage;

@Builder @Getter
public class SpeedUpBean {
    
    private final BufferedImage image;
    private final long seconds;
    private final String label;
}
