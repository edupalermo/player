package org.palermo.totalbattle;

import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;
import org.palermo.totalbattle.util.OcrUtil;

import java.awt.image.BufferedImage;
import java.util.regex.Pattern;

public class UpgradeMaterials {

    private static final MyRobot robot = MyRobot.INSTANCE;

    private static final Pattern PERCENT = Pattern.compile("^[0-9]{1,2}%$");

    public static void main(String arg[]) {
        upgradeTo(8); // 8 is max
    }
    
    private static void upgradeTo(int level) {

        robot.mouseMove(Point.of(15,15));
        
        Point materialsLabelPoint = Navigate.builder()
                .areaName("MATERIALS_LABEL")
                .resourceName("player/materials/materials_label.png")
                .comparationLimit(0.05)
                .build()
                .ensureExistence()
                .getPoint();
        
        System.out.println(materialsLabelPoint);

        Transformation transformation = Transformation.builder()
                .real(materialsLabelPoint)
                .reference(Point.of(960, 334))
                .build();
        
        
        Area first = transformation.transform(Point.of(654, 521), Point.of(721, 536));
        
        Integer[][] cache = new Integer[level - 1][4];

        robot.fastLeftClick(materialsLabelPoint);

        for (int c = 0; c < 22; c++) {
            for (int i = 0; i < level - 1; i++) {
                for (int j = 0; j < 4; j++) {
                    Area area = first.move(i * 87, j * 87);

                    if (cache[i][j] == null) {
                        BufferedImage qtdAsImage = robot.captureScreen(area);
                        cache[i][j] = ocr(qtdAsImage);
                    }

                    System.out.println(String.format("(%d,%d) %d", i, j, cache[i][j]));
                    
                    if (cache[i][j] >= 4) {
                        robot.fastLeftClick(area.getX() + 52, area.getY());
                        robot.fastLeftClick(materialsLabelPoint);
                        cache[i][j] = cache[i][j] - 4;
                        if (i < level - 2 && cache[i + 1][j] != null) {
                            cache[i + 1][j] = cache[i +1][j] + 1;
                        }
                    }
                }
            }
        }
    }

    private static int ocr(BufferedImage input) {
        try {
            BufferedImage image = ImageUtil.toGrayscale(input, new String[] {"FFF7BF"});
            image = ImageUtil.linearNormalization(image);
            image =ImageUtil.cropText(image);
            image = ImageUtil.linearNormalization(image);
            if (image.getHeight() < OcrUtil.OCR_HEIGHT) {
                image = ImageUtil.resize(image, OcrUtil.OCR_HEIGHT);
            }
            String asString = OcrUtil.ocr(image, OcrUtil.WHITELIST_FOR_NUMBERS_AND_MULTIPLIER, OcrUtil.PATTERN_FOR_NUMBERS_WITH_MULTIPLIER, false);
            return parseNumber(asString);
        } catch (Exception e) {
            return 0;
        }
    }

    
    private static int parseNumber(String input) {
        double multiplier = 1d;
        if (input.charAt(input.length() - 1) == 'K') {
            multiplier = 1000d;
            input = input.substring(0, input.length() - 1);
        }
        return (int) (Double.parseDouble(input) * multiplier);
    }

}
