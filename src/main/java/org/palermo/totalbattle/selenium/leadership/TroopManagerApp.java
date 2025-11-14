package org.palermo.totalbattle.selenium.leadership;

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
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TroopManagerApp extends JFrame {

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
    
    // ===== Exclusions =====
    private JCheckBox cbRanged, cbMelee, cbMounted, cbElemental, cbFlying, cbDragon;

    // ===== Buttons =====
    private JButton btnRetrieve;
    private JButton btnGenerate;
    private JButton btnAssign;
    private JButton btnClear;

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
        btnAssign   = new JButton("Assign troops");
        buttonsRow.add(btnGenerate);
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

        // ===== Actions =====
        btnGenerate.addActionListener(e -> {
            
            java.util.List<Unit> units = Backend.getUnits(getSelectedPlayerName(), 
                    getSelectedExclusions(), 
                    getSelectedLayers(),
                    (Backend.MonsterOverride) monsterOverride.getSelectedItem());
            units = limitUnits(units);
            int[] quantities = Backend.getUnitQuantity(getInformedHeadCount(), units);

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
            int[] headCount = Backend.getHeadCount(robot, screen);

            leadershipField.setValue(headCount[0]);
            dominanceField.setValue(headCount[1]);
            authorityField.setValue(headCount[2]);
            
            robot.mouseMove(Point.of(pointerInfo.getLocation().x, pointerInfo.getLocation().y));
        });

        setPreferredSize(new Dimension(820, 640));
        pack();
        setLocationRelativeTo(null);
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
        
        int count = 0;
        int limit = getSelectedLimit();
        
        for (Unit unit : input) {
            if (unit.getPool() == Pool.LEADERSHIP) {
                if (count < limit) {
                    output.add(unit);
                    count++;
                }
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
        playerCombo = new JComboBox<>(new String[]{"Palermo", "Peter II", "Mightshaper", "Grirana", "Elanin"});
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
        JPanel panel = new JPanel(new GridLayout(0, 3, 8, 4));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Exclusion",
                TitledBorder.LEFT, TitledBorder.TOP));

        cbRanged    = new JCheckBox("Ranged");
        cbMelee     = new JCheckBox("Melee");
        cbMounted   = new JCheckBox("Mounted");
        cbElemental = new JCheckBox("Elemental");
        cbFlying    = new JCheckBox("Flying");
        cbDragon    = new JCheckBox("Dragon");

        panel.add(cbRanged);
        panel.add(cbMelee);
        panel.add(cbMounted);
        panel.add(cbElemental);
        panel.add(cbFlying);
        panel.add(cbDragon);

        return panel;
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
