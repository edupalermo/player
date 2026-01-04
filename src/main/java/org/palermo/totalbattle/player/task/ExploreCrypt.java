package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.entity.PlayerEntity;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.player.state.location.Crypt;
import org.palermo.totalbattle.player.state.location.Location;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.selenium.stacking.Captain;
import org.palermo.totalbattle.service.player.PlayerService;
import org.palermo.totalbattle.util.Navigate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.event.KeyEvent;
import java.util.Optional;

@Slf4j
@Service
public class ExploreCrypt {

    private final MyRobot robot = MyRobot.INSTANCE;

    private final GameStateService gameStateService = new GameStateService();
    
    @Autowired
    private CaptainSelector captainSelector;
    
    @Autowired
    private PlayerService playerService;

    public void explore(PlayerEntity playerEntity) {
        try {
            internalExplore(playerEntity);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }
    
    public void internalExplore(PlayerEntity playerEntity) {
        Crypt crypt = Optional
                .ofNullable(playerEntity.getCommonCryptExploringLocation())
                .orElse(null); 
        
        if  (crypt == null) {
            if (playerEntity.getCommonTar() < playerEntity.getCommonTarRequired() ) {
                log.info("Not enough common tar {}/{}",  playerEntity.getCommonTar(), playerEntity.getCommonTarRequired());
                return;
            }
            
            final int playerCommonCryptLevel = playerEntity.getCommonCryptLevel();

            crypt = gameStateService
                    .getLocation(Crypt.class)
                    .stream()
                    .filter((c) -> c.getLevel() == playerCommonCryptLevel)
                    .findAny()
                    .orElse(null);
        }

        if (crypt == null) {
            log.info("There is not Crypt Level {} available", playerEntity.getCommonCryptLevel());
            return;
        }

        NavigationUtil.switchToMapIfNeeded();

        NavigationUtil.zoomInIfNeeded();

        boolean captainConfigured = captainSelector.select(Captain.UNKNOW, Captain.CARTER, Captain.UNKNOW);

        if (!captainConfigured) {
            log.info("Carter not available");
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
        
        Point target = NavigationUtil.goToMapPosition(crypt.getPosition());
        robot.leftClick(target);
        
        Navigate titleCrypt = Navigate.builder()
                .areaName(Area.CRYPT_TITLE)
                .resourceName("player/crypts/title_crypt.png")
                .waitLimit(3000)
                .build();
        
        if (!titleCrypt.exist()) {
            log.info("Crypt doesnt exist anymore");
            playerEntity.setCommonCryptExploringLocation(null);
            playerEntity = playerService.update(playerEntity);
            gameStateService.removeLocationAt(crypt.getPosition());
            
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

        Navigate secondCaptain = Navigate.builder()
                .area(transformation.transform(Point.of(867, 712), Point.of(893, 738)))
                .resourceName("player/watchtower/icon_checkmark.png")
                .build();

        if (!secondCaptain.exist()) {
            log.info("Carter is not available for Crypt exploration");

            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }

        // Click on Explore
        robot.leftClick(transformation.transform(Point.of(1170, 867)));
        robot.sleep(300);

        playerEntity.setCommonCryptExploringLocation(crypt);
        playerService.update(playerEntity);

        NavigationUtil.speedUpMarch();
    }
}
