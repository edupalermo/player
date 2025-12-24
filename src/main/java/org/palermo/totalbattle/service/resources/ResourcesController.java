package org.palermo.totalbattle.service.resources;

import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.state.Resources;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources")
public class ResourcesController {

    private final PlayerStateService playerStateService = new PlayerStateService();
    private final GameStateService gameStateService = new GameStateService();

    @PostMapping()
    public void set(@RequestBody ResourcesBean resourceBean) {
        Resources resources = Resources.builder()
                .lumber(resourceBean.getLumber())
                .iron(resourceBean.getIron())
                .stone(resourceBean.getStone())
                .silver(resourceBean.getSilver())
                .build();
        playerStateService.getState(Player.getPlayerByName(resourceBean.getPlayerName())).setResourcesTarget(resources);
        playerStateService.saveGameState();
    }
}
