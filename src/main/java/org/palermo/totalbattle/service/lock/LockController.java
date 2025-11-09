package org.palermo.totalbattle.service.lock;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.SharedData;
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

    @PostMapping("/lock")
    public void record(@RequestBody String playerName) {
        SharedData.INSTANCE.lock(playerName);
    }

    @DeleteMapping("/locks")
    public void clear() {
        SharedData.INSTANCE.clearLock();
    }
    
    @GetMapping("/locks")
    public Set<String> list() {
        return SharedData.INSTANCE.getLock();
    }
}
