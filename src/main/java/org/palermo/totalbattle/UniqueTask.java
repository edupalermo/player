package org.palermo.totalbattle;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.Task;
import org.palermo.totalbattle.service.util.UtilService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class UniqueTask {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        log.info("Application started!!!");

    }

    @Bean
    CommandLineRunner run(Task task) {
        return args -> {
            task.play();
        };
    }
}
