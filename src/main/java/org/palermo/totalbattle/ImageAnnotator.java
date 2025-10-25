package org.palermo.totalbattle;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class ImageAnnotator {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }

    // ==== App Frame ====
    static class App extends JFrame {
        private final DefaultListModel<String> nameModel = new DefaultListModel<>();
        private final JList<String> nameList = new JList<>(nameModel);
        private final ImagePanel imagePanel = new ImagePanel();
        private final Map<String, List<Rectangle2D.Double>> annotations = new HashMap<>(); // name -> boxes

        App() {
            super("Image Annotator");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setMinimumSize(new Dimension(1000, 700));

            // Left panel: names + controls
            JPanel left = new JPanel(new BorderLayout(8, 8));
            left.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            nameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            nameList.addListSelectionListener(this::onNameSelected);

            Arrays.asList("Alice", "Bob", "Charlie").forEach(nameModel::addElement);
            nameList.setSelectedIndex(0);

            JScrollPane namesScroll = new JScrollPane(nameList);
            left.add(new JLabel("Names"), BorderLayout.NORTH);
            left.add(namesScroll, BorderLayout.CENTER);

            JPanel nameControls = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;

            JTextField nameField = new JTextField();
            nameControls.add(nameField, c);

            JButton addBtn = new JButton("Add");
            c.gridx = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            nameControls.add(addBtn, c);

            JButton removeBtn = new JButton("Remove");
            c.gridx = 2;
            nameControls.add(removeBtn, c);

            left.add(nameControls, BorderLayout.SOUTH);

            addBtn.addActionListener(e -> {
                String n = nameField.getText().trim();
                if (!n.isEmpty() && !containsIgnoreCase(nameModel, n)) {
                    nameModel.addElement(n);
                    nameList.setSelectedIndex(nameModel.size() - 1);
                    nameField.setText("");
                }
            });

            removeBtn.addActionListener(e -> {
                int idx = nameList.getSelectedIndex();
                if (idx >= 0) {
                    String name = nameModel.get(idx);
                    annotations.remove(name);
                    nameModel.remove(idx);
                    if (!nameModel.isEmpty()) nameList.setSelectedIndex(Math.min(idx, nameModel.size() - 1));
                    imagePanel.setBoxes(currentBoxes());
                }
            });

            // Right panel: toolbar + image panel (scrollable)
            JPanel right = new JPanel(new BorderLayout());
            right.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 8));

            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            JButton openBtn = new JButton("Open Image");
            JButton undoBtn = new JButton("Undo");
            JButton clearBtn = new JButton("Clear");
            JButton saveBtn = new JButton("Save JSON");

            // Zoom controls
            JButton zoomOutBtn = new JButton("−");
            JButton zoomInBtn  = new JButton("+");
            JButton fitBtn     = new JButton("Fit");
            JButton oneToOneBtn= new JButton("100%");

            toolbar.add(openBtn);
            toolbar.addSeparator();
            toolbar.add(undoBtn);
            toolbar.add(clearBtn);
            toolbar.addSeparator();
            toolbar.add(saveBtn);
            toolbar.add(Box.createHorizontalStrut(16));
            toolbar.add(new JLabel("Zoom: "));
            toolbar.add(zoomOutBtn);
            toolbar.add(zoomInBtn);
            toolbar.add(fitBtn);
            toolbar.add(oneToOneBtn);

            right.add(toolbar, BorderLayout.NORTH);

            JScrollPane imageScroll = new JScrollPane(imagePanel);
            imageScroll.getVerticalScrollBar().setUnitIncrement(24);
            imageScroll.getHorizontalScrollBar().setUnitIncrement(24);
            right.add(imageScroll, BorderLayout.CENTER);

            // Split pane
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
            split.setResizeWeight(0.25);
            setContentPane(split);

            // Wire toolbar actions
            openBtn.addActionListener(e -> onOpenImage());
            undoBtn.addActionListener(e -> {
                List<Rectangle2D.Double> boxes = currentBoxes();
                if (!boxes.isEmpty()) {
                    boxes.remove(boxes.size() - 1);
                    imagePanel.repaint();
                }
            });
            clearBtn.addActionListener(e -> {
                currentBoxes().clear();
                imagePanel.repaint();
            });
            saveBtn.addActionListener(e -> onSaveJson());

            zoomInBtn.addActionListener(e -> imagePanel.zoomAtViewportCenter(1.25));
            zoomOutBtn.addActionListener(e -> imagePanel.zoomAtViewportCenter(1.0 / 1.25));
            fitBtn.addActionListener(e -> imagePanel.zoomToFit(imageScroll));
            oneToOneBtn.addActionListener(e -> imagePanel.setScale(1.0, null));

            // When user draws a box, store it for current name
            imagePanel.setOnBoxFinished(rect -> {
                String name = currentName();
                if (name != null) {
                    currentBoxes().add(rect);
                    imagePanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Select a name on the left first.");
                }
            });

            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void onOpenImage() {
            JFileChooser chooser = new JFileChooser();
            int r = chooser.showOpenDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img == null) throw new IOException("Unsupported image format");
                    imagePanel.setImage(img, f.getName());
                    imagePanel.setScale(1.0, null);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to open image: " + ex.getMessage());
                }
            }
        }

        private void onSaveJson() {
            if (imagePanel.getImage() == null) {
                JOptionPane.showMessageDialog(this, "Open an image first.");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("annotations.json"));
            int r = chooser.showSaveDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                File out = chooser.getSelectedFile();
                try (PrintWriter pw = new PrintWriter(new FileWriter(out))) {
                    String json = toJson(annotations, imagePanel.getImageName(), imagePanel.getImageWidth(), imagePanel.getImageHeight());
                    pw.print(json);
                    JOptionPane.showMessageDialog(this, "Saved: " + out.getAbsolutePath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage());
                }
            }
        }

        private void onNameSelected(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                imagePanel.setBoxes(currentBoxes());
            }
        }

        private String currentName() {
            return nameList.getSelectedValue();
        }

        private List<Rectangle2D.Double> currentBoxes() {
            String name = currentName();
            if (name == null) return Collections.emptyList();
            return annotations.computeIfAbsent(name, k -> new ArrayList<>());
        }

        private static boolean containsIgnoreCase(DefaultListModel<String> model, String name) {
            for (int i = 0; i < model.size(); i++) {
                if (model.get(i).equalsIgnoreCase(name)) return true;
            }
            return false;
        }

        // Minimal JSON writer (no external libs)
        private static String toJson(Map<String, List<Rectangle2D.Double>> data, String imageName, int w, int h) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"image\": ").append(quote(imageName)).append(",\n");
            sb.append("  \"width\": ").append(w).append(",\n");
            sb.append("  \"height\": ").append(h).append(",\n");
            sb.append("  \"annotations\": {\n");
            int i = 0;
            for (Map.Entry<String, List<Rectangle2D.Double>> e : data.entrySet()) {
                sb.append("    ").append(quote(e.getKey())).append(": [");
                List<Rectangle2D.Double> boxes = e.getValue();
                for (int j = 0; j < boxes.size(); j++) {
                    Rectangle2D.Double r = boxes.get(j);
                    sb.append(String.format(Locale.ROOT, "{\"x\":%.2f,\"y\":%.2f,\"w\":%.2f,\"h\":%.2f}", r.x, r.y, r.width, r.height));
                    if (j < boxes.size() - 1) sb.append(", ");
                }
                sb.append("]");
                if (i < data.size() - 1) sb.append(",");
                sb.append("\n");
                i++;
            }
            sb.append("  }\n");
            sb.append("}\n");
            return sb.toString();
        }

        private static String quote(String s) {
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
    }

    // ==== Image Panel with drawing + zoom/pan ====
    static class ImagePanel extends JComponent {
        private BufferedImage image;
        private String imageName = "";

        // Boxes are in IMAGE coordinates (pixels of the original image)
        private final List<Rectangle2D.Double> boxesView = new ArrayList<>(); // bound to current name

        // Drawing style
        private final Stroke boxStroke = new BasicStroke(2f);
        private final Color boxFill = new Color(0, 120, 215, 60);
        private final Color boxLine = new Color(0, 120, 215);

        // Live drawing (in IMAGE coords)
        private Point2D.Double dragStartImg;
        private Rectangle2D.Double liveRectImg;

        // Zoom / pan
        private double scale = 1.0;
        private final double MIN_SCALE = 0.1;
        private final double MAX_SCALE = 8.0;

        // For panning via viewport
        private Point lastPanPoint; // component coords
        private boolean spacePressed = false;

        interface BoxListener { void onBoxFinished(Rectangle2D.Double rect); }
        private BoxListener listener;

        ImagePanel() {
            setOpaque(true);
            setBackground(Color.DARK_GRAY);

            // Mouse for drawing boxes (left button)
            MouseAdapter drawAdapter = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    if (image == null) return;
                    if (isPanGesture(e)) {
                        startPan(e);
                        return;
                    }
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        requestFocusInWindow();
                        dragStartImg = toImagePoint(e.getPoint());
                        liveRectImg = new Rectangle2D.Double(dragStartImg.x, dragStartImg.y, 0, 0);
                        repaint();
                    }
                }

                @Override public void mouseDragged(MouseEvent e) {
                    if (image == null) return;
                    if (isPanning()) {
                        panTo(e);
                        return;
                    }
                    if (SwingUtilities.isLeftMouseButton(e) && dragStartImg != null) {
                        Point2D.Double pImg = toImagePoint(e.getPoint());
                        double x = Math.min(dragStartImg.x, pImg.x);
                        double y = Math.min(dragStartImg.y, pImg.y);
                        double w = Math.abs(pImg.x - dragStartImg.x);
                        double h = Math.abs(pImg.y - dragStartImg.y);
                        liveRectImg.setFrame(x, y, w, h);
                        constrainToImage(liveRectImg);
                        repaint();
                    }
                }

                @Override public void mouseReleased(MouseEvent e) {
                    if (image == null) return;
                    if (isPanning()) {
                        stopPan();
                        return;
                    }
                    if (SwingUtilities.isLeftMouseButton(e) && liveRectImg != null) {
                        if (liveRectImg.width >= 3 && liveRectImg.height >= 3) {
                            if (listener != null) listener.onBoxFinished(new Rectangle2D.Double(
                                    liveRectImg.x, liveRectImg.y, liveRectImg.width, liveRectImg.height));
                        }
                        liveRectImg = null;
                        dragStartImg = null;
                        repaint();
                    }
                }

                @Override public void mouseWheelMoved(MouseWheelEvent e) {
                    if (image == null) return;
                    // Zoom at cursor
                    double factor = Math.pow(1.1, -e.getPreciseWheelRotation());
                    zoomAtPoint(e.getPoint(), factor);
                }
            };

            addMouseListener(drawAdapter);
            addMouseMotionListener(drawAdapter);
            addMouseWheelListener(drawAdapter);

            // Spacebar to pan
            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        spacePressed = true;
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                }
                @Override public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        spacePressed = false;
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            });

            setFocusable(true);
        }

        // ---- Public API ----
        void setOnBoxFinished(BoxListener l) { this.listener = l; }

        void setImage(BufferedImage img, String name) {
            this.image = img;
            this.imageName = name == null ? "" : name;
            updatePreferredSize();
            revalidate();
            repaint();
        }

        BufferedImage getImage() { return image; }
        String getImageName() { return imageName; }
        int getImageWidth() { return image != null ? image.getWidth() : 0; }
        int getImageHeight() { return image != null ? image.getHeight() : 0; }

        void setBoxes(List<Rectangle2D.Double> boxes) {
            boxesView.clear();
            if (boxes != null) boxesView.addAll(boxes);
            repaint();
        }

        /** Zoom with center at viewport center (for toolbar +/-). */
        void zoomAtViewportCenter(double factor) {
            JViewport vp = getViewport();
            Point center = (vp != null) ? new Point(vp.getViewRect().x + vp.getViewRect().width/2,
                    vp.getViewRect().y + vp.getViewRect().height/2)
                    : new Point(getWidth()/2, getHeight()/2);
            zoomAtPoint(center, factor);
        }

        /** Zoom so the whole image fits into current viewport. */
        void zoomToFit(JScrollPane scrollPane) {
            if (image == null) return;
            Dimension viewSize = scrollPane.getViewport().getExtentSize();
            double sx = viewSize.getWidth()  / image.getWidth();
            double sy = viewSize.getHeight() / image.getHeight();
            double fit = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.min(sx, sy)));
            setScale(fit, new Point(viewSize.width/2, viewSize.height/2));
        }

        /** Set absolute scale; anchor may be null (keeps top-left stable). */
        void setScale(double newScale, Point anchorInView) {
            if (image == null) return;
            newScale = clamp(newScale, MIN_SCALE, MAX_SCALE);

            JViewport vp = getViewport();
            Rectangle viewRect = (vp != null) ? vp.getViewRect() : new Rectangle(0,0,getWidth(),getHeight());
            Point anchor = (anchorInView != null) ? anchorInView : viewRect.getLocation();

            // Keep the same image-point under the anchor after scaling
            Point2D.Double imgPtBefore = toImagePoint(anchor);
            this.scale = newScale;
            updatePreferredSize();
            revalidate(); // let scrollbars adapt

            SwingUtilities.invokeLater(() -> {
                if (vp != null) {
                    Point2D.Double compPtAfter = toComponentPoint(imgPtBefore);
                    int vx = (int)Math.round(compPtAfter.x - (anchor.x - viewRect.x));
                    int vy = (int)Math.round(compPtAfter.y - (anchor.y - viewRect.y));
                    vp.setViewPosition(new Point(Math.max(0, vx), Math.max(0, vy)));
                }
                repaint();
            });
        }

        // ---- Internals ----
        private void zoomAtPoint(Point viewPoint, double factor) {
            if (image == null) return;
            double target = clamp(scale * factor, MIN_SCALE, MAX_SCALE);
            setScale(target, viewPoint);
        }

        private JViewport getViewport() {
            Container p = getParent();
            return (p instanceof JViewport) ? (JViewport) p : null;
        }

        private boolean isPanGesture(MouseEvent e) {
            return SwingUtilities.isMiddleMouseButton(e) || (spacePressed && SwingUtilities.isLeftMouseButton(e));
        }

        private boolean isPanning() { return lastPanPoint != null; }

        private void startPan(MouseEvent e) {
            requestFocusInWindow();
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            lastPanPoint = e.getPoint();
        }

        private void panTo(MouseEvent e) {
            JViewport vp = getViewport();
            if (vp == null) return;
            Point now = e.getPoint();
            int dx = lastPanPoint.x - now.x;
            int dy = lastPanPoint.y - now.y;
            Rectangle vr = vp.getViewRect();
            vr.translate(dx, dy);
            vp.setViewPosition(vr.getLocation());
            lastPanPoint = now;
        }

        private void stopPan() {
            lastPanPoint = null;
            if (!spacePressed) setCursor(Cursor.getDefaultCursor());
        }

        private void updatePreferredSize() {
            if (image != null) {
                int w = (int)Math.round(image.getWidth()  * scale);
                int h = (int)Math.round(image.getHeight() * scale);
                setPreferredSize(new Dimension(Math.max(1, w), Math.max(1, h)));
            } else {
                setPreferredSize(new Dimension(600, 400));
            }
        }

        private double clamp(double v, double lo, double hi) {
            return Math.max(lo, Math.min(hi, v));
        }

        // Coordinate transforms
        private AffineTransform getImageToComponentTransform() {
            AffineTransform at = new AffineTransform();
            at.scale(scale, scale); // image (0,0) maps to component (0,0)
            return at;
        }

        private AffineTransform getComponentToImageTransform() {
            try {
                return getImageToComponentTransform().createInverse();
            } catch (NoninvertibleTransformException e) {
                return new AffineTransform();
            }
        }

        private Point2D.Double toImagePoint(Point p) {
            Point2D res = getComponentToImageTransform().transform(p, null);
            return new Point2D.Double(res.getX(), res.getY());
        }

        private Point2D.Double toComponentPoint(Point2D img) {
            Point2D res = getImageToComponentTransform().transform(img, null);
            return new Point2D.Double(res.getX(), res.getY());
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (image != null) {
                // Apply scale
                g2.transform(getImageToComponentTransform());

                // draw image in image coords
                g2.drawImage(image, 0, 0, null);

                // draw existing boxes (image coords)
                g2.setStroke(new BasicStroke((float)(2f / scale))); // keep screen-line ~2px
                for (Rectangle2D.Double r : boxesView) {
                    g2.setColor(adjustAlpha(boxFill, 1.0));
                    g2.fill(r);
                    g2.setColor(boxLine);
                    g2.draw(r);
                }

                // live box
                if (liveRectImg != null) {
                    float[] dash = {6f/(float)scale, 6f/(float)scale};
                    g2.setColor(adjustAlpha(boxFill, 1.0));
                    g2.fill(liveRectImg);
                    g2.setColor(boxLine);
                    g2.setStroke(new BasicStroke((float)(2f/scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0));
                    g2.draw(liveRectImg);
                }
            } else {
                // Placeholder when no image is loaded
                g2.setColor(Color.LIGHT_GRAY);
                String msg = "Open an image to start (PNG/JPG). Use mouse wheel to zoom, Space+Drag to pan.";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
            }

            g2.dispose();
        }

        private Color adjustAlpha(Color c, double alphaMultiplier) {
            int a = (int)Math.round(c.getAlpha() * alphaMultiplier);
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
        }

        private void constrainToImage(Rectangle2D.Double r) {
            if (image == null) return;
            double maxX = image.getWidth();
            double maxY = image.getHeight();
            if (r.x < 0) { r.width += r.x; r.x = 0; }
            if (r.y < 0) { r.height += r.y; r.y = 0; }
            if (r.x + r.width > maxX) r.width = maxX - r.x;
            if (r.y + r.height > maxY) r.height = maxY - r.y;
        }
    }
}
