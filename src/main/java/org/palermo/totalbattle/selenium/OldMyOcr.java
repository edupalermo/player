package org.palermo.totalbattle.selenium;

import lombok.Builder;
import lombok.Getter;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OldMyOcr {

    public static void main(String[] args) {

        Map<Character, List<BufferedImage>> database = loadDatabase();

        File folder = new File("./src/main/resources/font/training");
        File[] files = folder.listFiles(File::isFile);
        
        // Sort by name
        Arrays.sort(files, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
        
        for (File file : files) {
            System.out.println(file.getName() + " - " + perform(ImageUtil.load(file), database));
        }

    }
    
    private static Map<Character, List<BufferedImage>> loadDatabase() {
        File folder = new File("./src/main/resources/font/chars");

        File[] subfolders = folder.listFiles(File::isDirectory);

        Map<Character, List<BufferedImage>> database = new HashMap<>();

        if (subfolders == null) {
            throw new RuntimeException("Database is empty!");
        }

        for (File subfolder : subfolders) {
            String folderName = subfolder.getName();

            File[] files = subfolder.listFiles(File::isFile);

            ArrayList<BufferedImage> list = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    BufferedImage template = ImageUtil.load(file);
                    list.add(ImageUtil.linearNormalization(template));
                }
                database.put(getCharacter(folderName), list);
            }
        }
        System.out.println("Database loaded: " + database.size());
        return database;
    }

    private static Character getCharacter(String folderName) {
        if (folderName.length() == 1) {
            return folderName.charAt(0);
        }
        return (char) Integer.parseInt(folderName.split("_")[1], 16);
    }


    public static String perform(BufferedImage image, Map<Character, List<BufferedImage>> database) {
        return database
                .entrySet()
                .parallelStream()
                .map(entry -> findAreas(entry.getKey(), image, entry.getValue()))
                .flatMap(List::stream)
                .sorted((l, r) -> l.getArea().getX() - r.getArea().getX())
                .map(Match::getCharacter)
                .map((character) -> character.toString())
                .collect(Collectors.joining());
    }
    
    public static List<Match> findAreas(Character character, BufferedImage image, List<BufferedImage> templates) {
        List<Area> areas = templates.stream().flatMap((template) -> 
                ImageUtil.normalizedSearch(template, image, 0.5)
                        .stream()
                        .map(point ->Area.of(point.getX(), point.getY(), template.getWidth(), template.getHeight()))
        ).collect(Collectors.toList());

        List<Area> nonIntersectingAreas = new ArrayList<>();
        for (Area area : areas) {
            boolean intersects = nonIntersectingAreas.stream().anyMatch(area::intersect);
            if (!intersects) {
                nonIntersectingAreas.add(area);
            }
        }
        
        
        return nonIntersectingAreas
                .stream()
                .map(area -> Match.builder().character(character).area(area).build())
                .collect(Collectors.toList());
    }
    
    @Builder
    @Getter
    private static class Match {
        private final Area area;
        private final Character character;
        
        
    }
}
