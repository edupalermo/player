package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.state.location.Crypt;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.selenium.stacking.Captain;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;

@Slf4j
public class ExploreCrypt {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    private final GameStateService gameStateService = new GameStateService();
    private final PlayerStateService playerStateService = new PlayerStateService();

    public ExploreCrypt(Player player) {
        this.player = player;
    }
    
    public void explore() {
        Point location = playerStateService.getState(player).getExploringCrypt();
        
        if  (location == null) {
            if (playerStateService.getState(player).getCommonTar() < player.getCommonTarRequired() &&
                    playerStateService.getState(player).getExploringCrypt() == null) {
                log.info("Not enough common tar");
                return;
            }

            location = gameStateService
                    .getLocation(Crypt.class)
                    .filter((c) -> c.getLevel() == player.getCommonCryptLevel())         //TODO Delete me
                    .map(Crypt::getPosition)
                    .orElse(null);
        }

        if (location == null) {
            log.info("No Crypt is available");
            return;
        }

        NavigationUtil.switchToMapIfNeeded();

        NavigationUtil.zoomInIfNeeded();

        boolean captainConfigured = (new CaptainSelector(player)).select(Captain.UNKNOW, Captain.CARTER, Captain.UNKNOW);

        if (!captainConfigured) {
            log.info("Carter not available");

            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
        
        Point target = NavigationUtil.goToMapPosition(location);
        robot.leftClick(target);
        
        Navigate titleCrypt = Navigate.builder()
                .areaName(Area.CRYPT_TITLE)
                .resourceName("player/crypts/title_crypt.png")
                .waitLimit(3000)
                .build();
        
        if (!titleCrypt.exist()) {
            log.info("Crypt doesnt exist anymore");
            playerStateService.getState(player).setExploringCrypt(null);
            gameStateService.removeLocationAt(location);
            
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
        
        Transformation transformation = Transformation.builder()
                .real(titleCrypt.getPoint())
                .reference(Point.of(972, 325))
                .build();


        // Select Carter
        robot.leftClick(transformation.transform(Point.of(879, 722)));
        robot.sleep(300);

        // Click on Explore
        robot.leftClick(transformation.transform(Point.of(1170, 867)));
        robot.sleep(300);

        playerStateService.getState(player).setExploringCrypt(location);

        NavigationUtil.speedUpMarch();
    }
}
