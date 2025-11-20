package org.palermo.totalbattle.player.task;


import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

public class SummoningCircle {

    private final MyRobot robot;
    private final Player player;
    
    public SummoningCircle(MyRobot robot, Player player) {
        this.robot = robot;
        this.player = player;
    }

    public void evaluate() {
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(150);


        BufferedImage screen = robot.captureScreen();
        BufferedImage labelCity = ImageUtil.loadResource("player/label_city.png");
        Area labelCityArea = Area.fromTwoPoints(664, 1059, 716, 1075);
        Point labelCityPoint = ImageUtil.searchSurroundings(labelCity, screen, labelCityArea, 0.1, 20).orElse(null);

        if (labelCityPoint != null) {
            robot.leftClick(labelCityPoint.move(10, -30));
            robot.sleep(2500);
        }


        screen = robot.captureScreen();
        BufferedImage iconZoomMinus = ImageUtil.loadResource("player/icon_zoom_minus.png");
        Area iconZoomMinusArea = Area.fromTwoPoints(1791, 1003, 1836, 1044);
        Point iconZoomMinusPoint = ImageUtil.searchSurroundings(iconZoomMinus, screen, iconZoomMinusArea, 0.1, 20).orElse(null);

        if (iconZoomMinusPoint == null) {
            throw new RuntimeException("Icon zoom minus not found!");
        }

        for (int i = 0; i < 4; i++) {
            robot.leftClick(iconZoomMinusPoint, iconZoomMinus);
            robot.sleep(250);
        }

        robot.mouseDrag(Point.of(1350, 446), -240, 150);
        robot.sleep(250);

        screen = robot.captureScreen();
        BufferedImage scIcon = ImageUtil.loadResource("player/sc/sc_ground.png");
        Area scIconArea = Area.fromTwoPoints(878, 391, 1040, 463);
        Point scIconPoint = ImageUtil.searchSurroundings(scIcon, screen, scIconArea, 0.1, 20).orElse(null);

        if (scIconPoint == null) {
            throw new RuntimeException("Summoning circle icon ground not found!");
        }
        robot.leftClick(scIconPoint, scIcon);
        robot.sleep(1000);

        screen = robot.captureScreen();
        BufferedImage iconCaptainSummon = ImageUtil.loadResource("player/sc/icon_captain_summon.png");
        Area iconCaptainSummonArea = Area.fromTwoPoints(931, 450, 1003, 530);
        Point iconCaptainSummonPoint = ImageUtil.searchSurroundings(iconCaptainSummon, screen, iconCaptainSummonArea, 0.1, 20).orElse(null);

        if (iconCaptainSummonPoint == null) {
            throw new RuntimeException("Captain summon icon not found!");
        }
        robot.leftClick(iconCaptainSummonPoint, iconCaptainSummon);
        robot.sleep(1500);

        collectCaptainFragments();

        collectArtifacts();

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(150);
    }

    private void collectArtifacts() {

        BufferedImage screen = robot.captureScreen();

        BufferedImage iconArtifact = ImageUtil.loadResource("player/sc/icon_artifact.png");
        Area iconArtifactArea = Area.fromTwoPoints(1692, 225, 1833, 312);

        ImageUtil.write(ImageUtil.crop(screen, iconArtifactArea), "error_screen.png");
        ImageUtil.write(iconArtifact, "error_image.png");

        Point iconArtifactPoint = ImageUtil.searchSurroundings(iconArtifact, screen, iconArtifactArea, 0.1, 20).orElse(null);

        if (iconArtifactPoint == null) {
            SharedData.INSTANCE.setWait(player, Scenario.SUMMONING_CIRCLE_ARTIFACT_FRAGMENT, LocalDateTime.now().plusHours(12));
            return;
        }
        robot.leftClick(iconArtifactPoint, iconArtifact);
        robot.sleep(750);

        screen = robot.captureScreen();
        BufferedImage buttonFree = ImageUtil.loadResource("player/sc/button_free.png");
        Area buttonFreeArea = Area.fromTwoPoints(795, 759, 936, 807);
        // ImageUtil.showImageAndWait(ImageUtil.crop(screen, buttonFreeArea));
        Point buttonFreePoint = ImageUtil.searchSurroundings(buttonFree, screen, buttonFreeArea, 0.1, 20).orElse(null);

        if (buttonFreePoint != null) {
            robot.leftClick(buttonFreePoint, buttonFree);
            robot.sleep(7500);

            Navigate.builder()
                    .resourceName("player/sc/button_return.png")
                    .area(Area.fromTwoPoints(684, 815, 1032, 865))
                    .waitLimit(10000)
                    .build()
                    .leftClick();
            robot.sleep(500);
        }

        screen = robot.captureScreen();

        BufferedImage iconKey = ImageUtil.loadResource("player/sc/icon_key.png");
        Area iconKeyArea = RegionSelector.selectArea("SUMMON_CIRCLE_ARTIFACT_CHEST_KEY", screen);
        Point iconKeyAreaPoint = ImageUtil.searchSurroundings(iconKey, screen, iconKeyArea, 0.1, 20).orElse(null);

        int fragments = 0;

        if (iconKeyAreaPoint != null) {
            Area fragmentsQtdArea = Area.of(iconKeyAreaPoint.getX() + 25, iconKeyAreaPoint.getY() + 3, 36, 17);
            fragments =  getArtifactQuantity(ImageUtil.crop(screen, fragmentsQtdArea));
        }
        
        if (fragments == 0) {
            BufferedImage iconHourglass = ImageUtil.loadResource("player/sc/icon_hourglass.png");
            Area iconHourglassArea = RegionSelector.selectArea("SUMMON_CIRCLE_ARTIFACT_HOURGLASS", screen);
            Point iconHourglassPoint = ImageUtil.searchSurroundings(iconHourglass, screen, iconHourglassArea, 0.15, 20).orElse(null);

            // ImageUtil.showImageAndWait(screen, iconHourglassArea);
            
            if (iconHourglassPoint == null) {
                throw new RuntimeException("No hourglass and no free button");
            }

            BufferedImage timerImage = ImageUtil.crop(screen, Area.of(iconHourglassPoint.getX() + 16, iconHourglassPoint.getY() - 2, 110, 20));
            SharedData.INSTANCE.setWait(player, Scenario.SUMMONING_CIRCLE_ARTIFACT_FRAGMENT, ImageUtil.ocrTimer(timerImage, true));
        }
    }

    private static int getArtifactQuantity(BufferedImage input) {
        int qtd = 0;
        try {
            BufferedImage image = ImageUtil.toGrayscale(input);
            image = ImageUtil.invertGrayscale(image);
            image = ImageUtil.linearNormalization(image);
            image = ImageUtil.resize(image, 50);

            String numberAsText = ImageUtil.ocrBestMethod(image, ImageUtil.WHITELIST_FOR_ONLY_NUMBERS);
            qtd = Integer.parseInt(numberAsText);
        } catch (NumberFormatException e) {
            ImageUtil.showImageFor5Seconds(input, "Fail to parse artifact quantity!");
            throw e;
        }
        return qtd;
    }

    private void collectCaptainFragments() {
        BufferedImage buttonReturn = ImageUtil.loadResource("player/sc/button_return.png");

        BufferedImage screen = robot.captureScreen();

        BufferedImage buttonFree = ImageUtil.loadResource("player/sc/button_free.png");
        Area buttonFreeArea = Area.fromTwoPoints(568, 771, 726, 811);
        Point buttonFreePoint = ImageUtil.searchSurroundings(buttonFree, screen, buttonFreeArea, 0.1, 20).orElse(null);

        if (buttonFreePoint != null) { // Common Artifact
            robot.leftClick(buttonFreePoint, buttonFree);
            robot.sleep(5500);

            screen = robot.captureScreen();
            Area buttonReturnArea = Area.fromTwoPoints(784, 805, 932, 895);
            Point buttonReturnPoint = ImageUtil.searchSurroundings(buttonReturn, screen, buttonReturnArea, 0.1, 20).orElse(null);

            if (buttonReturnPoint == null) {
                throw new RuntimeException("Button return icon not found!");
            }
            robot.leftClick(buttonReturnPoint, buttonReturn);
            robot.sleep(500);
        }

        buttonFreeArea = Area.fromTwoPoints(1084, 771, 1247, 811);
        buttonFreePoint = ImageUtil.searchSurroundings(buttonFree, screen, buttonFreeArea, 0.1, 20).orElse(null);

        if (buttonFreePoint != null) { // Button free is available
            robot.leftClick(buttonFreePoint, buttonFree);
            robot.sleep(5500);

            screen = robot.captureScreen();
            Area buttonReturnArea = Area.fromTwoPoints(784, 805, 932, 895);
            Point buttonReturnPoint = ImageUtil.searchSurroundings(buttonReturn, screen, buttonReturnArea, 0.1, 20).orElse(null);

            if (buttonReturnPoint == null) {
                throw new RuntimeException("Button return icon not found!");
            }
            robot.leftClick(buttonReturnPoint, buttonReturn);
            robot.sleep(1000);
        }

        screen = robot.captureScreen();

        BufferedImage iconArtifact = ImageUtil.loadResource("player/sc/icon_fragment.png");
        Area iconArtifactArea = RegionSelector.selectArea("SUMMON_CIRCLE_TOP_FRAGMENT", screen);
        Point iconArtifactPoint = ImageUtil.searchSurroundings(iconArtifact, screen, iconArtifactArea, 0.1, 20).orElse(null);

        int commonArtifactQtd = 0;
        int eliteArtifactQtd = 0;

        if (iconArtifactPoint != null) {

            Area commonArtifactQtdArea = Area.of(iconArtifactPoint, Point.of(918, 180), Point.of(947, 183), Point.of(979, 199));
            commonArtifactQtd = getArtifactQuantity(ImageUtil.crop(screen, commonArtifactQtdArea));

            Area eliteArtifactQtdArea = Area.of(iconArtifactPoint, Point.of(918, 180), Point.of(1037, 183), Point.of(1067, 199));
            eliteArtifactQtd = getArtifactQuantity(ImageUtil.crop(screen, eliteArtifactQtdArea));
        }

        if (commonArtifactQtd == 0) {
            screen = robot.captureScreen();

            BufferedImage iconHourglass = ImageUtil.loadResource("player/sc/icon_hourglass.png");
            Area iconHourglassArea = RegionSelector.selectArea("SUMMON_CIRCLE_COMMON_CAPTAIN_HOURGLASS", screen);
            Point iconHourglassPoint = ImageUtil.searchSurroundings(iconHourglass, screen, iconHourglassArea, 0.15, 20).orElse(null);

            if (iconHourglassPoint == null) {
                throw new RuntimeException("No hourglass and no free button");
            }

            BufferedImage timerImage = ImageUtil.crop(screen, Area.of(iconHourglassPoint.getX() + 16, iconHourglassPoint.getY() - 2, 110, 20));
            SharedData.INSTANCE.setWait(player, Scenario.SUMMONING_CIRCLE_COMMON_CAPTAIN_FRAGMENT, ImageUtil.ocrTimer(timerImage, true));

        }
        else {
            collectCommonCaptainFragments(commonArtifactQtd);
        }
        
        if (eliteArtifactQtd == 0) {
            screen = robot.captureScreen();

            BufferedImage iconHourglass = ImageUtil.loadResource("player/sc/icon_hourglass.png");
            Area iconHourglassArea = RegionSelector.selectArea("SUMMON_CIRCLE_ELITE_CAPTAIN_HOURGLASS", screen);
            Point iconHourglassPoint = ImageUtil.searchSurroundings(iconHourglass, screen, iconHourglassArea, 0.15, 20).orElse(null);

            if (iconHourglassPoint == null) {
                throw new RuntimeException("No hourglass and no free button");
            }

            BufferedImage timerImage = ImageUtil.crop(screen, Area.of(iconHourglassPoint.getX() + 16, iconHourglassPoint.getY() - 2, 110, 20));
            SharedData.INSTANCE.setWait(player, Scenario.SUMMONING_CIRCLE_ELITE_CAPTAIN_FRAGMENT, ImageUtil.ocrTimer(timerImage, true));
        }
        else {
            collectEliteCaptainFragments(eliteArtifactQtd);
        }

    }

    private void collectCommonCaptainFragments(int qtd) {
        BufferedImage screen = robot.captureScreen();

        BufferedImage iconQuestionMark = ImageUtil.loadResource("player/sc/icon_question_mark.png");
        Area iconQuestionMarkArea = RegionSelector.selectArea("SUMMON_CIRCLE_COMMON_ARTIFACT_QUESTION_MARK", screen);
        Point iconQuestionMarkPoint = ImageUtil.searchSurroundings(iconQuestionMark, screen, iconQuestionMarkArea, 0.1, 20).orElse(null);

        if (iconQuestionMarkPoint == null) {
            throw new RuntimeException("Could not find question mark!");
        }

        robot.leftClick(iconQuestionMarkPoint.move(112, 56));
        robot.sleep(5000);

        screen = robot.captureScreen();
        BufferedImage buttonReturn = ImageUtil.loadResource("player/sc/button_return.png");
        Area buttonReturnArea =  RegionSelector.selectArea("SUMMON_CIRCLE_COMMON_RETURN_BUTTON", screen);
        Point buttonReturnPoint = ImageUtil.searchSurroundings(buttonReturn, screen, buttonReturnArea, 0.1, 20).orElse(null);

        if (buttonReturnPoint == null) {
            throw new RuntimeException("Button return icon not found!");
        }
        robot.leftClick(buttonReturnPoint, buttonReturn);
        robot.sleep(500);
    }

    private void collectEliteCaptainFragments(int qtd) {
        BufferedImage screen = robot.captureScreen();

        BufferedImage iconQuestionMark = ImageUtil.loadResource("player/sc/icon_question_mark.png");
        Area iconQuestionMarkArea = RegionSelector.selectArea("SUMMON_CIRCLE_ELITE_CAPTAIN_QUESTION_MARK", screen);
        Point iconQuestionMarkPoint = ImageUtil.searchSurroundings(iconQuestionMark, screen, iconQuestionMarkArea, 0.1, 20).orElse(null);

        if (iconQuestionMarkPoint == null) {
            throw new RuntimeException("Could not find question mark!");
        }

        robot.leftClick(iconQuestionMarkPoint.move(0, 56));
        robot.sleep(5000);

        screen = robot.captureScreen();
        BufferedImage buttonReturn = ImageUtil.loadResource("player/sc/button_return.png");
        Area buttonReturnArea =  RegionSelector.selectArea("SUMMON_CIRCLE_ELITE_RETRIEVED_RETURN_BUTTON", screen);
        Point buttonReturnPoint = ImageUtil.searchSurroundings(buttonReturn, screen, buttonReturnArea, 0.1, 20).orElse(null);

        if (buttonReturnPoint == null) {
            throw new RuntimeException("Button return icon not found!");
        }
        robot.leftClick(buttonReturnPoint, buttonReturn);
        robot.sleep(500);
    }
}
