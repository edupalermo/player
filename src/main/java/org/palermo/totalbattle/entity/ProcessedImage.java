package org.palermo.totalbattle.entity;

import lombok.Builder;
import lombok.Getter;

import java.awt.image.BufferedImage;

@Builder @Getter 
public class ProcessedImage {
    
    private BufferedImage image;
    private String text;
}
