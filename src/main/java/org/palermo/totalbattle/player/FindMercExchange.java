package org.palermo.totalbattle.player;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Random;

public class FindMercExchange {

    private final static MyRobot robot = MyRobot.INSTANCE;

    private static Navigate magnifierNavigate;
    private static Navigate buttonGoNavigate;
    
    private static final Mat template = ImageUtil.loadResourceAsMat("merc_exchange/exchange.png");

    private static final double THRESHOLD = 0.75;

    public static void main(String[] args) {
        
        NavigationUtil.switchToMapIfNeeded();
        
        NavigationUtil.zoomInIfNeeded();

        
        int kingdoms[] = new int[] {280, 281, 284, 288, 289, 279, 283, 287}; 
        
        Random random = new Random();
        
        for (int i = 0; i < 2; i++) {
            int kingdom = kingdoms[random.nextInt(kingdoms.length)];
            int x = random.nextInt(20, 999 - 20);
            int y = random.nextInt(15, 999 - 15);
            search(kingdom, x, y);                        
        }
    }
    
    private static void search(int kingdom, int x, int y) {
        if (magnifierNavigate == null) {
            magnifierNavigate = Navigate.builder()
                    .resourceName("player/icon_magnifier.png")
                    .areaName(Area.MAP_MAGNIFIER)
                    .waitLimit(10000)
                    .build()
                    .ensureExistence();
            
        }
        
        magnifierNavigate.leftClick();
        
        if (buttonGoNavigate == null) {
            buttonGoNavigate = Navigate.builder()
                    .areaName("NAVIGATE_BUTTON_GO")
                    .resourceName("player/button_go.png")
                    .waitLimit(5000)
                    .build().ensureExistence();
            System.out.println(String.format("Found button go at %d %d", buttonGoNavigate.getPoint().getX(), buttonGoNavigate.getPoint().getY()));
        }
        
        System.out.println(String.format("Using position %d %d", buttonGoNavigate.getPoint().getX(), buttonGoNavigate.getPoint().getY()));

        Transformation transformation = Transformation.builder()
                .reference(buttonGoNavigate.getPoint())
                .real(Point.of(981, 617))
                .build();

        Point point = transformation.transform(Point.of(922, 580));
        System.out.println(String.format("Field %d %d", point.getX(), point.getY()));
        
        robot.leftClick(transformation.transform(Point.of(922, 580)));
        robot.clearText();
        robot.sleep(200);
        robot.typeString(Integer.toString(kingdom));

        robot.leftClick(transformation.transform(Point.of(1022, 580)));
        robot.clearText();
        robot.sleep(200);
        robot.typeString(Integer.toString(x));

        robot.leftClick(transformation.transform(Point.of(1127, 580)));
        robot.clearText();
        robot.sleep(200);
        robot.typeString(Integer.toString(y));

        buttonGoNavigate.leftClick();
        
        robot.sleep(1500);

        Mat screen = bufferedImageToGrayMat(robot.captureScreen());
        
        org.bytedeco.opencv.opencv_core.Point found = findTemplate(screen, template);
        
        if (found != null) {
            for (;;) {
                Toolkit.getDefaultToolkit().beep();
                robot.sleep(5000);
            }
        }

        robot.type(KeyEvent.VK_ESCAPE);
    }

    private static org.bytedeco.opencv.opencv_core.Point findTemplate(Mat screenGray, Mat templateGray) {
        int resultWidth = screenGray.cols() - templateGray.cols() + 1;
        int resultHeight = screenGray.rows() - templateGray.rows() + 1;

        if (resultWidth <= 0 || resultHeight <= 0) {
            throw new IllegalArgumentException("Template is bigger than the screen capture");
        }

        Mat result = new Mat(resultHeight, resultWidth, opencv_core.CV_32FC1);

        opencv_imgproc.matchTemplate(
                screenGray,
                templateGray,
                result,
                opencv_imgproc.TM_CCOEFF_NORMED
        );

        DoublePointer minVal = new DoublePointer(1);
        DoublePointer maxVal = new DoublePointer(1);
        org.bytedeco.opencv.opencv_core.Point minLoc = new org.bytedeco.opencv.opencv_core.Point();
        org.bytedeco.opencv.opencv_core.Point maxLoc = new org.bytedeco.opencv.opencv_core.Point();

        opencv_core.minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, null);

        double similarity = maxVal.get();

        if (similarity >= THRESHOLD) {
            return maxLoc;
        }

        return null;
    }

    private static Mat bufferedImageToGrayMat(BufferedImage image) {
        BufferedImage converted = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR
        );

        converted.getGraphics().drawImage(image, 0, 0, null);

        byte[] pixels = ((DataBufferByte) converted.getRaster().getDataBuffer()).getData();

        Mat mat = new Mat(image.getHeight(), image.getWidth(), opencv_core.CV_8UC3);
        mat.data().put(pixels);

        Mat gray = new Mat();
        opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

        return gray;
    }    
}
