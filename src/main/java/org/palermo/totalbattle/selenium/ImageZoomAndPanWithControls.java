package org.palermo.totalbattle.selenium;

import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.util.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageZoomAndPanWithControls extends JFrame {

    private BufferedImage image;
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double startX, startY;
    private boolean selecting = false;
    private Rectangle selection = null;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> itemList = new JList<>(listModel);
    private ImagePanel imagePanel;
    
    private File FOLDER_TRAINING = new File("./src/main/resources/font/training");

    public ImageZoomAndPanWithControls() {
        setTitle("Image Zoom & Pan with Controls");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);

        try {
            image = ImageIO.read(new File("last_screen.png"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage());
            System.exit(1);
        }

        imagePanel = new ImagePanel();
        JScrollPane scrollPane = new JScrollPane(imagePanel);

        imagePanel.addMouseWheelListener(e -> {
            double oldScale = scale;
            if (e.getPreciseWheelRotation() < 0) {
                scale *= 1.1;
            } else {
                scale /= 1.1;
            }
            double mouseX = e.getX();
            double mouseY = e.getY();
            offsetX = mouseX - ((mouseX - offsetX) * (scale / oldScale));
            offsetY = mouseY - ((mouseY - offsetY) * (scale / oldScale));
            imagePanel.repaint();
        });

        imagePanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (selecting) {
                    startX = (e.getX() - offsetX) / scale;
                    startY = (e.getY() - offsetY) / scale;
                    selection = new Rectangle((int) startX, (int) startY, 0, 0);
                } else {
                    startX = e.getX() - offsetX;
                    startY = e.getY() - offsetY;
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (selecting && selection != null) {
                        BufferedImage subImage = image.getSubimage(
                                selection.x,
                                selection.y,
                                selection.width,
                                selection.height
                        );
                        BufferedImage temporary = ImageUtil.linearNormalization(subImage);
                        //temporary = ImageUtil.increaseContrast(temporary);
                        temporary = ImageUtil.cropText(temporary);
                        openDrawingWindow(temporary);
                        //ImageUtil.write(temporary, "selected_area.png");
                        // JOptionPane.showMessageDialog(null, "Selected area saved as selected_area.png");
                    selecting = false;
                    imagePanel.setCursor(Cursor.getDefaultCursor());
                    selection = null;
                    imagePanel.repaint();
                }
            }
        });

        imagePanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (selecting && selection != null) {
                    int currentX = (int) ((e.getX() - offsetX) / scale);
                    int currentY = (int) ((e.getY() - offsetY) / scale);
                    selection.setSize(currentX - selection.x, currentY - selection.y);
                    imagePanel.repaint();
                } else {
                    offsetX = e.getX() - startX;
                    offsetY = e.getY() - startY;
                    imagePanel.repaint();
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, buildRightPanel());
        splitPane.setDividerLocation(700);

        add(splitPane);
    }

    private JPanel buildRightPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton plusButton = new JButton("+");
        JButton minusButton = new JButton("-");
        JButton selectDarkTextButton = new JButton("Select Dark Text");
        JButton selectLightButton = new JButton("Select Light Text");

        buttonPanel.add(plusButton);
        buttonPanel.add(minusButton);
        buttonPanel.add(selectLightButton);

        plusButton.addActionListener(e -> {
            listModel.addElement("Item " + (listModel.size() + 1));
        });

        minusButton.addActionListener(e -> {
            int selectedIndex = itemList.getSelectedIndex();
            if (selectedIndex != -1) {
                listModel.remove(selectedIndex);
            }
        });

        selectLightButton.addActionListener(e -> {
            selecting = true;
            imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        });

        itemList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = itemList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        String currentValue = listModel.get(index);
                        String newValue = JOptionPane.showInputDialog(ImageZoomAndPanWithControls.this, "Edit Item:", currentValue);
                        if (newValue != null && !newValue.trim().isEmpty()) {
                            listModel.set(index, newValue.trim());
                        }
                    }
                }
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());

        JButton finishButton = new JButton("Finish");
        bottomPanel.add(finishButton);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(10, 10));
        rightPanel.add(buttonPanel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(itemList), BorderLayout.CENTER);
        rightPanel.add(finishButton, BorderLayout.SOUTH);

        return rightPanel;
    }

    class ImagePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(offsetX, offsetY);
                g2d.scale(scale, scale);
                g2d.drawImage(image, 0, 0, this);
                if (selection != null) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRect(selection.x, selection.y, selection.width, selection.height);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImageZoomAndPanWithControls viewer = new ImageZoomAndPanWithControls();
            viewer.setVisible(true);
        });
    }

    public void openDrawingWindow(BufferedImage image) {
        JFrame frame = new JFrame("Drawing Canvas");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel canvas = new JPanel() {
            private Point lastPoint = null;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.drawImage(image, 5, 5, this);
                    // treat(image, g2d);

                }
            }
        };

        JButton acceptButton = new JButton("Accept");
        JButton rejectButton = new JButton("Reject");

        acceptButton.addActionListener(e -> {
            // JOptionPane.showMessageDialog(frame, "Accepted!");
            int i = 0;
            File file;
            boolean searching;
            do {
                file = new File(FOLDER_TRAINING, String.format("%05d", i) + ".png");
                if (searching = file.exists()) {
                    i++;
                }
            } while (searching);
            
            ImageUtil.write(image, file);
            frame.dispose();
        });

        rejectButton.addActionListener(e -> {
            // JOptionPane.showMessageDialog(frame, "Rejected!");
            frame.dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton);

        frame.add(canvas, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }    
    
    
    // 22
    
    private static void treat(BufferedImage image,  Graphics2D g2d) {
        long[] total = new long[image.getWidth()];
        long lower = Long.MAX_VALUE;
        long higher = 0;
        
        for (int x = 0; x < image.getWidth(); x++) { 
            total[x] = sumVertical(image, x);
            if (total[x] < lower) {
                lower = total[x];
            }
            if (total[x] > higher) {
                higher = total[x];
            }
        }

        double[] normalized = new double[image.getWidth()];
        for (int x = 0; x < image.getWidth(); x++) {
            normalized[x] = 1 - ((double) (total[x] - lower) / (higher - lower));
        }
        
        double step = 0.01D;
        double threshold = 0.1; 
        java.util.List<Area> list = null;
        do {
            list = getCharacters(normalized, threshold, image.getHeight());
            System.out.println("Threshold: " + threshold + " characters: " + list.size());
            threshold += step;
        } while (list.size() < 22);
        // threshold -= step;
        // list = getCharacters(normalized, threshold, image.getHeight());
        
        int positionx = 1;
        
        for (Area area : list) {
            g2d.drawImage(ImageUtil.crop(image, area), positionx, 15, null);
            g2d.drawRect(positionx - 1, 45, area.getWidth() + 1, area.getHeight());
            positionx += area.getWidth() + 3;
            
            ImageUtil.write(ImageUtil.crop(image, area), "character" + positionx + ".png");
        }
        
    }

    private static java.util.List<Area> getCharacters(double[] normalized, double threshold, int height) {

        int initial = 0;

        boolean insideChar = false;

        java.util.List<Area> list = new java.util.ArrayList<>();

        for (int x = 0; x < normalized.length; x++) {
            if (insideChar) {
                if (normalized[x] < threshold) {
                    insideChar = false;
                    list.add(Area.of(initial, 0, (x - initial) , height));
                }
            } else {
                if (normalized[x] >= threshold) {
                    initial = x;
                    insideChar = true;
                }
            }
        }

        if (insideChar) {
            list.add(Area.of(initial, 0, (normalized.length - initial) , height));
        }

        return list;
    }


    private static long sumVertical(BufferedImage image,  int x) {
        long total = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            int rgb = image.getRGB(x, y);
            total = total + (rgb & 0xFF);
        }

        return total;
    }


}