package org.palermo.totalbattle.selenium.leadership;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Working {


    public static void main(String[] args) {
        MyRobot robot = new MyRobot();

        BufferedImage screen = robot.captureScreen();

        System.out.println("Player Name: " + getPlayerName(screen));
        
        int[] headCount = getHeadCount(screen);
        System.out.println("Head Count: " + headCount[0] + " - " + headCount[1] + " - " + headCount[2]);
    }
    
    private static String getPlayerName(BufferedImage screen) {
        BufferedImage feather = ImageUtil.loadResource("leadership/feather.png");

        Point position = ImageUtil.search(feather, screen, 950, 187, 50, 40, 0.05).
                orElseThrow(() -> new RuntimeException("Feather not found"));

        if (position == null) {
            System.out.println("Not found!");
        }
        System.out.println(position);


        BufferedImage imageWithText = ImageUtil.crop(screen, Area.of(position, Point.of(962, 194), Point.of(751, 197), Point.of(883, 213)));


        BufferedImage invertedGray = ImageUtil.toGrayscale(imageWithText);
        BufferedImage linearNormalized = ImageUtil.linearNormalization(invertedGray);
        BufferedImage croppedImage = ImageUtil.cropText(linearNormalized);

        return ImageUtil.ocr(croppedImage, ImageUtil.WHITELIST_FOR_USERNAME, ImageUtil.SINGLE_LINE_MODE);
    }
    
    private static int[] getHeadCount(BufferedImage screen) {
        BufferedImage closeButtonImage = ImageUtil.loadResource("leadership/close_button.png");
        Point closeButtonLocation = ImageUtil.search(closeButtonImage, screen, 1380, 300, 250, 250, 0.05)
                .orElseThrow(() -> new RuntimeException("Cannot find the close button"));

        int leadership = getHeadCountLimit(screen, Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(562, 839), Point.of(642, 856)));
        int dominance = getHeadCountLimit(screen, Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(787, 839), Point.of(867, 856)));
        int authority = getHeadCountLimit(screen, Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(674, 839), Point.of(754, 856)));

        return new int[] {leadership, dominance, authority};
    }

    private static int getHeadCountLimit(BufferedImage screen, Area area) {
        BufferedImage imageWithText = screen.getSubimage(area.getX(), area.getY(), area.getWidth(), area.getHeight());
        BufferedImage invertedGray = ImageUtil.invertGrayscale(imageWithText);
        BufferedImage linearNormalized = ImageUtil.linearNormalization(invertedGray);
        //BufferedImage contrastIncreased = ImageUtil.increaseContrast(linearNormalized);
        BufferedImage croppedImage = ImageUtil.cropText(linearNormalized);
        ImageUtil.write(croppedImage, "leadership_text.png");

        String leadershipText = ImageUtil.ocr(croppedImage, ImageUtil.WHITELIST_FOR_NUMBERS_AND_SLASH, ImageUtil.LINE_OF_PRINTED_TEXT);
        leadershipText = leadershipText.replaceAll(",", "");
        int slashIndex = leadershipText.indexOf("/");

        if (slashIndex == -1) {
            throw new RuntimeException("Invalid format! " + leadershipText);
        }
        return Integer.parseInt(leadershipText.substring(slashIndex + 1).trim());
    }


    public static void showImageAndWait(BufferedImage image) {
        showImageAndWait(image, null);
    }


    public static void showImageAndWait(BufferedImage image, String title) {
        Runnable ui = () -> {
            JDialog dialog = new JDialog((Frame) null, title != null ? title : "Image", true); // modal
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            JLabel lbl = new JLabel(new ImageIcon(image));
            JScrollPane scroller = new JScrollPane(lbl);
            dialog.getContentPane().add(scroller, BorderLayout.CENTER);

            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true); // blocks until dialog is disposed/closed
        };

        if (SwingUtilities.isEventDispatchThread()) {
            ui.run(); // safe: modal dialog pumps events
        } else {
            try {
                SwingUtilities.invokeAndWait(ui); // blocks caller until window closes
            } catch (Exception e) {
                throw new RuntimeException("Failed to show image", e);
            }
        }
    }
}
