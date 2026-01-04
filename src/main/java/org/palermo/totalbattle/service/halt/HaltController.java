package org.palermo.totalbattle.service.halt;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.player.SharedData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HaltController {

    @PostMapping("/halt")
    public void record(@RequestBody String playerName) {
        // It shouls receive the machine name as well... otherwise is going to be difficult to know where it is halted
        // SharedData.INSTANCE.halt(PlayerName.getPlayerByName(playerName));
        throw new RuntimeException("Not implemented");
    }
}
