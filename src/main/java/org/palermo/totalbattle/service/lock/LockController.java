package org.palermo.totalbattle.service.lock;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.service.player.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
public class LockController {
    
    @Autowired
    private PlayerService playerService;
    
    @PostMapping("/lock")
    public void lock(@RequestBody String playerName) {
        playerService.lock(PlayerName.getPlayerByName(playerName));
    }

    @DeleteMapping("/locks")
    public void clear() {
        playerService.free();
    }
    
    @GetMapping("/locks")
    public Set<String> list() {
        // return SharedData.INSTANCE.getLock();
        throw new RuntimeException("Not implemented");
    }
}
