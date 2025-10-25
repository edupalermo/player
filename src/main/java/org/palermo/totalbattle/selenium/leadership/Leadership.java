package org.palermo.totalbattle.selenium.leadership;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Leadership {
    
    private final BufferedImage leadershipIcon;
    
    public Leadership(BufferedImage leadershipIcon) {
        this.leadershipIcon = leadershipIcon;
    }


    public void test(BufferedImage screen) {
        test(screen, 0, 0, screen.getWidth(), screen.getHeight());
    }


    public void test(BufferedImage screen, int x, int y, int width, int height) {
        
        Point point = ImageUtil.bestFit(leadershipIcon, screen, x, y, width, height);

        System.out.println("Best: " + point);
        
        try {
            BufferedImage check = screen.getSubimage(point.getX(), point.getY(), leadershipIcon.getWidth(), leadershipIcon.getHeight());
            ImageIO.write(check, "png", new File("debug01.png"));
            
            BufferedImage imageWithText = screen.getSubimage(point.getX() + leadershipIcon.getWidth() , point.getY() + 6, 90, leadershipIcon.getWidth() - 11);
            ImageIO.write(imageWithText, "png", new File("debug02.png"));

            BufferedImage invertedGray = ImageUtil.invertGrayscale(imageWithText);
            ImageIO.write(invertedGray, "png", new File("debug03.png"));

            BufferedImage linearNormalized = ImageUtil.linearNormalization(invertedGray);
            ImageIO.write(linearNormalized, "png", new File("debug04.png"));

            BufferedImage croppedImage = ImageUtil.cropText(linearNormalized);
            ImageIO.write(croppedImage, "png", new File("debug05.png"));

            
            System.out.println("ReadImage: " + ImageUtil.ocr(croppedImage, ImageUtil.WHITELIST_FOR_NUMBERS_AND_SLASH, ImageUtil.SINGLE_LINE_MODE));
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
