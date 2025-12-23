package org.palermo.totalbattle;

import org.palermo.totalbattle.selenium.leadership.model.SearchResponse;
import org.palermo.totalbattle.util.ImageUtil;

import java.awt.image.BufferedImage;

public class TestSearch {

    public static void main(String[] args) {

        BufferedImage area = ImageUtil.load("/home/eduardo/workspace/test/total_battle_selenium/search/465228008/positive/26042177.png");
        BufferedImage item = ImageUtil.load("/home/eduardo/workspace/test/total_battle_selenium/search/465228008.png");

        SearchResponse response = ImageUtil.realSearch(item, area, 0, 0, area.getWidth(), area.getHeight(), 0.1)
                .orElse(null);
        
        if (response != null) {
            System.out.println(response.getDifference());
        }
        
    }
}
