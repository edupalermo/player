package org.palermo.totalbattle.selenium.leadership;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.palermo.totalbattle.selenium.leadership.model.CitadelQueryResult;
import org.palermo.totalbattle.selenium.leadership.model.EnemyRarity;
import org.palermo.totalbattle.selenium.leadership.model.EnemyType;
import org.palermo.totalbattle.selenium.leadership.model.Exclusion;
import org.palermo.totalbattle.selenium.leadership.model.TroopQuantity;
import org.palermo.totalbattle.selenium.stacking.Attribute;
import org.palermo.totalbattle.selenium.stacking.Pool;
import org.palermo.totalbattle.selenium.stacking.Unit;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TroopManagerApp extends JFrame {

    private static final Sheets service = buildSheetsService();
    private static final String SPREAD_SHEET_ID = "1egdLR8A1-hXZDr0xssNb3-UZx8iwJHr9xqj1KaV6Ibo";    

    private static MyRobot robot = MyRobot.INSTANCE;

    // ===== Section: Player & attributes =====
    private JComboBox<String> playerCombo;
    private JFormattedTextField leadershipField;
    private JFormattedTextField dominanceField;
    private JFormattedTextField authorityField;

    // ===== Layers =====
    private JComboBox<Integer> layersCombo;

    // ===== Limit =====
    private JComboBox<String> limitCombo;
    private JComboBox<String> waveCombo;
    private JComboBox<Backend.MonsterOverride> monsterOverride;
    private JComboBox<String> targetRarity;
    private JComboBox<String> targetType;
    private JComboBox<String> targetLevel;
    
    // ===== Exclusions =====
    private JCheckBox cbRanged, cbMelee, cbMounted, cbElemental, cbFlying, cbDragon, cbGiant, cbBeast, cbSpecialist;

    // ===== Buttons =====
    private JButton btnRetrieve;
    private JButton btnGenerate;
    private JButton btnAssign;
    private JButton btnCpRun;
    private JButton btnCpRunBig;
    private JButton btnClear;
    private JButton btnClearExclusions;

    // ===== Table =====
    private JTable table;
    private TroopTableModel tableModel;
    private JPanel tableContainer; // holds the table and Clear button

    private JScrollPane scrollPane; // for vertical scrolling
    private JPanel content;         // main content inside the scroll pane
    
    private java.util.List<TroopQuantity> troopQuantityList;

    public TroopManagerApp() {
        super("TroopManagerApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== Row: Player label + combo on the right =====
        content.add(buildPlayerRow());
        content.add(Box.createVerticalStrut(10));

        // ===== Row: Leadership / Dominance / Authority (side-by-side with titled borders) =====
        content.add(buildAttributesRow());
        content.add(Box.createVerticalStrut(10));

        // ===== Row: Retrieve button (alone on its own line) =====
        btnRetrieve = new JButton("Retrieve");
        JPanel retrieveRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        retrieveRow.add(btnRetrieve);
        content.add(retrieveRow);
        content.add(Box.createVerticalStrut(10));

        // ===== Row: Layers label + dropdown =====
        content.add(buildLayersRow());
        content.add(Box.createVerticalStrut(10));

        // ===== Exclusion section =====
        content.add(buildExclusionSection());
        content.add(Box.createVerticalStrut(10));

        // ===== Action buttons: Generate Stack / Assign troops =====
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnGenerate = new JButton("Generate Stack");
        btnCpRun   = new JButton("CP run");
        btnCpRunBig   = new JButton("CP run Big List");
        btnAssign   = new JButton("Assign troops");
        buttonsRow.add(btnGenerate);
        buttonsRow.add(btnCpRun);
        buttonsRow.add(btnCpRunBig);
        buttonsRow.add(btnAssign);
        content.add(buttonsRow);
        content.add(Box.createVerticalStrut(8));

        // ===== Table (editable) + Clear button, hidden by default =====
        tableModel = new TroopTableModel();
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        // Renderers for thousand separators on numeric columns
        DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
            private final NumberFormat fmt = new DecimalFormat("#,###");
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number) {
                    super.setValue(fmt.format(((Number) value).longValue()));
                } else {
                    super.setValue(value == null ? "" : value.toString());
                }
            }
        };
        numberRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        
        table.getColumnModel().getColumn(1).setCellRenderer(numberRenderer); // Quantity
        table.getColumnModel().getColumn(2).setCellRenderer(numberRenderer); // Health
        table.getColumnModel().getColumn(3).setCellRenderer(numberRenderer); // Total

        // Editors for integer-or-empty with thousand separators
        table.getColumnModel().getColumn(1).setCellEditor(nullableIntegerEditor());
        table.getColumnModel().getColumn(2).setCellEditor(nullableIntegerEditor());

        btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> {
            troopQuantityList = null;
            tableModel.clear();
            setTableVisible(false);
        });
        JPanel clearBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearBar.add(btnClear);

        tableContainer = new JPanel(new BorderLayout(5, 5));
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        tableContainer.add(clearBar, BorderLayout.SOUTH);
        tableContainer.setVisible(false); // start hidden

        content.add(tableContainer);

        // ===== Scroll the whole content if it exceeds the window =====
        scrollPane = new JScrollPane(content,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        btnCpRunBig.addActionListener(e -> {
            troopQuantityList = null;
            tableModel.clear();

            java.util.List<Object[]> lines = new ArrayList<>();
            troopQuantityList =  new ArrayList<>();

            addUnit(lines, troopQuantityList, Unit.S5_SWORDSMAN, 74000);
            addUnit(lines, troopQuantityList, Unit.S5_DEADSHOT, 74000);
            addUnit(lines, troopQuantityList, Unit.S5_VULTURE, 74000);
            addUnit(lines, troopQuantityList, Unit.S5_LION_RIDER, 37000);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.G5_MELEE, 74000);
            addUnit(lines, troopQuantityList, Unit.G5_RANGED, 74000);
            addUnit(lines, troopQuantityList, Unit.G5_MOUNTED, 37000);
            addUnit(lines, troopQuantityList, Unit.G5_GRIFFIN, 3700);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.S6_MELEE, 42000);
            addUnit(lines, troopQuantityList, Unit.S6_RANGED, 42000);
            addUnit(lines, troopQuantityList, Unit.S6_FLYING, 42000);
            addUnit(lines, troopQuantityList, Unit.S6_MOUNTED, 21000);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.G6_MELEE, 42000);
            addUnit(lines, troopQuantityList, Unit.G6_RANGED, 42000);
            addUnit(lines, troopQuantityList, Unit.G6_MOUNTED, 21000);
            addUnit(lines, troopQuantityList, Unit.G6_GRIFFIN, 2100);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.G7_MELEE, 24000);
            addUnit(lines, troopQuantityList, Unit.G7_RANGED, 24000);
            addUnit(lines, troopQuantityList, Unit.G7_MOUNTED, 12000);
            addUnit(lines, troopQuantityList, Unit.G7_GRIFFIN, 1200);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.ELEMENTAL_V, 700);
            addUnit(lines, troopQuantityList, Unit.DRAGON_V, 700);
            addUnit(lines, troopQuantityList, Unit.GIANT_V, 700);
            addUnit(lines, troopQuantityList, Unit.BEAST_V, 700);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.ELEMENTAL_VI, 300);
            addUnit(lines, troopQuantityList, Unit.DRAGON_VI, 300);
            addUnit(lines, troopQuantityList, Unit.GIANT_VI, 300);
            addUnit(lines, troopQuantityList, Unit.BEAST_VI, 300);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.ELEMENTAL_VII, 140);
            addUnit(lines, troopQuantityList, Unit.DRAGON_VII, 140);
            addUnit(lines, troopQuantityList, Unit.GIANT_VII, 140);
            addUnit(lines, troopQuantityList, Unit.BEAST_VII, 140);

            tableModel.setData(lines.toArray(new Object[lines.size()][]));

            setTableVisible(true);
        });
        
        btnCpRun.addActionListener(e -> {
            troopQuantityList = null;
            tableModel.clear();

            java.util.List<Object[]> lines = new ArrayList<>();
            troopQuantityList =  new ArrayList<>();

            addUnit(lines, troopQuantityList, Unit.G4_MELEE, 14943);
            addUnit(lines, troopQuantityList, Unit.G4_RANGED, 14943);
            addUnit(lines, troopQuantityList, Unit.G4_MOUNTED, 7471);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.S5_SWORDSMAN, 8333);
            addUnit(lines, troopQuantityList, Unit.S5_DEADSHOT, 8333);
            addUnit(lines, troopQuantityList, Unit.S5_VULTURE, 8333);
            addUnit(lines, troopQuantityList, Unit.S5_LION_RIDER, 4127);
            lines.add(new Object[] { "", "", "", ""});
            
            addUnit(lines, troopQuantityList, Unit.G5_MELEE, 8333);
            addUnit(lines, troopQuantityList, Unit.G5_RANGED, 8333);
            addUnit(lines, troopQuantityList, Unit.G5_MOUNTED, 4127);
            addUnit(lines, troopQuantityList, Unit.G5_GRIFFIN, 433);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.S6_MELEE, 4610);
            addUnit(lines, troopQuantityList, Unit.S6_RANGED, 4610);
            addUnit(lines, troopQuantityList, Unit.S6_FLYING, 4610);
            addUnit(lines, troopQuantityList, Unit.S6_MOUNTED, 2281);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.G6_MELEE, 4610);
            addUnit(lines, troopQuantityList, Unit.G6_RANGED, 4610);
            addUnit(lines, troopQuantityList, Unit.G6_MOUNTED, 2281);
            addUnit(lines, troopQuantityList, Unit.G6_GRIFFIN, 228);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.G7_MELEE, 2549);
            addUnit(lines, troopQuantityList, Unit.G7_RANGED, 2549);
            addUnit(lines, troopQuantityList, Unit.G7_MOUNTED, 1275);
            addUnit(lines, troopQuantityList, Unit.G7_GRIFFIN, 127);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.ELEMENTAL_V, 92);
            addUnit(lines, troopQuantityList, Unit.DRAGON_V, 92);
            addUnit(lines, troopQuantityList, Unit.GIANT_V, 92);
            addUnit(lines, troopQuantityList, Unit.BEAST_V, 92);
            lines.add(new Object[] { "", "", "", ""});
            
            addUnit(lines, troopQuantityList, Unit.ELEMENTAL_VI, 34);
            addUnit(lines, troopQuantityList, Unit.DRAGON_VI, 34);
            addUnit(lines, troopQuantityList, Unit.GIANT_VI, 34);
            addUnit(lines, troopQuantityList, Unit.BEAST_VI, 34);
            lines.add(new Object[] { "", "", "", ""});

            addUnit(lines, troopQuantityList, Unit.ELEMENTAL_VII, 14);
            addUnit(lines, troopQuantityList, Unit.DRAGON_VII, 14);
            addUnit(lines, troopQuantityList, Unit.GIANT_VII, 14);
            addUnit(lines, troopQuantityList, Unit.BEAST_VII, 14);

            tableModel.setData(lines.toArray(new Object[lines.size()][]));

            setTableVisible(true);
        });
        
        // ===== Actions =====
        btnGenerate.addActionListener(e -> {

            CitadelQueryResult citadelQueryResult = null;

            if (targetRarity.getSelectedItem().toString().equals("Citadel")) {
                citadelQueryResult = querySheet(getSelectedPlayerName(), getCitadelSelected()).orElse(null);
                if (citadelQueryResult == null) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Player / Citadel not in the sheet",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
            
            java.util.List<Unit> units = Backend.getUnits(getSelectedPlayerName(), 
                    getSelectedExclusions(), 
                    getSelectedLayers(),
                    (Backend.MonsterOverride) monsterOverride.getSelectedItem());
            
            units = limitUnits(units); // Limit to one or two for attaking common or rare monsters

            int[] informedHeadCount =  getInformedHeadCount();
            
            if (citadelQueryResult != null) {
                informedHeadCount[0] = informedHeadCount[0] - (citadelQueryResult.getUnit().getHeadCount() * citadelQueryResult.getQtd());
            }
            
            int[] quantities = Backend.getUnitQuantity(informedHeadCount, units);

            if (citadelQueryResult != null) {
                quantities = append(quantities, citadelQueryResult.getQtd());
                units.add(citadelQueryResult.getUnit());
            }


            java.util.List<Object[]> lines = new ArrayList<>();
            for (int i = 0; i < quantities.length; i++) {
                
                if (i > 0) {
                    // Pula linha para facilitar leitura
                    if (units.get(i - 1).getTier() != units.get(i).getTier() ||units.get(i - 1).getPool() != units.get(i).getPool()) {
                        lines.add(new Object[] { "", "", "", ""});
                    }
                    
                }
                Unit unit = units.get(i);
                lines.add(new Object[] { unit.name(), quantities[i], unit.getHealth(), computeWaves(quantities[i], getSelectedWaves())});
            }
            
            troopQuantityList = createTroopQuantityList(quantities, units);
            tableModel.setData(lines.toArray(new Object[lines.size()][]));
            
            setTableVisible(true);

            // Ensure the newly revealed table is visible by scrolling it into view
            SwingUtilities.invokeLater(() -> {
                Rectangle r = tableContainer.getBounds();
                scrollPane.getViewport().scrollRectToVisible(r);
            });
        });

        btnAssign.addActionListener(e -> {
            if (troopQuantityList == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Click on Generate Stack first!",
                        "Assign troops",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
            
            Backend.fillTroops(robot, troopQuantityList);
        });

        btnRetrieve.addActionListener(e -> {

            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            
            BufferedImage screen = robot.captureScreen();
            
            // Set Player Name            
            String playerName = Backend.getPlayerName(screen);
            for (int i = 0; i < playerCombo.getItemCount(); i++) {
                String item = playerCombo.getItemAt(i);
                if (playerName.equalsIgnoreCase(item)) {
                    playerCombo.setSelectedIndex(i);
                }
            }
            
            //Set Head Count
            int[] headCount = Backend.getHeadCount();

            leadershipField.setValue(headCount[0]);
            dominanceField.setValue(headCount[1]);
            authorityField.setValue(headCount[2]);
            
            robot.mouseMove(Point.of(pointerInfo.getLocation().x, pointerInfo.getLocation().y));
        });

        setPreferredSize(new Dimension(820, 640));
        pack();
        setLocationRelativeTo(null);
    }
    
    private int[] append(int[] array, int toBeAdded) {
        int[] response = Arrays.copyOf(array, array.length + 1);
        response[response.length - 1] = toBeAdded;
        return response;
    }

    private String getCitadelSelected() {
        StringBuilder sb = new StringBuilder();

        if (targetType.getSelectedItem().toString().equals("Elves")) {
            sb.append("Elven Citadel");
        }
        else {
            throw new RuntimeException("Error!");
        }
        sb.append(" ");
        sb.append(targetLevel.getSelectedItem().toString());
        return sb.toString();
    }

    private Optional<CitadelQueryResult> querySheet(String playerName, String citadelType) {
        try {
            ValueRange valueRange = service.spreadsheets().values()
                    .get(SPREAD_SHEET_ID, "Citadel!A1:D10")
                    .execute();

            java.util.List<java.util.List<Object>> rows = valueRange.getValues();

            for (List<Object> row : rows) {
                if (row.get(0).toString().equals(playerName) &&
                        row.get(1).toString().equals(citadelType)) {
                    return Optional.of(CitadelQueryResult.builder()
                                    .qtd(Integer.parseInt(row.get(3).toString()))
                                    .unit(Unit.valueOf(row.get(2).toString()))
                            .build());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private static Sheets buildSheetsService() {
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream("/home/eduardo/tokens/credentials.json"))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Troop Manager")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void addUnit(java.util.List<Object[]> lines, java.util.List<TroopQuantity> troopQuantityList, Unit unit, int quantity) {
        lines.add(new Object[] { unit.name(), quantity, unit.getHealth(), quantity});
        TroopQuantity troopQuantity = TroopQuantity.builder().unit(unit).quantity(quantity).build();
        troopQuantityList.add(troopQuantity);
    }
    
    private int computeWaves(int quantity, int wave) {
        double factor = 0;

        for (int i = 0; i < wave; i++) {
            factor += Math.pow(1.06, i);
        }

        return (int) Math.round(quantity * factor);
    }
    
    private java.util.List<TroopQuantity> createTroopQuantityList(int[] quantities, java.util.List<Unit> units) {
        java.util.List<TroopQuantity> answer = new ArrayList<>();
        for (int i = 0; i < quantities.length; i++) {
            answer.add(TroopQuantity.builder().quantity(quantities[i]).unit(units.get(i)).build());            
        }
        return answer;
    }

    private int getSelectedWaves() {
        return Integer.parseInt(waveCombo.getItemAt(waveCombo.getSelectedIndex()));
    }
    
    private String getSelectedPlayerName() {
        return playerCombo.getItemAt(playerCombo.getSelectedIndex());
    }
    
    private int getSelectedLayers() {
        return layersCombo.getItemAt(layersCombo.getSelectedIndex());
    }
    
    private Set<Attribute> getSelectedExclusions() {
        Set<Attribute> exclusions = new HashSet<>();
        if (cbRanged.isSelected()) {
            exclusions.add(Attribute.RANGED);
        }
        if (cbMelee.isSelected()) {
            exclusions.add(Attribute.MELEE);
        }
        if (cbMounted.isSelected()) {
            exclusions.add(Attribute.MOUNTED);
        }
        if (cbElemental.isSelected()) {
            exclusions.add(Attribute.ELEMENTAL);
        }
        if (cbFlying.isSelected()) {
            exclusions.add(Attribute.FLYING);
        }
        if (cbDragon.isSelected()) {
            exclusions.add(Attribute.DRAGON);
        }
        if (cbGiant.isSelected()) {
            exclusions.add(Attribute.GIANT);
        }
        if (cbBeast.isSelected()) {
            exclusions.add(Attribute.BEAST);
        }
        if (cbSpecialist.isSelected()) {
            exclusions.add(Attribute.SPECIALIST);
        }
        return exclusions;        
    }
    
    private int getSelectedLimit() {
        String value = limitCombo.getItemAt(limitCombo.getSelectedIndex());
        
        if (value.equalsIgnoreCase("no")) {
            return 100000; // No limit
        }
        String[] parts = value.split(" "); 
        return Integer.parseInt(parts[0]);
    }
    
    private java.util.List<Unit> limitUnits(java.util.List<Unit> input) {
        java.util.List<Unit> output = new ArrayList<>();
        
        int countLeadership = 0;
        int limit = getSelectedLimit();
        
        int totalLeadership = (int) input.stream().filter((it) -> it.getPool() == Pool.LEADERSHIP).count();
        
        for (Unit unit : input) {
            if (unit.getPool() == Pool.LEADERSHIP) {
                if (countLeadership >= totalLeadership - limit) {
                    output.add(unit);
                }
                countLeadership++;
            }
            else {
                output.add(unit);
            }
        }
        
        return output;
    }
    
    private int[] getInformedHeadCount() {
        int leadership = toInteger(leadershipField.getValue());
        int dominance = toInteger(dominanceField.getValue());
        int authority = toInteger(authorityField.getValue());
        
        return new int[] {leadership, dominance, authority};
    }
    
    private int toInteger(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Long) {
            return (int) ((Long) obj).longValue();
        }
        else {
            throw new RuntimeException("Cannot convert to int " + obj.getClass().getName());
        }
    }

    private static String nullableText(Object value) {
        if (value == null) return "(empty)";
        if (value instanceof Number) {
            return String.format("%,d", ((Number) value).longValue());
        }
        return value.toString();
    }


    private JPanel buildPlayerRow() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Label on the left
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        panel.add(new JLabel("Player"), gc);

        // Combo on the right (same row)
        playerCombo = new JComboBox<>(new String[]{"Palermo", "Peter", "Mightshaper", "Grirana", "Elanin", "Lorven"});
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1.0;
        panel.add(playerCombo, gc);

        return panel;
    }

    private JPanel buildAttributesRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 8, 0));

        leadershipField = nullableIntegerField();
        dominanceField  = nullableIntegerField();
        authorityField  = nullableIntegerField();

        row.add(wrapTitled("Leadership", leadershipField));
        row.add(wrapTitled("Dominance",  dominanceField));
        row.add(wrapTitled("Authority",  authorityField));

        return row;
    }

    private JPanel buildLayersRow() {
        JPanel row = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.anchor = GridBagConstraints.WEST;

        // Row 0: Layers
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        row.add(new JLabel("Layers"), gc);

        layersCombo = new JComboBox<>(new Integer[]{1, 2, 3});
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1.0; gc.fill = GridBagConstraints.HORIZONTAL;
        row.add(layersCombo, gc);

        // Row 1: Limit
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        row.add(new JLabel("Match Enemy"), gc);

        limitCombo = new JComboBox<>(new String[]{
                "No",
                "1 Enemy",
                "2 Enemies",
                "3 Enemies"
        });
        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1.0; gc.fill = GridBagConstraints.HORIZONTAL;
        row.add(limitCombo, gc);

        // Row 2: Wave
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        row.add(new JLabel("Waves"), gc);

        waveCombo = new JComboBox<>(new String[]{ "1", "2", "3", "4","5" });
        gc.gridx = 1; gc.gridy = 2; gc.weightx = 1.0; gc.fill = GridBagConstraints.HORIZONTAL;
        row.add(waveCombo, gc);

        // Row 3: Monster Override
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        row.add(new JLabel("Monsters"), gc);

        monsterOverride = new JComboBox<>(Backend.MonsterOverride.values());
        gc.gridx = 1; gc.gridy = 3; gc.weightx = 1.0; gc.fill = GridBagConstraints.HORIZONTAL;
        row.add(monsterOverride, gc);

        return row;
    }

    private JPanel buildExclusionSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Exclusion",
                TitledBorder.LEFT, TitledBorder.TOP));


        ActionListener commonActionListener = changeTargetConfiguration();
        
        // --- Top row ---
        JLabel lblTarget = new JLabel("Target:");
        targetRarity = new JComboBox<>(new String[]{
                "Undefined", "Common", "Rare", "Citadel"
        });
        targetRarity.addActionListener(commonActionListener);
        targetType = new JComboBox<>(new String[]{
                "Undefined", "Barbarian", "Inferno", "Undead", "Elves", "Cursed"
        });
        targetType.addActionListener(commonActionListener);

        targetLevel = new JComboBox<>(new String[]{
                "Undefined", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"
        });
        targetLevel.addActionListener(commonActionListener);
        
        btnClearExclusions = new JButton("Clear");
        btnClearExclusions.addActionListener(e -> {
            cbRanged.setSelected(false);
            cbMelee.setSelected(false);
            cbMounted.setSelected(false);
            cbElemental.setSelected(false);
            cbFlying.setSelected(false);
            cbDragon.setSelected(false);
            cbGiant.setSelected(false);
            cbBeast.setSelected(false);
            cbSpecialist.setSelected(false);

            targetRarity.setSelectedIndex(0);
            targetType.setSelectedIndex(0);
            targetLevel.setSelectedIndex(0);
            
        });

        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetPanel.add(lblTarget);
        targetPanel.add(targetRarity);
        targetPanel.add(targetType);
        targetPanel.add(targetLevel);
        targetPanel.add(btnClearExclusions);

        panel.add(targetPanel, BorderLayout.NORTH);

        // --- Checkbox grid ---
        JPanel grid = new JPanel(new GridLayout(0, 3, 8, 4));

        cbRanged    = new JCheckBox("Ranged");
        cbMelee     = new JCheckBox("Melee");
        cbMounted   = new JCheckBox("Mounted");
        cbElemental = new JCheckBox("Elemental");
        cbFlying    = new JCheckBox("Flying");
        cbDragon    = new JCheckBox("Dragon");
        cbGiant     = new JCheckBox("Giant");
        cbBeast     = new JCheckBox("Beast");
        cbSpecialist= new JCheckBox("Specialist");

        grid.add(cbRanged);
        grid.add(cbMelee);
        grid.add(cbMounted);
        grid.add(cbDragon);
        grid.add(cbElemental);
        grid.add(cbGiant);
        grid.add(cbBeast);
        grid.add(cbFlying);
        grid.add(cbSpecialist);

        panel.add(grid, BorderLayout.CENTER);

        return panel;
    }

/*
    targetRarity = new JComboBox<>(new String[]{
        "Undefined", "Common", "Rare"
    });
    targetType = new JComboBox<>(new String[]{
        "Undefined", "Barbarian", "Inferno", "Undead", "Elves", "Cursed"
    });
    targetLevel = new JComboBox<>(new String[]{
    */

        private ActionListener changeTargetConfiguration() {
        return actionEvent -> {
            if ((targetRarity.getSelectedIndex() == 0) || (targetType.getSelectedIndex() == 0)  || (targetLevel.getSelectedIndex() == 0)) {
                cbRanged.setSelected(false);
                cbMelee.setSelected(false);
                cbMounted.setSelected(false);
                cbElemental.setSelected(false);
                cbFlying.setSelected(false);
                cbDragon.setSelected(false);
                cbGiant.setSelected(false);
                cbBeast.setSelected(false);
                cbSpecialist.setSelected(false);
                return;
            }

            EnemyRarity rarity = EnemyRarity.fromString((String) targetRarity.getSelectedItem());
            EnemyType type = EnemyType.fromString((String) targetType.getSelectedItem());
            int level = Integer.parseInt((String) targetLevel.getSelectedItem());
            
            Exclusion exclusion = ExclusionDatabase.resolve(rarity, type, level).orElse(null);
            
            if (exclusion == null) {
                JOptionPane.showMessageDialog(
                        null,
                        "Enemy not recorded",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            cbRanged.setSelected(exclusion.isRanged());
            cbMelee.setSelected(exclusion.isMelee());
            cbMounted.setSelected(exclusion.isMounted());
            cbDragon.setSelected(exclusion.isDragon());
            cbElemental.setSelected(exclusion.isElemental());
            cbGiant.setSelected(exclusion.isGiant());
            cbBeast.setSelected(exclusion.isBeast());
            cbFlying.setSelected(exclusion.isFlying());            
        };
    }

    private void setTableVisible(boolean visible) {
        tableContainer.setVisible(visible);
        content.revalidate();
        content.repaint();
    }

    private static JPanel wrapTitled(String title, JComponent inner) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title,
                TitledBorder.LEFT, TitledBorder.TOP));
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    // ===== Nullable integer field with thousand separators =====
    private static JFormattedTextField nullableIntegerField() {
        NumberFormat fmt = new DecimalFormat("#,###");
        NullableIntegerFormatter nf = new NullableIntegerFormatter(fmt);
        nf.setValueClass(Long.class);
        nf.setAllowsInvalid(true);        // allow user to clear to empty
        nf.setCommitsOnValidEdit(true);
        JFormattedTextField f = new JFormattedTextField(nf);
        f.setColumns(10);
        f.setValue(null);                 // start empty
        f.setHorizontalAlignment(JTextField.RIGHT);
        return f;
    }

    // Editor for table numeric cells (nullable integers)
    private static DefaultCellEditor nullableIntegerEditor() {
        JFormattedTextField field = nullableIntegerField();
        DefaultCellEditor editor = new DefaultCellEditor(field) {
            @Override
            public Object getCellEditorValue() {
                Object v = ((JFormattedTextField) getComponent()).getValue();
                if (v == null) return null;
                if (v instanceof Number) return ((Number) v).longValue();
                try {
                    String s = v.toString().replace(",", "").trim();
                    if (s.isEmpty()) return null;
                    return Long.parseLong(s);
                } catch (Exception e) {
                    return null;
                }
            }
        };
        editor.setClickCountToStart(1);
        return editor;
    }

    // NumberFormatter that allows empty string -> null
    static class NullableIntegerFormatter extends NumberFormatter {
        public NullableIntegerFormatter(NumberFormat format) { super(format); }
        @Override
        public Object stringToValue(String text) throws ParseException {
            if (text == null) return null;
            String t = text.replace(",", "").trim();
            if (t.isEmpty()) return null;
            return super.stringToValue(text);
        }
    }

    // ===== Table Model =====
    static class TroopTableModel extends AbstractTableModel {
        private final String[] cols = {"Troop", "Quantity", "Health", "Total"};
        private Object[][] data = new Object[0][4];

        public void setData(Object[][] newData) {
            this.data = newData;
            fireTableDataChanged();
        }

        public void clear() {
            this.data = new Object[0][4];
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return data.length; }

        @Override
        public int getColumnCount() { return cols.length; }

        @Override
        public String getColumnName(int column) { return cols[column]; }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> String.class;
                default -> Long.class; // we use Long for numbers; may be null
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                data[rowIndex][columnIndex] = aValue == null ? "" : aValue.toString();
            } else if (columnIndex == 1 || columnIndex == 2) {
                data[rowIndex][columnIndex] = asLongNullable(aValue);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
            fireTableCellUpdated(rowIndex, 3); // update Total
        }

        private Long asLongNullable(Object v) {
            if (v == null) return null;
            if (v instanceof Number) return ((Number) v).longValue();
            try {
                String s = v.toString().replace(",", "").trim();
                if (s.isEmpty()) return null;
                return Long.parseLong(s);
            } catch (Exception e) {
                return null;
            }
        }
    }
   
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TroopManagerApp().setVisible(true));
    }
}
