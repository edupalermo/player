package org.palermo.totalbattle.selenium.leadership;

import org.palermo.totalbattle.selenium.leadership.model.TroopQuantity;
import org.palermo.totalbattle.selenium.stacking.Attribute;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Stacking;
import org.palermo.totalbattle.selenium.stacking.Unit;

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
    };

    
    public static List<Unit> getUnits(String player, 
                                      Set<Attribute> exclusions, 
                                      int tiers, 
                                      MonsterOverride monsterOverride) {
        
        List<Unit> troops = new ArrayList<>();
        
        switch(Stacking.Player.from(player)) {
            case PALERMO -> {
                if (tiers >= 4) {
                    troops.add(Unit.S2_SWORDSMAN);
                    troops.add(Unit.G2_RANGED);
                    troops.add(Unit.G2_MELEE);
                    troops.add(Unit.G2_MOUNTED);
                }
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
                        troops.add(Unit.EMERALD_DRAGON);
                        troops.add(Unit.WATER_ELEMENTAL);
                        troops.add(Unit.STONE_GARGOYLE);
                        troops.add(Unit.BATTLE_BOAR);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 2) {
                        troops.add(Unit.MAGIC_DRAGON);
                        troops.add(Unit.ICE_PHOENIX);
                        troops.add(Unit.MANY_ARMED_GUARDIAN);
                        troops.add(Unit.GORGON_MEDUSA);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 1) {
                        troops.add(Unit.DESERT_VANQUISER);
                        troops.add(Unit.FLAMING_CENTAUR);
                        troops.add(Unit.ETTIN);
                        troops.add(Unit.FEARSOME_MANTICORE);
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
            case PETER_II, MIGHTSHAPER -> {
                if (tiers >= 3) {
                    troops.add(Unit.S2_SWORDSMAN);
                    troops.add(Unit.G2_RANGED);
                    troops.add(Unit.G2_MELEE);
                    troops.add(Unit.G2_MOUNTED);
                }
                if (tiers >= 2) {
                    //troops.add(Unit.S3_SWORDSMAN);
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
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 2) {
                        troops.add(Unit.EMERALD_DRAGON);
                        troops.add(Unit.WATER_ELEMENTAL);
                        troops.add(Unit.STONE_GARGOYLE);
                        troops.add(Unit.BATTLE_BOAR);
                    }
                    if (monsterOverride == MonsterOverride.INCLUDE_ALL || tiers >= 1) {
                        troops.add(Unit.MAGIC_DRAGON);
                        troops.add(Unit.ICE_PHOENIX);
                        troops.add(Unit.MANY_ARMED_GUARDIAN);
                        troops.add(Unit.GORGON_MEDUSA);
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
            case GRIRANA, ELANIN -> {
                if (tiers >= 3) {
                    troops.add(Unit.S1_SWORDSMAN);
                    troops.add(Unit.G1_RANGED);
                    troops.add(Unit.G1_MELEE);
                    troops.add(Unit.G1_MOUNTED);
                }
                if (tiers >= 2) {
                    //troops.add(Unit.S3_SWORDSMAN);
                    troops.add(Unit.G2_RANGED);
                    troops.add(Unit.G2_MELEE);
                    troops.add(Unit.G2_MOUNTED);
                }
                if (tiers >= 1) {
                    troops.add(Unit.G3_RANGED);
                    troops.add(Unit.G3_MELEE);
                    troops.add(Unit.G3_MOUNTED);
                }

                if (tiers >= 1) {
                    troops.add(Unit.EMERALD_DRAGON);
                    troops.add(Unit.WATER_ELEMENTAL);
                    troops.add(Unit.STONE_GARGOYLE);
                    troops.add(Unit.BATTLE_BOAR);
                }

                troops.add(Unit.EPIC_MONSTER_HUNTER_VI);
            }
            default -> throw new RuntimeException("Not implemented for " + player);
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

        return ImageUtil.ocr(croppedImage, ImageUtil.WHITELIST_FOR_USERNAME, ImageUtil.SINGLE_LINE_MODE);
    }

    public static int[] getHeadCount(MyRobot robot, BufferedImage screen) {
        BufferedImage closeButtonImage = ImageUtil.loadResource("leadership/close_button.png");
        Point closeButtonLocation = ImageUtil.search(closeButtonImage, screen, 1380, 300, 250, 250, 0.05)
                .orElseThrow(() -> new RuntimeException("Cannot find the close button"));

        int[] result;
        
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            CompletableFuture<Integer> leadershipF = CompletableFuture.supplyAsync(
                    () -> getHeadCountLimit(robot, screen,
                            Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(562, 839), Point.of(642, 856))),
                    pool);

            CompletableFuture<Integer> dominanceF = CompletableFuture.supplyAsync(
                    () -> getHeadCountLimit(robot, screen,
                            Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(787, 839), Point.of(867, 856))),
                    pool);

            CompletableFuture<Integer> authorityF = CompletableFuture.supplyAsync(
                    () -> getHeadCountLimit(robot, screen,
                            Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(674, 839), Point.of(754, 856))),
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

    private static int getHeadCountLimit(MyRobot robot, BufferedImage screen, Area area) {

        enableDragon(robot, screen);
        
        BufferedImage imageWithText = screen.getSubimage(area.getX(), area.getY(), area.getWidth(), area.getHeight());
        BufferedImage invertedGray = ImageUtil.invertGrayscale(imageWithText);
        BufferedImage linearNormalized = ImageUtil.linearNormalization(invertedGray);
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

    private static void enableDragon(MyRobot robot, BufferedImage screen) {
        Point closeButtonLocation = getCloseButtonLocation(robot, screen);
        Area area = Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(636, 639), Point.of(772, 693));
        BufferedImage sendDragonCheckBox = ImageUtil.loadResource("leadership/send_dragon.png");
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












    private static Point getCloseButtonLocation(MyRobot robot, BufferedImage screen) {
        BufferedImage closeButtonImage = ImageUtil.loadResource("leadership/close_button.png");
        return ImageUtil.search(closeButtonImage, screen, 1380, 300, 250, 250, 0.05)
                .orElseThrow(() -> new RuntimeException("Cannot find the close button"));
    }    
    


    public static void fillTroops(MyRobot robot, List<TroopQuantity> stack) {

        BufferedImage screen = robot.captureScreen();

        enableDragon(robot, screen);

        Point closeButtonLocation = getCloseButtonLocation(robot, screen);

        stack(robot, closeButtonLocation, stack);

        Toolkit.getDefaultToolkit().beep();
    }

    private static void stack(MyRobot robot, Point closeButtonLocation, List<TroopQuantity> stack) {
        BufferedImage screen = robot.captureScreen();

        Area leftPanel = Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(552, 420), Point.of(624, 820));
        // ImageUtil.write(ImageUtil.crop(screen, leftPanel), "left_panel.png");

        Area rightPanel = Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(772, 420), Point.of(844, 820));
        // ImageUtil.write(ImageUtil.crop(screen, rightPanel), "right_panel.png");

        Area scrollBarArea = Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(970, 437), Point.of(986, 802));
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
            
            final int delta = 50;

            if (processed.size() < stack.size()) {
                Point scroolBar = Point.of(closeButtonLocation, Point.of(1438, 356), Point.of(978, 448));
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
        
        Point startMarchButton = Point.of(closeButtonLocation, Point.of(1448, 351), Point.of(899, 898));
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
