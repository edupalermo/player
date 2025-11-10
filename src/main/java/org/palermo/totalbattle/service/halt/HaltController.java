package org.palermo.totalbattle.service.halt;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.SharedData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HaltController {

    @PostMapping("/halt")
    public void record(@RequestBody String playerName) {
        SharedData.INSTANCE.halt(Player.builder().name(playerName).build());
    }
}
