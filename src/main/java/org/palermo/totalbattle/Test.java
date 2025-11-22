package org.palermo.totalbattle;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
public class Test {

    public static void main(String[] args) {
        BufferedImage image = ImageUtil.loadResource("test.png");
        //image = ImageUtil.toGrayscale(image);
        // image = ImageUtil.toGrayscale(image);

        image = ImageUtil.toGrayscale(image, new String[] {"FF9900", "FFE04E"});
        image = ImageUtil.resize(image, 400);
        image = ImageUtil.linearNormalization(image);

        //image = ImageUtil.increaseContrast(image);
        ImageUtil.showImageAndWait(image);
        System.out.println(ImageUtil.ocrBestMethod(image, ImageUtil.WHITELIST_FOR_SPEED_UPS));
    }
}
