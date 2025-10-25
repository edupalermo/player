package org.palermo.totalbattle;

import org.palermo.totalbattle.player.PlayerRunnable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    
    private static Thread thread = new Thread(new PlayerRunnable());

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        thread.start();
    }
}
