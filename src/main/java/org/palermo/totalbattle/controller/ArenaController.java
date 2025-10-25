package org.palermo.totalbattle.controller;

import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/arena")
public class ArenaController {

    @PostMapping()
    public void record(@RequestBody ArenaBean arenaBean) {
        System.out.println("New arena recorded.");
        SharedData.INSTANCE.addArena(Point.of(arenaBean.getX(), arenaBean.getY()));
    }
}
