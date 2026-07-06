package org.palermo.totalbattle.selenium.leadership;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.RegionSelector;
import org.palermo.totalbattle.selenium.leadership.model.TroopQuantity;
import org.palermo.totalbattle.selenium.stacking.Attribute;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;
import org.palermo.totalbattle.util.OcrUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Backend {
    
    private static final MyRobot robot = MyRobot.INSTANCE;
    
    public enum MonsterOverride {
          DEFAULT("Default"), EXCLUDE_ALL("Exclude all"), INCLUDE_ALL("Include all");

        private final String label;

        MonsterOverride(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    
    public static List<Unit> getUnits(String playerName, 
                                      Set<Attribute> exclusions, 
                                      int tiers, 
                                      MonsterOverride monsterOverride) {
        
        List<Unit> troops = new ArrayList<>();
        
        Player player = Player.getPlayerByName(playerName);
        
        switch(player) {
            case PALERMO -> {
                if (tiers >= 4) {
                    troops.add(Unit.S4_SWORDSMAN);
                    troops.add(Unit.G4_MELEE);
                    troops.add(Unit.G4_MOUNTED);

                    troops.add(Unit.S5_DEADSHOT);
                    troops.add(Unit.G5_RANGED);
                }
                if (tiers >= 3) {
                    troops.add(Unit.S5_SWORDSMAN);
                    troops.add(Unit.S5_VULTURE);
                    troops.add(Unit.S5_LION_RIDER);

                    troops.add(Unit.G5_MELEE);
                    troops.add(Unit.G5_MOUNTED);
                    troops.add(Unit.G5_GRIFFIN);

                    troops.add(Unit.S6_RANGED);
                    troops.add(Unit.G6_RANGED);
                }
                if (tiers >= 2) {
                    troops.add(Unit.S6_MELEE);
                    troops.add(Unit.S6_FLYING);
                    troops.add(Unit.S6_MOUNTED);

                    troops.add(Unit.G6_MELEE);
                    troops.add(Unit.G6_MOUNTED);
                    troops.add(Unit.G6_GRIFFIN);

                    troops.add(Unit.G7_RANGED);
                }
                if (tiers >= 1) {
                    troops.add(Unit.G7_MELEE);
                    troops.add(Unit.G7_MOUNTED);
                    troops.add(Unit.G7_GRIFFIN);

                    troops.add(Unit.G8_RANGED);
                }

                if (monsterOverride == MonsterOverride.INCLUDE_ALL || 
                        monsterOverride == MonsterOverride.DEFAULT) {
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL) {
                        troops.add(Unit.DRAGON_III);
                        troops.add(Unit.ELEMENTAL_III);
                        troops.add(Unit.GIANT_III);
                        troops.add(Unit.BEAST_III);

                        troops.add(Unit.DRAGON_IV);
                        troops.add(Unit.ELEMENTAL_IV);
                        troops.add(Unit.GIANT_IV);
                        troops.add(Unit.BEAST_IV);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 3) {
                        troops.add(Unit.DRAGON_V);
                        troops.add(Unit.ELEMENTAL_V);
                        troops.add(Unit.GIANT_V);
                        troops.add(Unit.BEAST_V);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 2) {
                        troops.add(Unit.DRAGON_VI);
                        troops.add(Unit.ELEMENTAL_VI);
                        troops.add(Unit.GIANT_VI);
                        troops.add(Unit.BEAST_VI);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 1) {
                        troops.add(Unit.DRAGON_VII);
                        troops.add(Unit.ELEMENTAL_VII);
                        troops.add(Unit.GIANT_VII);
                        troops.add(Unit.BEAST_VII);
                    }
                }
                if (monsterOverride != MonsterOverride.EXCLUDE_ALL) {
                    troops.add(Unit.EPIC_MONSTER_HUNTER_IX);
                }
            }
            case PETER -> {
                if (tiers >= 3) {
                    troops.add(Unit.S3_SWORDSMAN);
                    troops.add(Unit.G3_RANGED);
                    troops.add(Unit.G3_MELEE);
                    troops.add(Unit.G3_MOUNTED);
                }
                if (tiers >= 2) {
                    troops.add(Unit.S4_SWORDSMAN);
                    troops.add(Unit.G4_RANGED);
                    troops.add(Unit.G4_MELEE);
                    troops.add(Unit.G4_MOUNTED);
                }
                if (tiers >= 1) {
                    troops.add(Unit.G5_RANGED);
                    troops.add(Unit.G5_MELEE);
                    troops.add(Unit.G5_MOUNTED);
                    troops.add(Unit.G5_GRIFFIN);
                }

                if (monsterOverride == MonsterOverride.INCLUDE_ALL ||
                        monsterOverride == MonsterOverride.DEFAULT) {
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 3) {
                        troops.add(Unit.DRAGON_III);
                        troops.add(Unit.ELEMENTAL_III);
                        troops.add(Unit.GIANT_III);
                        troops.add(Unit.BEAST_III);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 2) {
                        troops.add(Unit.DRAGON_IV);
                        troops.add(Unit.ELEMENTAL_IV);
                        troops.add(Unit.GIANT_IV);
                        troops.add(Unit.BEAST_IV);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 1) {
                        troops.add(Unit.DRAGON_V);
                        troops.add(Unit.ELEMENTAL_V);
                        troops.add(Unit.GIANT_V);
                        troops.add(Unit.BEAST_V);
                    }
                }

                if (monsterOverride != MonsterOverride.EXCLUDE_ALL) {
                    troops.add(Unit.EPIC_MONSTER_HUNTER_VI);
                    troops.add(Unit.ARBALESTER_VI);
                    troops.add(Unit.LEGIONARY_VI);
                    troops.add(Unit.CHARIOT_VI);
                    troops.add(Unit.SPHYNX_VI);

                    troops.add(Unit.EPIC_MONSTER_HUNTER_VII);
                }
            }
            case MIGHTSHAPER -> {
                if (tiers >= 3) {
                    troops.add(Unit.S3_SWORDSMAN);
                    troops.add(Unit.G3_RANGED);
                    troops.add(Unit.G3_MELEE);
                    troops.add(Unit.G3_MOUNTED);
                }
                if (tiers >= 2) {
                    troops.add(Unit.S4_SWORDSMAN);
                    troops.add(Unit.G4_RANGED);
                    troops.add(Unit.G4_MELEE);
                    troops.add(Unit.G4_MOUNTED);
                }
                if (tiers >= 1) {
                    troops.add(Unit.G5_RANGED);
                    troops.add(Unit.G5_MELEE);
                    troops.add(Unit.G5_MOUNTED);
                    troops.add(Unit.G5_GRIFFIN);
                }

                if (monsterOverride == MonsterOverride.INCLUDE_ALL ||
                        monsterOverride == MonsterOverride.DEFAULT) {
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 3) {
                        troops.add(Unit.DRAGON_III);
                        troops.add(Unit.ELEMENTAL_III);
                        troops.add(Unit.GIANT_III);
                        troops.add(Unit.BEAST_III);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 2) {
                        troops.add(Unit.DRAGON_IV);
                        troops.add(Unit.ELEMENTAL_IV);
                        troops.add(Unit.GIANT_IV);
                        troops.add(Unit.BEAST_IV);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 1) {
                        troops.add(Unit.DRAGON_V);
                        troops.add(Unit.ELEMENTAL_V);
                        troops.add(Unit.GIANT_V);
                        troops.add(Unit.BEAST_V);
                    }
                }

                if (monsterOverride != MonsterOverride.EXCLUDE_ALL) {
                    troops.add(Unit.EPIC_MONSTER_HUNTER_VI);
                    troops.add(Unit.ARBALESTER_VI);
                    troops.add(Unit.LEGIONARY_VI);
                    troops.add(Unit.CHARIOT_VI);
                    troops.add(Unit.SPHYNX_VI);

                    troops.add(Unit.EPIC_MONSTER_HUNTER_VII);
                }
            }
            case GRIRANA -> {
                if (tiers >= 3) {
                    troops.add(Unit.S2_SWORDSMAN);
                    troops.add(Unit.G2_RANGED);
                    troops.add(Unit.G2_MELEE);
                    troops.add(Unit.G2_MOUNTED);
                    
                }
                if (tiers >= 2) {
                    troops.add(Unit.G3_RANGED);
                    troops.add(Unit.G3_MELEE);
                    troops.add(Unit.G3_MOUNTED);
                }
                if (tiers >= 1) {
                    troops.add(Unit.G4_RANGED);
                    troops.add(Unit.G4_MELEE);
                    troops.add(Unit.G4_MOUNTED);
                }

                if (monsterOverride == MonsterOverride.INCLUDE_ALL ||
                        monsterOverride == MonsterOverride.DEFAULT) {
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 1) {
                        troops.add(Unit.DRAGON_IV);
                        troops.add(Unit.ELEMENTAL_IV);
                        troops.add(Unit.GIANT_IV);
                        troops.add(Unit.BEAST_IV);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 2) {
                        troops.add(Unit.DRAGON_III);
                        troops.add(Unit.ELEMENTAL_III);
                        troops.add(Unit.GIANT_III);
                        troops.add(Unit.BEAST_III);
                    }
                }

                troops.add(Unit.EPIC_MONSTER_HUNTER_VI);
            }
            case ELANIN -> {
                if (tiers >= 3) {
                    troops.add(Unit.S2_SWORDSMAN);
                    troops.add(Unit.G2_RANGED);
                    troops.add(Unit.G2_MELEE);
                    troops.add(Unit.G2_MOUNTED);
                }
                if (tiers >= 2) {
                    troops.add(Unit.G3_RANGED);
                    troops.add(Unit.G3_MELEE);
                    troops.add(Unit.G3_MOUNTED);
                }
                if (tiers >= 1) {
                    troops.add(Unit.G4_RANGED);
                    troops.add(Unit.G4_MELEE);
                    troops.add(Unit.G4_MOUNTED);
                }

                if (monsterOverride == MonsterOverride.INCLUDE_ALL ||
                        monsterOverride == MonsterOverride.DEFAULT) {
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 1) {
                        troops.add(Unit.DRAGON_IV);
                        troops.add(Unit.ELEMENTAL_IV);
                        troops.add(Unit.GIANT_IV);
                        troops.add(Unit.BEAST_IV);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 2) {
                        troops.add(Unit.DRAGON_III);
                        troops.add(Unit.ELEMENTAL_III);
                        troops.add(Unit.GIANT_III);
                        troops.add(Unit.BEAST_III);
                    }
                }

                troops.add(Unit.EPIC_MONSTER_HUNTER_VI);
            }
            case LORVEN -> {
                if (tiers >= 1) {
                    troops.add(Unit.G1_RANGED);
                    troops.add(Unit.G1_MELEE);
                    troops.add(Unit.G1_MOUNTED);
                }
            }
            default -> throw new RuntimeException("Not implemented for " + playerName);
        }

        return troops
                .stream()
                .filter(unit -> !unit.wasExcluded(exclusions))
                .toList();
    } 
    

    public static int[] getUnitQuantity(int[] headCount, List<Unit> units) {
        ConfigurationBuilder builder = Configuration.builder()
                .leadership(headCount[0])
                .dominance(headCount[1]) // Monsters
                .authority(headCount[2]);

        for (Unit unit: units) {
                builder.addUnit(unit);
        }

        return builder.build().resolve();
    }


    public static String getPlayerName(BufferedImage screen) {
        BufferedImage feather = ImageUtil.loadResource("leadership/feather.png");

        Point position = ImageUtil.search(feather, screen, 950, 187, 50, 40, 0.05).
                orElseThrow(() -> new RuntimeException("Feather not found"));

        if (position == null) {
            System.out.println("Not found!");
        }
        BufferedImage imageWithText = ImageUtil.crop(screen, Area.of(position, Point.of(962, 194), Point.of(751, 197), Point.of(883, 213)));

        BufferedImage invertedGray = ImageUtil.toGrayscale(imageWithText);
        BufferedImage linearNormalized = ImageUtil.linearNormalization(invertedGray);
        BufferedImage croppedImage = ImageUtil.cropText(linearNormalized);

        return OcrUtil.ocr(croppedImage, OcrUtil.WHITELIST_FOR_USERNAME, OcrUtil.SINGLE_LINE_MODE);
    }

    public static int[] getHeadCount() {
        BufferedImage screen = robot.captureScreen();

        Point startMarchButtonLocation = getStartMarchButtonLocation();
        System.out.println(String.format("Start Button Location %d %d", startMarchButtonLocation.getX(), startMarchButtonLocation.getY()));
        Transformation transformation = Transformation.builder()
                .reference(Point.of(853, 877))
                .real(startMarchButtonLocation)
                .build();

        enableDragon(transformation);

        int[] result;
        
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            CompletableFuture<Integer> leadershipF = CompletableFuture.supplyAsync(
                    () -> getHeadCountLimit(screen, transformation.transform(Point.of(562, 820), Point.of(642, 837))),
                    pool);

            CompletableFuture<Integer> dominanceF = CompletableFuture.supplyAsync(
                    () -> getHeadCountLimit(screen, transformation.transform(Point.of(787, 820), Point.of(867, 837))),
                    pool);

            CompletableFuture<Integer> authorityF = CompletableFuture.supplyAsync(
                    () -> getHeadCountLimit(screen, transformation.transform(Point.of(674, 820), Point.of(754, 837))),
                    pool);

            int leadership  = leadershipF.join();
            int dominance   = dominanceF.join();
            int authority   = authorityF.join();
            
            result = new int[] {leadership, dominance, authority};

        } finally {
            pool.shutdown();
        }

        return result;
    }

    private static int getHeadCountLimit(BufferedImage screen, Area area) {
        // showImageAndWait(ImageUtil.crop(screen, area));
        ImageUtil.write(ImageUtil.crop(screen, area), "debug.png");
        
        BufferedImage imageWithText = screen.getSubimage(area.getX(), area.getY(), area.getWidth(), area.getHeight());
        BufferedImage invertedGray = ImageUtil.invertGrayscale(imageWithText);
        BufferedImage linearNormalized = ImageUtil.linearNormalization(invertedGray);
        BufferedImage croppedImage = ImageUtil.cropText(linearNormalized);
        ImageUtil.write(croppedImage, "leadership_text.png");

        String leadershipText = OcrUtil.ocr(croppedImage, OcrUtil.WHITELIST_FOR_NUMBERS_AND_SLASH_AND_MULTIPLIER, OcrUtil.LINE_OF_PRINTED_TEXT);
        leadershipText = leadershipText.replaceAll(",", "");
        int slashIndex = leadershipText.indexOf("/");

        if (slashIndex == -1) {
            throw new RuntimeException("Invalid format! " + leadershipText);
        }
        leadershipText = leadershipText.substring(slashIndex + 1);
        int multiplier = 1;
        int toAdd = 0;
        if (leadershipText.charAt(leadershipText.length() - 1) == 'K') {
            multiplier = 1000;
            leadershipText = leadershipText.substring(0, leadershipText.length() - 1);
            toAdd = missingNines(leadershipText);
        }
        
        return (int) Math.round(Double.parseDouble(leadershipText) * multiplier) + toAdd;
    }

    public static int missingNines(String input) {
        int decimalPos = input.indexOf('.');

        int decimalDigits = decimalPos < 0
                ? 0
                : input.length() - decimalPos - 1;

        int missing = 3 - decimalDigits;

        if (missing <= 0) {
            return 0;
        }

        return Integer.parseInt("9".repeat(missing));
    }
    private static void enableDragon(Transformation transformation) {
        Area area = transformation.transform(Point.of(636, 639), Point.of(772, 693));
        BufferedImage sendDragonCheckBox = ImageUtil.loadResource("leadership/send_dragon.png");
        BufferedImage screen = robot.captureScreen();
        Point sendDragonLocation = ImageUtil.search(sendDragonCheckBox, screen, area, 0.01).orElse(null);
        if (sendDragonLocation != null) {
            robot.leftClick(sendDragonLocation.move(20, 8));
            System.out.println("Clicked on the dragon");
        }
        else {
            System.out.println("Dragon is not enabled");
        }
    }

    private static void showImageAndWait(BufferedImage image) {
        showImageAndWait(image, null);
    }


    private static void showImageAndWait(BufferedImage image, String title) {
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

    private static Point getStartMarchButtonLocation() {
        //Area area = RegionSelector.selectArea("FILL_TROOPS_START_MARCH_BUTTON", robot.captureScreen());
        //ImageUtil.showImageAndWait(robot.captureScreen(), area);
        Navigate navigate = Navigate.builder()
                .resourceName("player/army/button_start_march.png")
                .areaName("FILL_TROOPS_START_MARCH_BUTTON")
                .build()
                .ensureExistence();
        return navigate.getPoint();
    }

    private static Point getCloseButtonLocation() {
        BufferedImage screen = robot.captureScreen();
        Area area = Area.of(1380, 300, 250, 250);    
        
        ImageUtil.showImageAndWait(screen, area);
        BufferedImage closeButtonImage = ImageUtil.loadResource("leadership/close_button.png");
        return ImageUtil.search(closeButtonImage, screen, area, 0.05)
                .orElseThrow(() -> new RuntimeException("Cannot find the close button"));
                
    }    
    


    public static void fillTroops(MyRobot robot, List<TroopQuantity> stack) {

        Point startMarchButtonLocation = getStartMarchButtonLocation();
        System.out.println(String.format("Start Button Location %d %d", startMarchButtonLocation.getX(), startMarchButtonLocation.getY()));
        Transformation transformation = Transformation.builder()
                .reference(Point.of(853, 877))
                .real(startMarchButtonLocation)
                .build();
        
        enableDragon(transformation);

        stack(transformation, stack);

        Toolkit.getDefaultToolkit().beep();
    }

    private static void stack(Transformation transformation, List<TroopQuantity> stack) {
        BufferedImage screen = robot.captureScreen();

        Area leftPanel = transformation.transform(Point.of(552, 403), Point.of(624, 803));
        //ImageUtil.write(ImageUtil.crop(screen, leftPanel), "left_panel.png");

        Area rightPanel = transformation.transform(Point.of(772, 403), Point.of(844, 803));
        //ImageUtil.write(ImageUtil.crop(screen, rightPanel), "right_panel.png");

        Area scrollBarArea = transformation.transform(Point.of(970, 420), Point.of(986, 785));
        BufferedImage oldPosition;
        BufferedImage newPosition = ImageUtil.crop(screen, scrollBarArea);

        Set<Integer> processed = Collections.synchronizedSet(new HashSet<>());

        int count = 0;
        int lastPosition = 0;

        do {
            oldPosition = newPosition;
            
            Object semaphore = new Object();

            final BufferedImage currentScreen = screen;
            IntStream.range(0, stack.size()).parallel().forEach(i -> {
                if (!processed.contains(i)) {
                    if (processUnit(robot, currentScreen, leftPanel, stack.get(i), semaphore)) {
                        processed.add(i);
                    }
                    else if (processUnit(robot, currentScreen, rightPanel, stack.get(i), semaphore)) {
                        processed.add(i);
                    }
                }
            });
            
            final int delta = 32;

            if (processed.size() < stack.size()) {
                Point scroolBar = transformation.transform(Point.of(978, 448));
                scroolBar = scroolBar.move(0, (count * delta));
                robot.mouseDrag(scroolBar, 0, delta);
                robot.sleep(150);
                count = count + 1;


                lastPosition = scroolBar.getY() + (count * delta);
                System.out.println("Last point " + lastPosition);
                
                screen = robot.captureScreen();
                newPosition = ImageUtil.crop(screen, scrollBarArea);
            }

        } while ((!ImageUtil.compare(newPosition, oldPosition, 0.01))  && 
                processed.size() < stack.size() &&
                lastPosition < 1081
        );

        if (processed.size() < stack.size()) {
            for (int i = 0; i < stack.size(); i++) {
                if (!processed.contains(i)) {
                    System.out.println("Missing: " + stack.get(i).getUnit().name());
                }
            }
        }
        
        Point startMarchButton = transformation.transform(Point.of(899, 880));
        robot.mouseMove(startMarchButton);
    }


    private static boolean processUnit(MyRobot robot, 
                                       BufferedImage originalScreen, 
                                       Area area, 
                                       TroopQuantity troopQuantity,
                                       Object semaphore) {
        Point point = ImageUtil.search(troopQuantity.getUnit().getIcon(), originalScreen, area, 0.05).orElse(null);
        if (point == null) {
            return false;
        }

        synchronized (semaphore) {
            robot.leftClick(point.move(135, 44));
            robot.typeString(Integer.toString(troopQuantity.getQuantity()));
            robot.sleep(150);
        }
        return true;
    }
}
