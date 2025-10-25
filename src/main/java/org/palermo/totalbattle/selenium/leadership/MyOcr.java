package org.palermo.totalbattle.selenium.leadership;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyOcr {
    
    public static void main(String arg[]) {
        List<CharRepresentation> database = loadDatabase("font02");
        System.out.println("Database loaded with " + database.size() + " entries");

        BufferedImage trial = ImageUtil.load(new File("./src/main/resources/font/trial01.png"));
        BufferedImage grayscaleTrial = ImageUtil.toGrayscale(trial);

        List<CharPosition> response = parse(grayscaleTrial, database, 2);
        
        for (CharPosition charPosition: response) {
            System.out.println("Char: " + charPosition.getRep().getCharacter() + " Pos: " + charPosition.getX() + " Diff: " + charPosition.getDiff());
        }
    }

    /**
     * 
     * @param grayscaleImage grayscale
     * @return
     */
    public static String decode(BufferedImage grayscaleImage, String databaseName) {
        List<CharRepresentation> database = loadDatabase(databaseName);
        List<CharPosition> response = parse(grayscaleImage, database, 2);
        
        StringBuilder sb = new StringBuilder();
        for (CharPosition charPosition: response) {
            sb.append(charPosition.getRep().getCharacter());
        }

        return sb.toString();
    }
    
    private static List<CharPosition> parse(BufferedImage image, List<CharRepresentation> database, int skip) {
        List<CharPosition> result = new ArrayList<>();
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (CharRepresentation rep : database) {
                if (image.getWidth() - x < rep.getFunction().length) {
                    continue;
                }
                
                double diff = difference(image, x, rep, skip);
                System.out.println("Diff: " + diff + " Char: " + rep.getCharacter() + " Pos: " + x);
                if (diff < 0.15D) {
                    result.add(new CharPosition(rep, x, diff));
                }
            }                        
        }
        
        return result;
    }
    
    private static double difference(BufferedImage image, int x, CharRepresentation rep, int skip) {

        double[] imageFunction = normalizeArray(skip(toFunction(image, x, rep.getFunction().length), skip));
        double[] sample = normalizeArray(skip(rep.getFunction(), skip));
        
        double total = 0;  
        
        for (int i = 0; i < imageFunction.length; i++) {
            total = total + Math.abs(imageFunction[i] - sample[i]);
        }
        
        return total / (imageFunction.length);
    }


    private static int[] skip(int[] input, int skip) {
        int[] output = new int[input.length - 2 * skip];
        for (int i = 0; i < input.length - 2 * skip; i++) {
            output[i] = input[i + skip];
        }
        return output;
    }
    
    private static int[] toFunction(BufferedImage image, int x, int size) {
        int[] result = new int[size];
        
        for (int i = 0 ; i < size ; i++) {
            for (int y = 0 ; y < image.getHeight() ; y++) {
                int argb = image.getRGB(x + i, y);

                int alpha = (argb >> 24) & 0xFF;
                int g = argb & 0xFF;

                if (alpha == 0) {
                    continue;
                }
                result[i] = result[i] + g;
            }
        }
        return result;
    }
    
    private static List<CharRepresentation> loadDatabase(String databaseName) {
        File folder = new File("./src/main/resources/font/" + databaseName);

        File[] files = folder.listFiles(File::isFile);

        List<CharRepresentation> list = new ArrayList<>();
        
        ImageUtil.MinMax minMax = new ImageUtil.MinMax();
        
        for (File file : files) {
            BufferedImage image = ImageUtil.load(file);
            BufferedImage grayscaleImage = ImageUtil.toGrayscale(image);
            minMax = minMax.compute(ImageUtil.minMax(grayscaleImage));
            CharRepresentation rep = CharRepresentation.builder()
                    .character(getCharacter(file.getName()))
                    .image(grayscaleImage)
                    .build();
            list.add(rep);
        }

        for (int i = 0; i < list.size(); i++) {
            CharRepresentation rep = list.get(i);
            BufferedImage normalizedImage = ImageUtil.linearNormalization(rep.getImage(), minMax);
            rep = rep.withFunction(toFunction(normalizedImage));
            list.set(i, rep);
        }
        
        return list;
    }
    
    private static Character getCharacter(String filename) {
        String withoutExtension = filename.substring(0, filename.length() - 4);
        if (withoutExtension.contains("_")) {
            String utf = (withoutExtension.split("_")[2]);
            return (char) Integer.parseInt(utf, 16);
        }
        return filename.charAt(0);
    }
    
    private static final int[] toFunction(BufferedImage image) {
        int[] result = new int[image.getWidth()];
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int argb = image.getRGB(x, y);

                int alpha = (argb >> 24) & 0xFF;
                int g = argb & 0xFF;

                if (alpha == 0) {
                    continue;
                }
                result[x] = result[x] + g;
            }
        }
        return result;
    }

    private static double[] normalizeArray(int[] input) {

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < input.length; i++) {
            if (input[i] < min) {
                min = input[i];
            }
            if (input[i] > max) {
                max = input[i];
            }
        }

        double[] output = new double[input.length];
        for (int x = 0; x < output.length; x++) {
            output[x] = ((double) input[x] - (double) min) / ((double) max - (double) min);
        }
        return output;
    }


    @Getter
    @Builder
    public static class CharRepresentation {
        
        private final BufferedImage image;
        @With
        private final int[] function;
        private final Character character;
        
    }

    @Getter
    @Builder
    public static class CharPosition {
        private final CharRepresentation rep;
        private final int x;
        private final double diff;
    }

}