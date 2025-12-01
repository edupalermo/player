package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.bean.UnitQuantity;
import org.palermo.totalbattle.selenium.leadership.model.TroopQuantity;
import org.palermo.totalbattle.player.state.location.Arena;
import org.palermo.totalbattle.player.state.location.Citadel;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.Backend;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.selenium.stacking.Captain;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.util.Navigate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AttackCitadel {
    
    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    private GameStateService gameStateService = new GameStateService();

    public AttackCitadel(Player player) {
        this.player = player;
    }
    
    public void attack() {
        Point arenaLocation = gameStateService
                .getLocation(Citadel.class)
                .filter((c) -> c.getLevel() == 10)
                .map(Citadel::getPosition)
                .orElse(null);
        if (arenaLocation == null) {
            log.info("No citadel is available");
            return;
        }

        NavigationUtil.switchToMapIfNeeded();

        NavigationUtil.zoomInIfNeeded();

        Point target = NavigationUtil.goToMapPosition(arenaLocation).move(0, -8);
        
        robot.leftClick(target);

        Navigate titleElvenCitadel = Navigate.builder()
                .resourceName("player/watchtower/title_elven_citadel.png")
                .areaName(Area.TITLE_ELVEN_CITADEL)
                .waitLimit(5000)
                .build();
        
        if (!titleElvenCitadel.exist()) {
            log.info("Citadel disappeared");
            //gameStateService.removeLocationAt(arenaLocation);
            return;
        }

        Transformation transform = Transformation.builder()
                .real(titleElvenCitadel.getPoint())
                .reference(Point.of(946, 451))
                .build();
        
        robot.leftClick(transform.transform(995, 738));
        robot.sleep(1500);

        boolean captainConfigured = (new CaptainSelector(player)).select(Captain.AYDAE, Captain.MINAMOTO, Captain.UNKNOW);
        
        if (!captainConfigured) {
            log.info("It was not possible to configure the correct captains");
            return;            
        }

        int[] headCount = Backend.getHeadCount(robot);
        
        System.out.println(headCount[0] + " - " + headCount[1] + " - " + headCount[2]);

        List<Unit> units = new ArrayList<>();
        units.add(Unit.G2_RANGED);
        units.add(Unit.G3_RANGED);
        units.add(Unit.G4_RANGED);
        units.add(Unit.STONE_GARGOYLE);
        units.add(Unit.GORGON_MEDUSA);
        
        ConfigurationBuilder builder = Configuration.builder()
                .leadership(headCount[0] - (Unit.EC4_ENGINEER.getHeadCount() - 40))
                .dominance(headCount[1])
                .authority(headCount[2]);

        for (int i = 0; i < units.size(); i++) {
            builder.addUnit(units.get(i));
        }
        
        int[] qtd = builder.build().resolve();
        
        List<TroopQuantity> quantities = new ArrayList<>();
        quantities.add(TroopQuantity.builder()
                .unit(Unit.EC4_ENGINEER)
                .quantity(40)
                .build());
        
        for (int i = 0; i < qtd.length; i++) {
            quantities.add(TroopQuantity.builder()
                    .unit(units.get(i))
                    .quantity(qtd[i])
                    .build());
        }
        
        Backend.fillTroops(robot, quantities);
        
    }
    
    private int[] hardcodeFirst(int[] qtd, int firstValue) {
        int[] answer = new int[qtd.length + 1];
        
        answer[0] = firstValue;
        
        for (int i = 0; i < qtd.length; i++) {
            answer[i +1] = qtd[i];
        }
        
        return answer;
    }
}
