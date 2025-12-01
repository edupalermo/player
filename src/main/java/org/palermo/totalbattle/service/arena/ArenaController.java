package org.palermo.totalbattle.service.arena;

import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.player.state.location.Arena;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/arena")
public class ArenaController {

    private GameStateService gameStateService = new GameStateService();
    
    @PostMapping()
    public void record(@RequestBody ArenaBean arenaBean) {
        System.out.println("New arena recorded.");
        
        gameStateService.add(Arena.builder()
                .position(Point.of(arenaBean.getX(), arenaBean.getY()))
                .build());
    }
}
