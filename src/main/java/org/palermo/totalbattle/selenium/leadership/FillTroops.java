package org.palermo.totalbattle.selenium.leadership;

import com.google.common.collect.ImmutableSet;
import org.palermo.totalbattle.selenium.leadership.model.TroopQuantity;
import org.palermo.totalbattle.selenium.stacking.Attribute;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.util.ImageUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FillTroops {
    
    public enum Enemy {
        BOSS, RARE, COMMON, CONTRACT_COMMON, CONTRACT_RARE
    }
    
    public static Enemy enemyType;
    
    private static final List<EnemyAttribute> ENEMY_DATABASE = new ArrayList<>();
    static {
        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/firehorse_rider_iii.png"))
                .attributes(ImmutableSet.of(Attribute.RANGED)).build());
        
        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/horned_demon_ii.png"))
                .attributes(ImmutableSet.of(Attribute.MOUNTED)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/werewolf_ii.png"))
                .attributes(ImmutableSet.of(Attribute.MOUNTED)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/jaguar_rider_ii.png"))
                .attributes(ImmutableSet.of(Attribute.RANGED)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/fiend.png"))
                .attributes(ImmutableSet.of(Attribute.MOUNTED)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/storm_crow_iv.png"))
                .attributes(ImmutableSet.of(Attribute.MELEE, Attribute.ELEMENTAL)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/axe_thrower_ii.png"))
                .attributes(ImmutableSet.of(Attribute.MELEE, Attribute.FLYING)).build());


        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/wolf_rider_i.png"))
                .attributes(ImmutableSet.of(Attribute.RANGED, Attribute.SIEGE)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/magog_i.png"))
                .attributes(ImmutableSet.of(Attribute.MELEE)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/necromancer_ii.png"))
                .attributes(ImmutableSet.of(Attribute.MELEE)).build());


        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/deathhound_rider_ii.png"))
                .attributes(ImmutableSet.of(Attribute.RANGED)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/dark_rider_iv.png"))
                .attributes(ImmutableSet.of(Attribute.RANGED)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/ghoul.png"))
                .attributes(ImmutableSet.of(Attribute.MOUNTED)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/cerberus_iv.png"))
                .attributes(ImmutableSet.of(Attribute.RANGED, Attribute.ELEMENTAL)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/witch_doctor_i.png"))
                .attributes(ImmutableSet.of(Attribute.MELEE)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/vampire_iv.png"))
                .attributes(ImmutableSet.of(Attribute.MELEE, Attribute.ELEMENTAL)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/skeleton.png"))
                .attributes(ImmutableSet.of(Attribute.MOUNTED)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/goblin.png"))
                .attributes(ImmutableSet.of(Attribute.MOUNTED)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/elven_archer_i.png"))
                .attributes(ImmutableSet.of(Attribute.MELEE)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/dwarf.png"))
                .attributes(ImmutableSet.of(Attribute.MOUNTED)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/runic_guardian_i.png"))
                .attributes(Collections.emptySet()).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/druid_ii.png"))
                .attributes(ImmutableSet.of(Attribute.MELEE)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/banshee_i.png"))
                .attributes(ImmutableSet.of(Attribute.MELEE)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/centaur_iii.png"))
                .attributes(ImmutableSet.of(Attribute.RANGED, Attribute.SIEGE)).build());

        ENEMY_DATABASE.add(EnemyAttribute.builder()
                .icon(ImageUtil.loadResource("enemies/pegasus_rider_iv.png"))
                .attributes(ImmutableSet.of(Attribute.MELEE, Attribute.DRAGON)).build());

    }

    public static void main(String[] args) {
        // Create the frame
        JFrame frame = new JFrame("Total Battle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 150);
        frame.setLayout(null); // Absolute positioning

        // Create the label
        JLabel label = new JLabel("Enemy Type:");
        label.setBounds(30, 20, 80, 25);

        // Create the combo box
        String[] options = {"COMMON", "BOSS", "RARE", "CONTRACT_COMMON", "CONTRACT_RARE"};
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setBounds(120, 20, 180, 25);

        // Create the button
        JButton button = new JButton("Magic");
        button.setBounds(120, 60, 180, 25);

        // Add action listener to the button
        button.addActionListener(new MyActionListener(comboBox));

        // Add components to the frame
        frame.add(label);
        frame.add(comboBox);
        frame.add(button);

        // Show the frame
        frame.setVisible(true);
    }
    
    public static final class MyActionListener implements ActionListener {

        private final JComboBox<String> comboBox;
        
        public MyActionListener(JComboBox<String> comboBox) {
            this.comboBox = comboBox;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String selected = (String) comboBox.getSelectedItem();
            enemyType = Enemy.valueOf(selected);
            FillTroops.fillTroops();
        }
    }
    

    public static void fillTroops() {
        MyRobot robot = MyRobot.INSTANCE;

        BufferedImage screen = robot.captureScreen();

        BufferedImage closeButtonImage = ImageUtil.loadResource("leadership/close_button.png");
        Point closeButtonLocation = ImageUtil.search(closeButtonImage, screen, 1380, 300, 250, 250, 0.05)
                .orElseThrow(() -> new RuntimeException("Cannot find the close button"));
        System.out.println(closeButtonLocation);
        ImageUtil.write(screen, closeButtonLocation, closeButtonImage, "found.png");
        
        
        Area dragonPanel = Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(638, 662), Point.of(767, 693));
        ImageUtil.write(ImageUtil.crop(screen, dragonPanel), "dragon_panel01.png");


        // Activate the window
        robot.mouseMove(Point.of(closeButtonLocation, Point.of(1438, 356), Point.of(1230, 389)));
        robot.activateWindowUnderMouse();

        robot.mouseWheel(Point.of(closeButtonLocation, Point.of(1438, 356), Point.of(739, 617)), -10000);

        enableDragon(robot, closeButtonLocation);
        screen = robot.captureScreen();

        int leadership = getHeadCountLimit(screen, Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(562, 839), Point.of(642, 856)));
        int dominance = getHeadCountLimit(screen, Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(787, 839), Point.of(867, 856)));
        int authority = getHeadCountLimit(screen, Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(674, 839), Point.of(754, 856)));

        log("Got headcounts, leadership: " + leadership + ", dominance: " + dominance + ", authority: " + authority);

        Set<Attribute> exclusions = enemyType == Enemy.BOSS || enemyType == Enemy.RARE || enemyType == Enemy.CONTRACT_RARE ? Collections.emptySet() : 
                getExclusionsFromEnemies(robot, closeButtonLocation);
        
        List<TroopQuantity> stack = buildStack(robot, leadership, dominance, authority, exclusions);
        log("Stack was calculated");
                        
        stack(robot, closeButtonLocation, stack);
        System.out.println("Done!!");
    }

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static void log(String text) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now.format(formatter) + " " + text);
    }
    
    private static void enableDragon(MyRobot robot, Point closeButtonLocation) {
        BufferedImage screen = robot.captureScreen();

        Area area = Area.of(closeButtonLocation, Point.of(1438, 356), Point.of(636, 639), Point.of(772, 693));
        BufferedImage closeButtonImage = ImageUtil.loadResource("leadership/send_dragon.png");
        Point sendDragonLocation = ImageUtil.search(closeButtonImage, screen, area, 0.01).orElse(null);
        if (sendDragonLocation != null) {
            robot.leftClick(sendDragonLocation.move(20, 8));
            System.out.println("Clicked on the dragon");
        }
        else {
            System.out.println("Dragon is not enabled");
        }
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
        
        Set<Integer> processed = new HashSet<>();
        
        int count = 0;
        
        do {
            oldPosition = newPosition;
            
            for (int i = 0; i < stack.size(); i++) {
                if (!processed.contains(i)) {
                    if (processUnit(robot, closeButtonLocation, screen, leftPanel, stack.get(i))) {
                        processed.add(i);
                        continue;
                    }

                    if (processUnit(robot, closeButtonLocation, screen, rightPanel, stack.get(i))) {
                        processed.add(i);
                        continue;
                    }
                }
            }

            if (processed.size() < stack.size()) {
                //robot.mouseWheel(Point.of(closeButtonLocation, Point.of(1438, 356), Point.of(739, 617)), 200);
                //robot.sleep(750);
                Point scroolBar = Point.of(closeButtonLocation, Point.of(1438, 356), Point.of(978, 448));
                scroolBar = scroolBar.move(0, (count * 70));
                robot.mouseDrag(scroolBar, 0, 70);
                robot.sleep(150);
                count = count + 1;
                
                
                screen = robot.captureScreen();
                newPosition = ImageUtil.crop(screen, scrollBarArea);
            }            

        } while ((!ImageUtil.compare(newPosition, oldPosition, 0.01))  && processed.size() < stack.size());
        
        if (processed.size() < stack.size()) {
            for (int i = 0; i < stack.size(); i++) {
                if (!processed.contains(i)) {
                    System.out.println("Missing: " + stack.get(i).getUnit().name());
                }
            }
        }
    }
     
    
    private static int counter = 0;
    private static boolean processUnit(MyRobot robot, Point closeButtonLocation, BufferedImage originalScreen, Area area, TroopQuantity troopQuantity) {
        Point point = ImageUtil.search(troopQuantity.getUnit().getIcon(), originalScreen, area, 0.05).orElse(null);
        if (point == null) {
            return false;
        }
        ImageUtil.write(troopQuantity.getUnit().getIcon(), "original.png");
        ImageUtil.write(originalScreen, point, troopQuantity.getUnit().getIcon(), "found.png");

        robot.leftClick(point.move(135, 44));
        robot.typeString(Integer.toString(troopQuantity.getQuantity()));

        // Click in the header to remove focus from the input field
        //Point header = Point.of(closeButtonLocation, Point.of(1438, 356), Point.of(753, 368));
        //robot.leftClick(header);
        //robot.sleep(150);
        
        Area textArea = Area.of(point, 95, 32, 82, 16);
        BufferedImage currentImage = robot.captureScreen(textArea);
        BufferedImage linearNormalized = ImageUtil.linearNormalization(currentImage);
        BufferedImage croppedImage = ImageUtil.cropText(linearNormalized);
        //ImageUtil.write(croppedImage, "set" + (counter++) + ".png");
        String numberAsText = ImageUtil.ocr(croppedImage, ImageUtil.WHITELIST_FOR_NUMBERS, ImageUtil.SINGLE_LINE_MODE);
        numberAsText = numberAsText.replaceAll(",", "").replaceAll(" ", "");
        if (numberAsText.isEmpty()) {
            System.out.println("Fail to parse quantity for : " + troopQuantity.getUnit().name() + " expected " + troopQuantity.getQuantity());
            return true;
        }
        int valueAssigned = Integer.parseInt(numberAsText);
        if (valueAssigned != troopQuantity.getQuantity()) {
            //throw new RuntimeException(troopQuantity.getUnit().name() + " expected " + troopQuantity.getQuantity() + " but found " + valueAssigned);
            System.out.println("Missing: " + troopQuantity.getUnit().name() + " expected " + troopQuantity.getQuantity() + " but found " + valueAssigned + " diff: " + (troopQuantity.getQuantity() - valueAssigned));
        }
            
        return true;
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
            return showLeadershipDialog();
        }
        return Integer.parseInt(leadershipText.substring(slashIndex + 1).trim());
    }

    private static int showLeadershipDialog() {
        JTextField numberField = new JTextField(10);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Leadership"));
        panel.add(numberField);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Enter a Number",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String input = numberField.getText();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered", e);
            }
        } else {
            throw new RuntimeException("Dialog canceled");
        }
    }
    
    public static List<TroopQuantity> buildStack(MyRobot myRobot, int leadership, int dominance, int authority, Set<Attribute> exclusions) {
        
        List<Unit> inputList = new ArrayList<>();


        if (getUsername(myRobot).equals("Grirana") || getUsername(myRobot).equals("Elanin")) {
            if (enemyType == Enemy.BOSS) {
                inputList.add(Unit.S1_SWORDSMAN);
            }
            inputList.add(Unit.G1_RANGED);
            inputList.add(Unit.G1_MELEE);
            inputList.add(Unit.G1_MOUNTED);

            inputList.add(Unit.G2_RANGED);
            inputList.add(Unit.G2_MELEE);
            inputList.add(Unit.G2_MOUNTED);
            
            inputList.add(Unit.G3_MOUNTED);
            if (getUsername(myRobot).equals("Grirana")) {
                inputList.add(Unit.G3_MELEE);
                inputList.add(Unit.G3_MOUNTED);
            }
            
            inputList.add(Unit.EPIC_MONSTER_HUNTER_V);
            inputList.add(Unit.SWIFT_MARKSMAN);
        }
        else {
            if (enemyType == Enemy.BOSS) {
                if (getUsername(myRobot).equals("Palermo")) {
                    inputList.add(Unit.S3_SWORDSMAN);
                    inputList.add(Unit.G3_RANGED);
                    inputList.add(Unit.G3_MELEE);
                    inputList.add(Unit.G3_MOUNTED);

                } else {
                    inputList.add(Unit.S2_SWORDSMAN);
                    inputList.add(Unit.G2_RANGED);
                    inputList.add(Unit.G2_MELEE);
                    inputList.add(Unit.G2_MOUNTED);

                }
            }
            
            if (!getUsername(myRobot).equals("Palermo")) {
                inputList.add(Unit.G3_RANGED);
                inputList.add(Unit.G3_MELEE);
                inputList.add(Unit.G3_MOUNTED);
            }

            inputList.add(Unit.G4_RANGED);
            inputList.add(Unit.G4_MELEE);
            inputList.add(Unit.G4_MOUNTED);
            
            if (getUsername(myRobot).equals("Palermo")) {
                inputList.add(Unit.G5_RANGED);
                inputList.add(Unit.G5_MELEE);
                inputList.add(Unit.G5_MOUNTED);
                inputList.add(Unit.G5_GRIFFIN);
            }

            if (enemyType == Enemy.BOSS || enemyType == Enemy.CONTRACT_COMMON || enemyType == Enemy.CONTRACT_RARE) {
                inputList.add(Unit.STONE_GARGOYLE);
                inputList.add(Unit.WATER_ELEMENTAL);
                inputList.add(Unit.EMERALD_DRAGON);
                inputList.add(Unit.BATTLE_BOAR);

                inputList.add(Unit.MAGIC_DRAGON);
                inputList.add(Unit.ICE_PHOENIX);
                inputList.add(Unit.MANY_ARMED_GUARDIAN);
                inputList.add(Unit.GORGON_MEDUSA);

                if (getUsername(myRobot).equals("Palermo")) {
                    inputList.add(Unit.DESERT_VANQUISER);
                }

                inputList.add(Unit.EPIC_MONSTER_HUNTER_VII);
                inputList.add(Unit.EPIC_MONSTER_HUNTER_VI);
                inputList.add(Unit.ARBALESTER_VI);
                inputList.add(Unit.LEGIONARY_VI);
                inputList.add(Unit.CHARIOT_VI);
                inputList.add(Unit.SPHYNX_VI);
            }
            
        }
        
        
        ConfigurationBuilder configurationBuilder = Configuration.builder()
                .leadership(leadership)
                .dominance(dominance)
                .authority(authority);

        List<TroopQuantity> outputList = new ArrayList<>();

        for (Unit unit : inputList) {
            if (!unit.wasExcluded(exclusions)) {
                configurationBuilder.addUnit(unit);
                outputList.add(TroopQuantity.builder().unit(unit).quantity(0).build());
            }
        }

        int[] troops = configurationBuilder.build().resolve();
        
        for (int i = 0; i < troops.length; i++) {
            TroopQuantity troopQuantity = outputList.get(i);
            outputList.set(i, troopQuantity.withQuantity(troops[i]));
        }
        
        return outputList;
    }
    
    private static Set<Attribute> getExclusionsFromEnemies(MyRobot robot, Point closeButtonLocation) {
        
        Set<Attribute> attributes = new HashSet<>();

        Point enemyPoint = Point.of(closeButtonLocation, Point.of(1438, 356), Point.of(1032, 456));
        attributes.addAll(matchEnemy(robot, enemyPoint));
        
        enemyPoint = Point.of(closeButtonLocation, Point.of(1438, 356), Point.of(1142, 456));
        attributes.addAll(matchEnemy(robot, enemyPoint));
        
        return attributes;
    }

    private static Set<Attribute> matchEnemy(MyRobot robot, Point enemyPoint) {
        Area enemyIconArea = Area.of(enemyPoint, 0, 0, 72, 54);
        BufferedImage enemyIcon = robot.captureScreen(enemyIconArea);
        for (EnemyAttribute enemyAttribute : ENEMY_DATABASE) {
            if (ImageUtil.compare(enemyAttribute.getIcon(), enemyIcon, 0.05)) {
                return enemyAttribute.getAttributes();
            }
        }
        
        if (ImageUtil.compare(ImageUtil.loadResource("enemies/empty01.png"), enemyIcon, 0.05)) {
            return Collections.emptySet();    
        }
        
        ImageUtil.write(enemyIcon, "unknown_enemy.png");
        throw new RuntimeException("Unknown enemy");
    }
    
    private static String getUsername(MyRobot robot) {

        BufferedImage screen = robot.captureScreen();
        
        BufferedImage woodReference = ImageUtil.loadResource("leadership/wood.png");
        Point rootReferencePoint;
        while ((rootReferencePoint = ImageUtil.search(woodReference, screen, Area.of(673, 162, 173, 113), 0.02).orElse(null)) == null) {
            BufferedImage nextButton = ImageUtil.loadResource("leadership/next_title.png");
            Point nextButtonPoint = ImageUtil.search(nextButton, screen, Area.of(1320, 198, 35, 35), 0.05).orElse(null);
            if (nextButtonPoint == null) {
                throw new RuntimeException("Cannot find the next button");
            }
            robot.leftClick(nextButtonPoint.move(5, 5));
            robot.mouseMove(nextButtonPoint.move(50, 50));
            screen = robot.captureScreen();
            // throw new RuntimeException("Cannot find wood reference");
        }
        System.out.println("Wood: " + rootReferencePoint);
        Area nameArea = Area.of(rootReferencePoint, Point.of(750, 199), Point.of(751, 231), Point.of(868, 248));
        
        BufferedImage imageWithText = ImageUtil.crop(screen, nameArea);
        BufferedImage invertedGray = ImageUtil.toGrayscale(imageWithText);
        BufferedImage linearNormalized = ImageUtil.linearNormalization(invertedGray);
        BufferedImage croppedImage = ImageUtil.cropText(linearNormalized);
        ImageUtil.write(croppedImage, "name.png");

        return ImageUtil.ocr(croppedImage, ImageUtil.WHITELIST_FOR_USERNAME, ImageUtil.SINGLE_LINE_MODE);
    }
}
