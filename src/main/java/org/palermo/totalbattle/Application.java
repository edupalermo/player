package org.palermo.totalbattle;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.PlayerRunnable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Application {
    
    private static Thread thread = new Thread(new PlayerRunnable());

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        log.info("Application started!!!");
        // thread.start();
    }
}
