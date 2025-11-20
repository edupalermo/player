package org.palermo.totalbattle.player.task;

import javazoom.jl.decoder.JavaLayerException;
import org.palermo.totalbattle.player.Player;

import java.io.IOException;
import java.io.InputStream;

public class Announce {
    
    public void playPlayerName(Player player) {
        switch(player) {
            case PALERMO:
                play("player/audio/palermo.mp3");
                break;
            default:
                System.out.println("Not implemented " + player.getName());
        }
    }
    
    private void play(String mp3File) {
        try(InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(mp3File)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + mp3File);
            }
            javazoom.jl.player.Player player = new javazoom.jl.player.Player(is);
            player.play();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JavaLayerException e) {
            throw new RuntimeException(e);
        }
    }
}
