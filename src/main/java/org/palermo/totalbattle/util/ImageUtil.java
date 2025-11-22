package org.palermo.totalbattle.util;

import lombok.Getter;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.palermo.totalbattle.dao.OcrDao;
import org.palermo.totalbattle.entity.ProcessedImage;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.model.SearchResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

public class ImageUtil {

    private static final int GRAY_THRESHOLD = 20;

    public static final Pattern PATTERN_FOR_ONLY_NUMBERS = Pattern.compile("^[0-9]+$");

    public static final String WHITELIST_FOR_SPEED_UPS = "0123456789dhm.";
    public static final String WHITELIST_FOR_COUNTDOWN = "0123456789:dhms";
    public static final String WHITELIST_FOR_ONLY_NUMBERS = "0123456789";
    public static final String WHITELIST_FOR_NUMBERS_AND_SLASH = "0123456789,/";
    public static final String WHITELIST_FOR_USERNAME = buildWhitelist("Mightshaper", "Palermo", "Peter II", "Grirana", "Elanin");
    public static final String WHITELIST_FOR_NUMBERS = "0123456789,";
    public static final int PSM_DEFAULT = 3;
    public static final int LINE_OF_PRINTED_TEXT = 6;
    public static final int SINGLE_LINE_MODE = 7;
    public static final int SINGLE_WORD_MODE = 8;
    public static final int PSM_SINGLE_CHARACTER = 10;
    public static final int PSM_SPARSE_TEXT = 11;
    
    public static final String LANGUAGE_TB = "tb";
    
    private static final String HOSTNAME_NOTEBOOK = "eduardo-XPS-15-9500";

    private static Map<String, BufferedImage> imageCache = new HashMap<>();
    
    private static OcrDao ocrDao = new OcrDao();
    
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

    public static void write(BufferedImage image, org.palermo.totalbattle.selenium.leadership.Point offset, BufferedImage size, String filename) {
        write(image.getSubimage(offset.getX(), offset.getY(), size.getWidth(), size.getHeight()), filename);
    }

    /**
     * @param x y width height area inside the screen to search
     */
    public static org.palermo.totalbattle.selenium.leadership.Point bestFit(BufferedImage item, BufferedImage screen, int x, int y, int width, int height) {
        long difference = Long.MAX_VALUE;
        org.palermo.totalbattle.selenium.leadership.Point best = null;

        for (int i = x; i < x + width - item.getWidth(); i++) {
            for (int j = y; j < y + height - item.getHeight(); j++) {
                long currentDifference = compareWithBreak(item, screen, i, j, item.getWidth(), item.getHeight(), difference);
                if (currentDifference < difference) {
                    difference = currentDifference;
                    best = org.palermo.totalbattle.selenium.leadership.Point.of(i, j);
                }
            }
        }

        System.out.println("Difference: " + difference);
        //TODO throw an exception if the difference is too high

        return best;
    }

    private static final String SEARCH_SURROUNDINGS = "history_search_surroundings.dat";
    private static Map<Long, org.palermo.totalbattle.selenium.leadership.Point> searchSurroundingsHistory = IoUtil.deserializeFromFile(SEARCH_SURROUNDINGS, Map.class)
            .orElseGet(() -> new HashMap<>());
    public static Optional<org.palermo.totalbattle.selenium.leadership.Point> searchSurroundings(BufferedImage item, BufferedImage screen, double limit, int variation) {
        return searchSurroundings(item, screen, Area.of(0,0,screen.getWidth(),screen.getHeight()), limit, variation);
    }

    public static Optional<org.palermo.totalbattle.selenium.leadership.Point> searchSurroundings(BufferedImage item, BufferedImage screen, Area area, double limit, int variation) {
        long crc = crcImage(item);
        org.palermo.totalbattle.selenium.leadership.Point past = searchSurroundingsHistory.get(crc);
        org.palermo.totalbattle.selenium.leadership.Point actual = null;
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

    public static Optional<org.palermo.totalbattle.selenium.leadership.Point> search(BufferedImage item, BufferedImage screen, double limit) {
        return search(item, screen, 0, 0, screen.getWidth(), screen.getHeight(), limit);
    }

    public static Optional<org.palermo.totalbattle.selenium.leadership.Point> search(BufferedImage item, BufferedImage screen, Area area, double limit) {
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

    public static List<org.palermo.totalbattle.selenium.leadership.Point> searchMultiple(BufferedImage item, BufferedImage screen, Area area, double limit) {
        return searchMultiple(item, screen, area.getX(), area.getY(), area.getWidth(), area.getHeight(), limit);
    }

    public static List<org.palermo.totalbattle.selenium.leadership.Point> searchMultiple(BufferedImage item, BufferedImage screen, double limit) {
        return searchMultiple(item, screen, 0, 0, screen.getWidth(), screen.getHeight(), limit);
    }

    private static final String HISTORY_FILENAME = "history.dat";
    private static Map<Long, List<org.palermo.totalbattle.selenium.leadership.Point>> history = IoUtil.deserializeFromFile(HISTORY_FILENAME, Map.class)
            .orElseGet(() -> new HashMap<>());
    
    /**
     * @param x y width height area inside the screen to search
     */
    public static Optional<org.palermo.totalbattle.selenium.leadership.Point> search(BufferedImage item, BufferedImage screen, int x, int y, int width, int height, double limit) {
        return realSearch(item, screen, x, y, width, height, limit).map(SearchResponse::getPoint);
    }

    public static Point searchBestFit(BufferedImage[] items, BufferedImage screen, Area area) {
        Point answer = null;
        double best = Double.MAX_VALUE;
        
        for (BufferedImage item : items) {
            SearchResponse response = realSearch(item, screen, area.getX(), area.getY(), area.getWidth(), area.getHeight(), 1)
                    .orElse(null);
            if (response != null) {
                if (response.getDifference() < best) {
                    best = response.getDifference();
                    answer = response.getPoint();
                }
            }
        }
        
        return answer;
    }



    public static Optional<SearchResponse> realSearch(BufferedImage item, BufferedImage screen, int x, int y, int width, int height, double limit) {
        // Clock clock = Clock.start();

        long difference = Long.MAX_VALUE;
        org.palermo.totalbattle.selenium.leadership.Point best = null;

        long crc = crcImage(item);
        List<org.palermo.totalbattle.selenium.leadership.Point> past = history.computeIfAbsent(crc, key -> new ArrayList<>());
        if (!past.isEmpty()) {
            Area area = Area.of(x, y, width, height);
            for (org.palermo.totalbattle.selenium.leadership.Point point : past) {
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
                    best = org.palermo.totalbattle.selenium.leadership.Point.of(i, j);
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
        return Optional.of(SearchResponse
                .builder()
                .point(best)
                .difference(percentage)
                .build());
    }



    /**
     * @param x y width height area inside the screen to search
     */
    public static List<org.palermo.totalbattle.selenium.leadership.Point> searchMultiple(BufferedImage item, BufferedImage screen, int x, int y, int width, int height, double limit) {
        List<org.palermo.totalbattle.selenium.leadership.Point> result = new ArrayList<>();
        long cutOff = Math.round(limit * 3 * 255 * item.getWidth() * item.getHeight());
        for (int i = x; i <= x + width - item.getWidth(); i++) {
            for (int j = y; j <= y + height - item.getHeight(); j++) {
                long currentDifference = compareWithBreak(item, screen, i, j, item.getWidth(), item.getHeight(), cutOff);
                if (currentDifference < Long.MAX_VALUE) {
                    // System.out.println(String.format("Diff: %.3f ", ((double) currentDifference / (3 * 255 * item.getWidth() * item.getHeight()))));
                }
                if (currentDifference < cutOff) {
                    //System.out.println(String.format("Diff / cutoff %d %d", currentDifference, cutOff));
                    result.add(org.palermo.totalbattle.selenium.leadership.Point.of(i, j));
                }
            }
        }
        return result;
    }

    public static List<org.palermo.totalbattle.selenium.leadership.Point> normalizedSearch(BufferedImage item, BufferedImage screen, double limit) {
        List<org.palermo.totalbattle.selenium.leadership.Point> result = new ArrayList<>();
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

    public static BufferedImage removeBackground(BufferedImage original, BufferedImage background, String color) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); // This will support grayscale with transparency
        
        int newBackgroundColor = toRgbInt(color);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalRgb = original.getRGB(x, y);
                int backgroundRgb = background.getRGB(x, y);
                
                if (originalRgb == backgroundRgb) {
                    result.setRGB(x, y, newBackgroundColor);
                }
                else {
                    result.setRGB(x, y, originalRgb);
                }
            }
        }
        return result;
    }

    public static BufferedImage toGrayscale(BufferedImage original, String[] blackReferences) {
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

                double minimumDistance = Double.MAX_VALUE;
                
                for (String blackReference: blackReferences) {
                    int[] rgb = hexToRgb(blackReference); 
                    minimumDistance = Math.min(minimumDistance, Math.abs(rgb[0] - r) + Math.abs(rgb[1] - g) + Math.abs(rgb[2] - b));                    
                }
                
                // Maximum possible distance between two RGB colors
                double maxDistance = 3 * 255;

                // Normalize 0..max → 0..255
                int gray = (int) Math.round((minimumDistance / maxDistance) * 255);
                
                // Set new pixel (same value for R, G, B)
                int newPixel = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                result.setRGB(x, y, newPixel);
            }
        }

        return result;
    }

    public static int[] hexToRgb(String hex) {
        // Remove optional leading '#'
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        if (hex.length() != 6) {
            throw new IllegalArgumentException("Hex color must be 6 characters long.");
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        return new int[] { r, g, b };
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
                normalizedGray = Math.min(normalizedGray, 255);

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
        
        final int MARGIN = 3;
        
        minX = Math.max(minX - MARGIN , 0);
        minY = Math.max(minY - MARGIN, 0);
        return image.getSubimage(
                minX, 
                minY, 
                Math.min(maxX - minX + MARGIN,  width - minX),
                Math.min((maxY - minY + MARGIN), height - minY));
    }

    public static String ocr(BufferedImage image, String whitelist, int pageSegMode) {
        return ocr(image, whitelist, pageSegMode, null);
    }

    public static String ocr(BufferedImage image, String whitelist, Pattern pattern) {

        try {
            List<ProcessedImage> list =  ocrDao.retrieve(image.getWidth(), image.getHeight(), whitelist);
            ProcessedImage databaseAnswer = list.stream()
                    .filter((pi) -> compare(pi.getImage(), image, 0.05))
                    .findAny()
                    .orElse(null);
            if (databaseAnswer != null) {
                if (pattern.matcher(databaseAnswer.getText()).matches()) {
                    return databaseAnswer.getText();
                }
            }

            String stringValue = ocrBestMethod(image, whitelist);
            if (stringValue != null && stringValue.length() > 0) {
                if (pattern.matcher(stringValue).matches()) {
                    return stringValue;
                }
            }
            
            if (HOSTNAME_NOTEBOOK.equalsIgnoreCase(InetAddress.getLocalHost().getHostName())) {
                stringValue = askManualOcr(image);
                
                if (stringValue != null && stringValue.length() > 0) {
                    if (pattern.matcher(stringValue).matches()) {
                        ocrDao.persist(image, stringValue, whitelist);
                        return stringValue;
                    }
                }
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        
        ImageUtil.showImageFor5Seconds(image, "Fail to parse it as " + whitelist);
        throw new RuntimeException("It was not possible to make ocr of the given image!");
    }

    /**
     * Shows a modal popup with the given image, a text field, and a confirm button.
     * Returns the text the user typed, or null if the user cancelled/closed the dialog.
     */
    public static String askManualOcr(BufferedImage image) {
        // Panel with image and text field
        JLabel imageLabel = new JLabel(new ImageIcon(image));

        JTextField textField = new JTextField(20);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.add(imageLabel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(new JLabel("OCR text:"), BorderLayout.WEST);
        bottomPanel.add(textField, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        // Custom button text
        String[] options = { "Confirm", "Cancel" };

        int result = JOptionPane.showOptionDialog(
                null,
                content,
                "Manual OCR",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (result == JOptionPane.OK_OPTION) {
            return textField.getText();
        } else {
            return null; // user cancelled or closed
        }
    }


    public static String ocrBestMethod(BufferedImage image, String whitelist) {
        
        String result = ocr(image, whitelist, SINGLE_LINE_MODE, null);

        String temp = ocr(image, whitelist, SINGLE_WORD_MODE, null);
        if (temp.length() > result.length()) {
            result = temp;
        }

        temp = ocr(image, whitelist, PSM_SINGLE_CHARACTER, null);
        if (temp.length() > result.length()) {
            result = temp;
        }
        return result;
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
        tesseract.setOcrEngineMode(1); // 1 = LSTM only
        try {
            return tesseract.doOCR(image).trim(); // Using BufferedImage directly
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
        showImageAndWait(image, (String) null);
    }

    public static void showImageAndWait(BufferedImage image, Area area) {
        showImageAndWait(crop(image, area), (String) null);
    }

    public static void showImageFor5Seconds(BufferedImage image, String title) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        try {
            SwingUtilities.invokeAndWait(() -> {
                JFrame frame = new JFrame(title);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JLabel label = new JLabel(new ImageIcon(image));
                frame.getContentPane().add(label, BorderLayout.CENTER);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // Create a timer to close the window after 5 seconds
                new Timer(5000, e -> frame.dispose()).start();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    
    public static double compareHistograms(BufferedImage image1, BufferedImage image2) {
        double[][] histogram1 = getHistogram(image1);
        double[][] histogram2 = getHistogram(image2);
        
        double difference = 0;
        long counter = 0;

        for (int i = 0; i < histogram1.length; i++) {
            for (int j = 0; j < histogram1[0].length; j++) {
                difference = difference + Math.abs(histogram1[i][j] - histogram2[i][j]);
                counter++;
            }
        }
        return difference / counter;
    }
    
    
    private static double[][] getHistogram(BufferedImage image) {
        int[][] rgb = new int[3][256];
        
        int counter = 0;
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                
                int argb = image.getRGB(x, y);

                // Extract color components
                int alpha = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (alpha == 0) {
                    continue;                    
                }

                rgb[0][r]++;
                rgb[1][g]++;
                rgb[2][b]++;
                
                counter++;
            }
        }

        double[][] answer = new double[3][256];

        for (int i = 0; i < rgb.length; i++) {
            for (int j = 0; j < rgb[0].length; j++) {
                answer[i][j] = (double) rgb[i][j] / (double) counter;                
            }
        }

        return answer;
    }

    public static LocalDateTime ocrTimer(BufferedImage image, boolean invert) {
        BufferedImage timeLeft = ImageUtil.toGrayscale(image);
        if (invert) {
            timeLeft = ImageUtil.invertGrayscale(timeLeft);
        }
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        
        if (timeLeft.getHeight() < 50) {
            timeLeft = ImageUtil.resize(timeLeft, 50);
        }
        
        String timeLeftAsText = ImageUtil.ocr(timeLeft, ImageUtil.WHITELIST_FOR_COUNTDOWN, ImageUtil.LINE_OF_PRINTED_TEXT);
        System.out.println("Time Left: " + timeLeftAsText);

        LocalDateTime localDateTime = null;
        try {
            localDateTime = calculateNext(timeLeftAsText).orElse(null);
        } catch (Exception e) {
            showImageFor5Seconds(image, "Fail to parse timer");
            throw e;
        }

        return localDateTime;
    }

    public static int ocrNumber(BufferedImage image, boolean invert) {
        BufferedImage timeLeft = ImageUtil.toGrayscale(image);
        if (invert) {
            timeLeft = ImageUtil.invertGrayscale(timeLeft);
        }
        timeLeft = ImageUtil.linearNormalization(timeLeft);

        if (timeLeft.getHeight() < 50) {
            timeLeft = ImageUtil.resize(timeLeft, 50);
        }

        String numberAsText = ImageUtil.ocr(timeLeft, ImageUtil.WHITELIST_FOR_NUMBERS, ImageUtil.LINE_OF_PRINTED_TEXT);
        return Integer.parseInt(numberAsText);
    }

    private static Optional<LocalDateTime> calculateNext(String input) {
        Pattern pattern = Pattern.compile("(\\d+)h[:]?([\\d+]+)m");
        Matcher matcher = pattern.matcher(input.trim());

        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        boolean parsed = false;

        if (matcher.matches()) {
            hours = Integer.parseInt(matcher.group(1));
            minutes = Integer.parseInt(matcher.group(2));
            parsed = true;
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)m[:]?([\\d+]+)5");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                minutes = Integer.parseInt(matcher.group(1));
                seconds = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)m[:]?([\\d+]+)s");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                minutes = Integer.parseInt(matcher.group(1));
                seconds = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)d[:]?([\\d+]+)h");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                days = Integer.parseInt(matcher.group(1));
                hours = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            throw new RuntimeException("Impossible to parse " + input);
        }

        LocalDateTime answer = LocalDateTime.now()
                .plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);

        return Optional.of(answer);
    }
    
    public static BufferedImage resize(BufferedImage input, int height) {
        double scale = height / (double) input.getHeight();

        int targetWidth = (int) (input.getWidth() * scale);
        Image scaled = input.getScaledInstance(targetWidth, height, Image.SCALE_SMOOTH);
        BufferedImage output = new BufferedImage(targetWidth, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = output.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();

        return output;
    }

    public static int toRgbInt(String hex) {
        // Remove optional leading '#'
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        // Expect exactly 6 hex digits
        if (hex.length() != 6) {
            throw new IllegalArgumentException("Hex color must be 6 characters: " + hex);
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        // Pack into 0xRRGGBB 
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }    
}
