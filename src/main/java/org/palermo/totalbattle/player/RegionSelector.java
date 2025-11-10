package org.palermo.totalbattle.player;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.palermo.totalbattle.selenium.leadership.Area;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class RegionSelector {

    private RegionSelector() {}

    private static ObjectMapper mapper = new ObjectMapper();
    private static final File file = new File("regions.json");
    
    private static Map<String, Area> regions;
    
    static {
        if (file.exists()) {
            try {
                regions = mapper.readValue(file, new TypeReference<Map<String, Area>>() {});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            regions = new HashMap<>();
        }
    }
    
    public static Area selectArea(String scenario, BufferedImage image) {
        Area area =  regions.get(scenario);
        if (area != null) {
            return area;
        }
        
        Rectangle rectangle = innerSelectArea(null, scenario, image);
        if (rectangle == null) {
            return null;
        }
        
        area = Area.of(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        regions.put(scenario, area);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, regions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return area;
    }

        /**
         * Shows a modal dialog to select a rectangular area on the image.
         * @param parent Parent window (can be null).
         * @param image  The image to display/select on (non-null).
         * @return Rectangle with x,y,width,height of selection, or null if canceled/closed.
         */
    private static Rectangle innerSelectArea(Window parent, String title, BufferedImage image) {
        if (image == null) throw new IllegalArgumentException("image == null");

        final AtomicReference<Rectangle> result = new AtomicReference<>(null);

        Runnable ui = () -> {
            JDialog dialog = new JDialog(parent, "Select Area for " + title, Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            ImagePanel panel = new ImagePanel(image, dialog, result);
            JScrollPane scroll = new JScrollPane(panel);
            scroll.getViewport().setBackground(Color.DARK_GRAY);
            dialog.setContentPane(scroll);

            // ESC cancels (returns null)
            dialog.getRootPane().registerKeyboardAction(e -> {
                result.set(null);
                dialog.dispose();
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        };

        if (SwingUtilities.isEventDispatchThread()) {
            ui.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(ui);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result.get();
    }

    // ---- Helper component that draws the image + selection overlay ----
    private static final class ImagePanel extends JComponent {
        private final BufferedImage image;
        private final Window owner;
        private final AtomicReference<Rectangle> resultRef;

        private Point dragStart;  // image coords
        private Rectangle selection; // normalized selection in image coords

        ImagePanel(BufferedImage image, Window owner, AtomicReference<Rectangle> resultRef) {
            this.image = image;
            this.owner = owner;
            this.resultRef = resultRef;

            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

            MouseAdapter ma = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        dragStart = clampToImage(e.getPoint());
                        selection = new Rectangle(dragStart);
                        repaint();
                    }
                }

                @Override public void mouseDragged(MouseEvent e) {
                    if (dragStart != null) {
                        Point p = clampToImage(e.getPoint());
                        selection = normalizedRect(dragStart, p);
                        repaint();
                    }
                }

                @Override public void mouseReleased(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e) || dragStart == null) return;
                    Point p = clampToImage(e.getPoint());
                    selection = normalizedRect(dragStart, p);
                    dragStart = null;
                    repaint();

                    if (selection.width <= 0 || selection.height <= 0) {
                        // Ignore zero-size selection
                        return;
                    }

                    int choice = JOptionPane.showConfirmDialog(
                            owner,
                            String.format("Use this area? x=%d, y=%d, w=%d, h=%d",
                                    selection.x, selection.y, selection.width, selection.height),
                            "Confirm Selection",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        resultRef.set(new Rectangle(selection));
                        owner.dispose();
                    } else {
                        // allow reselection
                    }
                }
            };

            addMouseListener(ma);
            addMouseMotionListener(ma);

            // Right-click clears current selection
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        selection = null;
                        repaint();
                    }
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(image, 0, 0, null);

                if (selection != null) {
                    // Darken outside selection
                    Composite old = g2.getComposite();
                    g2.setComposite(AlphaComposite.SrcOver.derive(0.45f));
                    g2.setColor(Color.BLACK);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setComposite(old);

                    // "Punch out" selection area by redrawing the image portion
                    g2.drawImage(image.getSubimage(selection.x, selection.y, selection.width, selection.height),
                            selection.x, selection.y, null);

                    // Draw selection border + handles
                    Stroke oldStroke = g2.getStroke();
                    g2.setColor(new Color(0, 120, 215));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRect(selection.x, selection.y, selection.width, selection.height);
                    g2.setStroke(oldStroke);

                    // Info label
                    String info = String.format("x=%d, y=%d, w=%d, h=%d",
                            selection.x, selection.y, selection.width, selection.height);
                    FontMetrics fm = g2.getFontMetrics();
                    int pad = 4;
                    int tx = selection.x + 1;
                    int ty = selection.y - fm.getHeight() - 4;
                    if (ty < 0) ty = selection.y + selection.height + 6;
                    int tw = fm.stringWidth(info) + pad * 2;
                    int th = fm.getHeight() + pad * 2;

                    g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
                    g2.setColor(new Color(30, 30, 30));
                    g2.fillRoundRect(tx, ty, tw, th, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.drawString(info, tx + pad, ty + th - fm.getDescent() - pad);
                }
            } finally {
                g2.dispose();
            }
        }

        private Point clampToImage(Point p) {
            int x = Math.max(0, Math.min(image.getWidth() - 1, p.x));
            int y = Math.max(0, Math.min(image.getHeight() - 1, p.y));
            return new Point(x, y);
        }

        private static Rectangle normalizedRect(Point a, Point b) {
            int x = Math.min(a.x, b.x);
            int y = Math.min(a.y, b.y);
            int w = Math.abs(a.x - b.x);
            int h = Math.abs(a.y - b.y);
            return new Rectangle(x, y, w, h);
        }
    }
}