package org.palermo.totalbattle;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.OcrUtil;
import org.palermo.totalbattle.util.ServerFacade;

import java.awt.image.BufferedImage;

@Slf4j
public class Test {

    public static void main(String[] args) {
        ServerFacade server = new ServerFacade();

        Player player = server.startPlaying().orElse(null);
        
        if (player == null) {
            System.out.println("No player to play");
            return;
        }
        
        System.out.println("Playing with " + player.getName());
        
        server.stopPlaying(player);

        
        //Player stop = new Player();
        //stop.setName("Robur");
        //server.stopPlaying(stop);
        
    }

    public static void main3(String[] args) {
        BufferedImage image = ImageUtil.loadResource("player/watchtower/icon_checkmark.png");
        System.out.println(ImageUtil.crcImage(image));
    }

    public static void main2(String[] args) {
        BufferedImage image = ImageUtil.loadResource("test.png");
        //image = ImageUtil.toGrayscale(image);
        // image = ImageUtil.toGrayscale(image);

        image = ImageUtil.toGrayscale(image, new String[] {"FF9900", "FFE04E"});
        image = ImageUtil.resize(image, 400);
        image = ImageUtil.linearNormalization(image);

        //image = ImageUtil.increaseContrast(image);
        ImageUtil.showImageAndWait(image);
        System.out.println(OcrUtil.ocrBestMethod(image, OcrUtil.WHITELIST_FOR_SPEED_UPS));
    }
}
