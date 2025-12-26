package org.palermo.totalbattle;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.OcrUtil;

import java.awt.image.BufferedImage;

@Slf4j
public class Test {

    public static void main3(String[] args) {
        System.out.println(Integer.parseInt("2147483648"));
    }

    public static void main2(String[] args) {
        BufferedImage image = ImageUtil.loadResource("test.png");
        //image = ImageUtil.toGrayscale(image);
        // image = ImageUtil.toGrayscale(image);

        image = ImageUtil.toGrayscale(image, new String[] {"FF9900", "FFE04E"});
        image = ImageUtil.resize(image, 400);
        image = ImageUtil.linearNormalization(image);

        //image = ImageUtil.increaseContrast(image);
        ImageUtil.showImageAndWait(image);
        System.out.println(OcrUtil.ocrBestMethod(image, OcrUtil.WHITELIST_FOR_SPEED_UPS));
    }
}
