package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.state.location.Citadel;
import org.palermo.totalbattle.player.state.location.Crypt;
import org.palermo.totalbattle.player.state.location.Mine;
import org.palermo.totalbattle.player.state.location.Rarity;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;

@Slf4j
public class ExploreCrypts {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    private final GameStateService gameStateService = new GameStateService();
    private final PlayerStateService playerStateService = new PlayerStateService();

    public ExploreCrypts(Player player) {
        this.player = player;
    }
    
    public void explore() {

        //TODO Remove me
        gameStateService.add(Crypt.builder()
                        .rarity(Rarity.COMMON)
                        .position(Point.of(393, 483))
                        .level(10)
                .build());
        
        if (playerStateService.getState(player).getCommonTar() < player.getCommonTarRequired() &&
                playerStateService.getState(player).getExploringCrypt() == null) {
            log.info("Nothing to explore");
            return;
        }

        Point location = gameStateService
                .getLocation(Crypt.class)
                .filter((c) -> c.getLevel() == player.getMiningLevel())
                .map(Crypt::getPosition)
                .orElse(null);
        if (location == null) {
            log.info("No Crypt is available");
            return;
        }

        NavigationUtil.switchToMapIfNeeded();

        NavigationUtil.zoomInIfNeeded();

        Point target = NavigationUtil.goToMapPosition(location);

        robot.leftClick(target);

        playerStateService.getState(player).setExploringCrypt(location);
    }
}
