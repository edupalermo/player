package org.palermo.totalbattle.selenium.leadership;

import lombok.Getter;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.palermo.totalbattle.selenium.Clock;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.CRC32;

public class ImageUtil {

    private static final int GRAY_THRESHOLD = 255;

    public static final String WHITELIST_FOR_COUNTDOWN = "0123456789:hms";
    public static final String WHITELIST_FOR_ONLY_NUMBERS = "0123456789";
    public static final String WHITELIST_FOR_NUMBERS_AND_SLASH = "0123456789,/";
    public static final String WHITELIST_FOR_USERNAME = buildWhitelist("Mightshaper", "Palermo", "Peter II", "Grirana", "Elanin");
    public static final String WHITELIST_FOR_NUMBERS = "0123456789,";
    public static final int LINE_OF_PRINTED_TEXT = 6;
    public static final int SINGLE_LINE_MODE = 7;
    public static final int SINGLE_WORD_MODE = 8;
    
    public static final String LANGUAGE_TB = "tb";

    private static Map<String, BufferedImage> imageCache = new HashMap<>();
    
    public static BufferedImage loadResource(String resourceName) {
        BufferedImage cachedImage = imageCache.get(resourceName);
        if (cachedImage != null) {
            return cachedImage;
        }
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            BufferedImage imageRead = ImageIO.read(is);
            imageCache.put(resourceName, imageRead);
            return imageRead;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static BufferedImage load(File file) {
        if (!file.exists()) {
            throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
        }
        try (InputStream is = new FileInputStream(file)) {
            return ImageIO.read(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(BufferedImage image, String filename) {
        write(image, new File(filename));
    }
    
    public static void write(BufferedImage image, File file) {
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(BufferedImage image, Point offset, BufferedImage size, String filename) {
        write(image.getSubimage(offset.getX(), offset.getY(), size.getWidth(), size.getHeight()), filename);
    }

    /**
     * @param x y width height area inside the screen to search
     */
    public static Point bestFit(BufferedImage item, BufferedImage screen, int x, int y, int width, int height) {
        long difference = Long.MAX_VALUE;
        Point best = null;

        for (int i = x; i < x + width - item.getWidth(); i++) {
            for (int j = y; j < y + height - item.getHeight(); j++) {
                long currentDifference = compareWithBreak(item, screen, i, j, item.getWidth(), item.getHeight(), difference);
                if (currentDifference < difference) {
                    difference = currentDifference;
                    best = Point.of(i, j);
                }
            }
        }

        System.out.println("Difference: " + difference);
        //TODO throw an exception if the difference is too high

        return best;
    }

    private static final String SEARCH_SURROUNDINGS = "history_search_surroundings.dat";
    private static Map<Long, Point> searchSurroundingsHistory = IoUtil.deserializeFromFile(SEARCH_SURROUNDINGS, Map.class)
            .orElseGet(() -> new HashMap<>());
    public static Optional<Point> searchSurroundings(BufferedImage item, BufferedImage screen, double limit, int variation) {
        return searchSurroundings(item, screen, Area.of(0,0,screen.getWidth(),screen.getHeight()), limit, variation);
    }

    public static Optional<Point> searchSurroundings(BufferedImage item, BufferedImage screen, Area area, double limit, int variation) {
        long crc = crcImage(item);
        Point past = searchSurroundingsHistory.get(crc);
        Point actual = null;
        if (past != null && area.contain(past)) {
            int x = Math.max(0, past.getX() - variation);
            int y = Math.max(0, past.getY() - variation);
            int width = Math.min(screen.getWidth() - x, item.getWidth() + (2 * variation));
            int height = Math.min(screen.getHeight() - y, item.getHeight() + (2 * variation));
            actual = search(item, screen, x, y, width, height, limit).orElse(null);
        }
        if (actual == null) {
            actual = search(item, screen, area, limit).orElse(null);
        }
        if (actual != null) {
            searchSurroundingsHistory.put(crc, actual);
            IoUtil.serializeToFile(SEARCH_SURROUNDINGS, searchSurroundingsHistory);
        }
        return Optional.ofNullable(actual);
    }

    public static Optional<Point> search(BufferedImage item, BufferedImage screen, double limit) {
        return search(item, screen, 0, 0, screen.getWidth(), screen.getHeight(), limit);
    }

    public static Optional<Point> search(BufferedImage item, BufferedImage screen, Area area, double limit) {
        if (area.getWidth() < item.getWidth() || area.getHeight() < item.getHeight()) { 
            throw new RuntimeException("Cannot search in a area smaller than the image");
        }
        if (area.getWidth() <= 0 || area.getHeight() <= 0) {
            throw new RuntimeException("Area cannot have negative measures");
        }
        if (area.getX() + area.getWidth() > screen.getWidth() ||
                area.getY() + area.getHeight() > screen.getHeight()) {
            throw new RuntimeException("Area doesn't fit to screen");
        }
        
        return search(item, screen, area.getX(), area.getY(), area.getWidth(), area.getHeight(), limit);
    }

    public static List<Point> searchMultiple(BufferedImage item, BufferedImage screen, Area area, double limit) {
        return searchMultiple(item, screen, area.getX(), area.getY(), area.getWidth(), area.getHeight(), limit);
    }

    public static List<Point> searchMultiple(BufferedImage item, BufferedImage screen, double limit) {
        return searchMultiple(item, screen, 0, 0, screen.getWidth(), screen.getHeight(), limit);
    }

    private static final String HISTORY_FILENAME = "history.dat";
    private static Map<Long, List<Point>> history = IoUtil.deserializeFromFile(HISTORY_FILENAME, Map.class)
            .orElseGet(() -> new HashMap<>());
    
    /**
     * @param x y width height area inside the screen to search
     */
    public static Optional<Point> search(BufferedImage item, BufferedImage screen, int x, int y, int width, int height, double limit) {
        Clock clock = Clock.start();

        long difference = Long.MAX_VALUE;
        Point best = null;
        
        long crc = crcImage(item);
        List<Point> past = history.computeIfAbsent(crc, key -> new ArrayList<>());
        if (!past.isEmpty()) {
            Area area = Area.of(x, y, width, height);
            for (Point point : past) {
                if (area.toRectangle().contains(point.getX(), point.getY())) {
                    long currentDifference = compareWithBreak(item, screen, point.getX(), point.getY(), item.getWidth(), item.getHeight(), difference);
                    if (currentDifference < difference) {
                        difference = currentDifference;
                        best = point;
                    }
                }
            }
            if (best != null) {
                // System.out.println("History had " + past.size() + " points");
            }
        }

        for (int i = x; i <= x + width - item.getWidth(); i++) {
            for (int j = y; j <= y + height - item.getHeight(); j++) {
                long currentDifference = compareWithBreak(item, screen, i, j, item.getWidth(), item.getHeight(), difference);
                if (currentDifference < difference) {
                    difference = currentDifference;
                    best = Point.of(i, j);
                }
            }
        }

        double percentage = (double) difference / (3 * 255 * (item.getWidth() * item.getHeight()));
        // System.out.println("Difference: " + difference + " Percentage: " + percentage);
        
        if (percentage > limit) {
            // System.out.println(String.format("Difference %f more than limit: %f ", percentage, limit));
            return Optional.empty();
        }

        if (!history.get(crc).contains(best)) {
            history.get(crc).add(best);
            IoUtil.serializeToFile(HISTORY_FILENAME, history);
        }
        
        // System.out.println("Best " + best.getX() + " " + best.getY() + " "  + percentage + " search took " + clock.elapsedTime());
        return Optional.of(best);
    }
    
    
    

    /**
     * @param x y width height area inside the screen to search
     */
    public static List<Point> searchMultiple(BufferedImage item, BufferedImage screen, int x, int y, int width, int height, double limit) {
        List<Point> result = new ArrayList<>();
        long cutOff = Math.round(limit * 3 * 255 * item.getWidth() * item.getHeight());
        for (int i = x; i <= x + width - item.getWidth(); i++) {
            for (int j = y; j <= y + height - item.getHeight(); j++) {
                long currentDifference = compareWithBreak(item, screen, i, j, item.getWidth(), item.getHeight(), cutOff);
                if (currentDifference < Long.MAX_VALUE) {
                    // System.out.println(String.format("Diff: %.3f ", ((double) currentDifference / (3 * 255 * item.getWidth() * item.getHeight()))));
                }
                if (currentDifference < cutOff) {
                    //System.out.println(String.format("Diff / cutoff %d %d", currentDifference, cutOff));
                    result.add(Point.of(i, j));
                }
            }
        }
        return result;
    }

    public static List<Point> normalizedSearch(BufferedImage item, BufferedImage screen, double limit) {
        List<Point> result = new ArrayList<>();
        for (int i = 0; i <= screen.getWidth() - item.getWidth(); i++) {
            for (int j = 0; j <= screen.getHeight() - item.getHeight(); j++) {
                
                BufferedImage croppedImage = ImageUtil.crop(screen, Area.of(i, j, item.getWidth(), item.getHeight()));
                BufferedImage normalizedScreenArea = ImageUtil.linearNormalization(croppedImage);

                double currentDifference = compareGrayPixelLevel(item, normalizedScreenArea, 0, 0, item.getWidth(), item.getHeight(), limit);
                if (currentDifference < limit) {
                    //System.out.println(String.format("Diff / cutoff %d %d", currentDifference, cutOff));
                    result.add(Point.of(i, j));
                }
            }
        }
        return result;
    }


    public static boolean compare(BufferedImage item, BufferedImage screen, double percentualLimit) {
        long relevantPixels = countValidPixels(item, screen, 0, 0, screen.getWidth(), screen.getHeight());
        long limit = (long) (3 * 255 * relevantPixels * percentualLimit); 
        return compareWithBreak(item, screen, 0, 0, screen.getWidth(), screen.getHeight(), limit) < limit;
    }


    private static long compareWithBreak(BufferedImage item, BufferedImage screen, long differenceLimit) {
        return compareWithBreak(item, screen, 0, 0, screen.getWidth(), screen.getHeight(), differenceLimit);
    }

    private static long compareWithBreak(BufferedImage item, BufferedImage screen, int x, int y, int width, int height, long differenceLimit) {
        
        //if (item.getType() != screen.getType()) {
            // System.err.println(String.format("WARN: Cannot compare images with different type! %d %d", item.getType(), screen.getType()));
            // throw new RuntimeException(String.format("Cannot compare images with different type! %d %d", item.getType(), screen.getType()));
        //}
        
        long difference = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int screenPixel = screen.getRGB(x + i, y + j);
                int itemPixel = item.getRGB(i, j);

                int screenAlpha = (screenPixel >> 24) & 0xFF;
                int itemAlpha = (itemPixel >> 24) & 0xFF;

                // Skip comparison if either pixel is fully transparent
                if (screenAlpha == 0 || itemAlpha == 0) {
                    continue;
                }

                difference += difference(screenPixel, itemPixel);

                if (difference > differenceLimit) {
                    return Long.MAX_VALUE;
                }
            }
        }
        return difference;
    }


    private static double compareGrayPixelLevel(BufferedImage item, BufferedImage screen, int x, int y, int width, int height, double differenceLimit) {

        if (item.getType() != screen.getType()) {
            // System.err.println(String.format("WARN: Cannot compare images with different type! %d %d", item.getType(), screen.getType()));
            // throw new RuntimeException(String.format("Cannot compare images with different type! %d %d", item.getType(), screen.getType()));
        }

        double difference = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int screenPixel = screen.getRGB(x + i, y + j);
                int itemPixel = item.getRGB(i, j);
                
                double itDifference = ((double) grayDifference(screenPixel, itemPixel)) / 255.d; 
                
                if (itDifference > differenceLimit) {
                    return Long.MAX_VALUE;
                }
                
                if (itDifference > difference) {
                    difference = itDifference;
                }

            }
        }
        return difference;
    }

    private static long countValidPixels(BufferedImage item, BufferedImage screen, int x, int y, int width, int height) {
        long relevantPixels = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int screenPixel = screen.getRGB(x + i, y + j);
                int itemPixel = item.getRGB(i, j);

                int screenAlpha = (screenPixel >> 24) & 0xFF;
                int itemAlpha = (itemPixel >> 24) & 0xFF;

                // Skip comparison if either pixel is fully transparent
                if (screenAlpha == 0 || itemAlpha == 0) {
                    continue;
                }

                relevantPixels++;
            }
        }
        return relevantPixels;
    }

    private static int difference(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }

    private static int grayDifference(int rgb1, int rgb2) {
        int a1 = (rgb1 >> 24) & 0xFF;
        int g1 = rgb1 & 0xFF;


        int a2 = (rgb2 >> 24) & 0xFF;
        int g2 = rgb2 & 0xFF;
        
        if ((a1 == 0) || (a2 == 0)) {
            return 0;
        }

        return Math.abs(g1 - g2);
    }
    

    public static BufferedImage invertGrayscale(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); // This will support grayscale with transparency

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = original.getRGB(x, y);

                // Extract color components
                int alpha = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Convert to grayscale using luminosity method
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);

                // Invert the grayscale value
                int invertedGray = 255 - gray;

                // Set new pixel (same value for R, G, B)
                int newPixel = (alpha << 24) | (invertedGray << 16) | (invertedGray << 8) | invertedGray;
                result.setRGB(x, y, newPixel);
            }
        }

        return result;
    }

    public static BufferedImage toGrayscale(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); // This will support grayscale with transparency

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = original.getRGB(x, y);

                // Extract color components
                int alpha = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Convert to grayscale using luminosity method
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);

                // Set new pixel (same value for R, G, B)
                int newPixel = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                result.setRGB(x, y, newPixel);
            }
        }

        return result;
    }
    
    @Getter
    public static class MinMax {

        private final int min;
        private final int max;

        public MinMax() {
            this.min = Integer.MAX_VALUE;
            this.max = Integer.MIN_VALUE;
        }

        public MinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public MinMax compute(int value) {
            int min = Math.min(this.min, value);
            int max = Math.max(this.max, value);
            
            return new MinMax(min, max);
        }

        public MinMax compute(MinMax minMax) {
            int min = Math.min(this.min, minMax.getMin());
            int max = Math.max(this.max, minMax.getMax());

            return new MinMax(min, max);
        }
    }

    public static MinMax minMax(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        MinMax minMax = new MinMax();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = original.getRGB(x, y);

                // Extract color components
                int alpha = (argb >> 24) & 0xFF;
                int g = argb & 0xFF;

                if (alpha == 0) {
                    continue;
                }


                minMax = minMax.compute(g);
            }
        }
        return minMax;
    }


    public static BufferedImage linearNormalization(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); // This will support grayscale with transparency

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);

                // Extract color components
                int gray = rgb & 0xFF;

                min = Math.min(min, gray);
                max = Math.max(max, gray);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);

                // Extract color components
                int alpha = (rgb >> 24) & 0xFF;
                int gray = rgb & 0xFF;
                int normalizedGray = ((gray - min) * 255) / (max - min);
                if (normalizedGray > GRAY_THRESHOLD) {
                    normalizedGray = 255;
                }

                int newPixel = (alpha << 24) | (normalizedGray << 16) | (normalizedGray << 8) | normalizedGray;
                result.setRGB(x, y, newPixel);
            }
        }
        return result;
    }

    public static BufferedImage linearNormalization(BufferedImage original, MinMax minMax) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); // This will support grayscale with transparency

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);

                // Extract color components
                int alpha = (rgb >> 24) & 0xFF;
                int gray = rgb & 0xFF;
                int normalizedGray = (int) Math.round((((double) gray - (double) minMax.getMin()) * 255d) / ((double) minMax.getMax() - (double) minMax.getMin()));

                int newPixel = (alpha << 24) | (normalizedGray << 16) | (normalizedGray << 8) | normalizedGray;
                result.setRGB(x, y, newPixel);
            }
        }
        return result;
    }

    public static BufferedImage scaleUp(BufferedImage original, int scale) {
        BufferedImage scaled = new BufferedImage(original.getWidth() * scale, original.getHeight() * scale, original.getType());
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(original, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
        g2d.dispose();
        return scaled;
    }

    public static BufferedImage increaseContrast(BufferedImage bufferedImage) {
        float scaleFactor = 2.0f;   // Adjust as needed
        float offset = 0f;          // Brightness offset

        RescaleOp rescale = new RescaleOp(scaleFactor, offset, null);
        return rescale.filter(bufferedImage, null);
    }

    public static BufferedImage cropText(BufferedImage image)  {
        int width = image.getWidth();
        int height = image.getHeight();

        int minX = width, minY = height;
        int maxX = 0, maxY = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = rgb & 0xFF;

                if (gray < GRAY_THRESHOLD) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        return image.getSubimage(minX, minY, (maxX - minX + 1), (maxY - minY + 1));
    }

    public static String ocr(BufferedImage image, String whitelist, int pageSegMode) {
        return ocr(image, whitelist, pageSegMode, null);
    }
    
    public static String ocr(BufferedImage image, String whitelist, int pageSegMode, String language) {
        Tesseract tesseract = new Tesseract();
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        if (isWindows) {
            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        } else {
            tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        }
        if (language != null) {
            tesseract.setLanguage(language);
        }
        tesseract.setTessVariable("tessedit_char_whitelist", whitelist);
        tesseract.setPageSegMode(pageSegMode); // single line mode

        try {
            return tesseract.doOCR(scaleUp(image, 3)).trim(); // Using BufferedImage directly
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage crop(BufferedImage image, Area area) {
        return image.getSubimage(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    }

    public static String buildWhitelist(String... inputs) {
        Set<Character> uniqueChars = new LinkedHashSet<>();
        for (String input : inputs) {
            for (char c : input.toCharArray()) {
                uniqueChars.add(c);
            }
        }

        StringBuilder result = new StringBuilder();
        for (char c : uniqueChars) {
            result.append(c);
        }

        return result.toString();
    }

    /*
    public static void savePngWithDPI(BufferedImage image, File output, int dpi) throws Exception {
        // Converter DPI para pixels por milímetro
        double inchesPerMillimeter = 1.0 / 25.4;
        double pixelsPerMillimeter = dpi * inchesPerMillimeter;

        // Criar o escritor de imagem
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer = writers.next();

        // Criar o stream de saída
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(ios);

            // Criar metadados com DPI
            IIOMetadata metadata = writer.getDefaultImageMetadata(ImageIO.getImageTypeSpecifier(image), null);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                throw new IllegalArgumentException("Não foi possível modificar os metadados.");
            }

            IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
            IIOMetadataNode dimension = new IIOMetadataNode("Dimension");

            IIOMetadataNode horizontalPixelSize = new IIOMetadataNode("HorizontalPixelSize");
            horizontalPixelSize.setAttribute("value", Double.toString(1.0 / pixelsPerMillimeter));

            IIOMetadataNode verticalPixelSize = new IIOMetadataNode("VerticalPixelSize");
            verticalPixelSize.setAttribute("value", Double.toString(1.0 / pixelsPerMillimeter));

            dimension.appendChild(horizontalPixelSize);
            dimension.appendChild(verticalPixelSize);
            root.appendChild(dimension);
            metadata.mergeTree("javax_imageio_1.0", root);

            // Salvar imagem com metadados
            writer.write(null, new javax.imageio.IIOImage(image, null, metadata), null);
        }

        writer.dispose();
    }
     */

    public static long crcImage(BufferedImage image) {
        CRC32 crc = new CRC32();
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        for (int p : pixels) crc.update(p);
        return crc.getValue();
    }

    public static void showImageAndWait(BufferedImage image) {
        showImageAndWait(image, null);
    }


    public static void showImageAndWait(BufferedImage image, String title) {
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
}
