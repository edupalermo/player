package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.entity.PlayerEntity;
import org.palermo.totalbattle.internalservice.ArmyService;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.state.location.Citadel;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.Backend;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.selenium.leadership.model.TroopQuantity;
import org.palermo.totalbattle.selenium.stacking.Captain;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.selenium.stacking.UnitType;
import org.palermo.totalbattle.service.property.PlayerPropertyService;
import org.palermo.totalbattle.util.Navigate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AttackCitadel {
    
    private final MyRobot robot = MyRobot.INSTANCE;

    private GameStateService gameStateService = new GameStateService();
    
    @Autowired
    private ArmyService armyService;

    @Autowired
    private CaptainSelector captainSelector;
    
    @Autowired
    private PlayerPropertyService playerPropertyService;

    public void attack(PlayerEntity playerEntity) {
        try{
            internalAttack(playerEntity);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
    }

    public void internalAttack(PlayerEntity playerEntity) {
        if (!playerPropertyService.isLocked(playerEntity, Scenario.FINISHED_TRAINING_NON_MONSTERS)) {
            log.info("Aborting Citadel attack because there are no NO troops trained");
            return;
        }

        Point citadelLocation = gameStateService
                .getLocation(Citadel.class)
                .stream()
                .filter((c) -> c.getLevel() == playerEntity.getCitadelLevel())
                .map(Citadel::getPosition)
                .findAny()
                .orElse(null);
        if (citadelLocation == null) {
            log.info("No citadel is available");
            return;
        }

        NavigationUtil.switchToMapIfNeeded();

        NavigationUtil.zoomInIfNeeded();

        Point target = NavigationUtil.goToMapPosition(citadelLocation);
        
        robot.leftClick(target);

        Navigate titleElvenCitadel = Navigate.builder()
                .resourceName("player/watchtower/title_elven_citadel.png")
                .areaName(Area.TITLE_ELVEN_CITADEL)
                .waitLimit(5000)
                .build();
        
        if (!titleElvenCitadel.exist()) {
            log.info("Citadel disappeared");
            gameStateService.removeLocationAt(citadelLocation);
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }

        Transformation transform = Transformation.builder()
                .real(titleElvenCitadel.getPoint())
                .reference(Point.of(946, 451))
                .build();
        
        robot.leftClick(transform.transform(995, 738));
        robot.sleep(1500);

        boolean captainConfigured;
        switch(playerEntity.getCitadelLevel()) {
            case 10:
                captainConfigured = captainSelector.select(Captain.AYDAE, Captain.MINAMOTO, Captain.UNKNOW);
                break;
            case 15:
                captainConfigured = captainSelector.select(Captain.AYDAE, Captain.ALEXANDER, Captain.UNKNOW);
                break;
            default:
                throw new RuntimeException("Not implemented");
        }
        if (!captainConfigured) {
            log.info("It was not possible to configure the correct captains");
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;            
        }

        if (!checkMinimumRequirements()) {
            log.info("Minium group of hero and captains cannot be selected, maybe some of them are on a march");
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }
        
        int[] headCount = Backend.getHeadCount(robot);
        
        // System.out.println(headCount[0] + " - " + headCount[1] + " - " + headCount[2]);
        
        List<Unit> units = armyService.getUnits(playerEntity);
        Unit bestSiegeUnit = getBestSiegeUnit(units).orElse(null);
        if (bestSiegeUnit == null) {
            log.info("Player {} doesn't have a siege unit to attack a Citadel",  playerEntity.getPlayerName().name());
        }

        units = armyService.removeWeakPointForCitadel(units, playerEntity.getCitadelLevel());
        
        final int siegeQtd = armyService.getQtdSiegesForCitadel(playerEntity);
        
        
        ConfigurationBuilder builder = Configuration.builder()
                .leadership(headCount[0] - (bestSiegeUnit.getHeadCount() * siegeQtd))
                .dominance(headCount[1])
                .authority(headCount[2]);

        for (int i = 0; i < units.size(); i++) {
            builder.addUnit(units.get(i));
        }
        
        int[] qtd = builder.build().resolve();
        
        List<TroopQuantity> quantities = new ArrayList<>();
        quantities.add(TroopQuantity.builder()
                .unit(bestSiegeUnit)
                .quantity(siegeQtd)
                .build());
        
        for (int i = 0; i < qtd.length; i++) {
            quantities.add(TroopQuantity.builder()
                    .unit(units.get(i))
                    .quantity(qtd[i])
                    .build());
        }
        
        Backend.fillTroops(robot, quantities);

        Navigate buttonStartMarch = Navigate.builder()
                .resourceName("player/watchtower/button_start_march.png")
                .areaName(Area.POPUP_ENEMY_START_MARCH_BUTTON)
                .waitLimit(3000)
                .build();
        
        buttonStartMarch.leftClickIfExists();
        gameStateService.removeLocationAt(citadelLocation);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);

        NavigationUtil.speedUpMarch();

        playerPropertyService.clear(playerEntity, Scenario.BUILD_TROOPS_REEVALUATE);
        playerPropertyService.clear(playerEntity, Scenario.FINISHED_TRAINING_ALL_TROOPS);
        playerPropertyService.clear(playerEntity, Scenario.FINISHED_TRAINING_NON_MONSTERS);
    }
    
    private boolean checkMinimumRequirements() {
        Navigate heroLabel = Navigate.builder()
                .resourceName("player/watchtower/label_hero.png")
                .areaName(Area.POPUP_ENEMY_START_HERO_LABEL)
                .waitLimit(3000)
                .build().ensureExistence();
        
        Transformation transformation = Transformation.builder()
                .real(heroLabel.getPoint())
                .reference(Point.of(561, 418))
                .build();
        
        Area areas[] = new Area[3];
        areas[0] = transformation.transform(Point.of(579, 526),Point.of(605, 552));
        areas[1] = transformation.transform(Point.of(689, 526),Point.of(715, 552));
        areas[2] = transformation.transform(Point.of(799, 526),Point.of(824, 552));
        
        for (Area area : areas) {
            Navigate navigate = Navigate.builder()
                    .area(area)
                    .resourceName("player/watchtower/icon_checkmark.png")
                    .build();
            if (!navigate.exist()) {
                return false;
            }
        }
        return true;
    }
   
    private int[] hardcodeFirst(int[] qtd, int firstValue) {
        int[] answer = new int[qtd.length + 1];
        
        answer[0] = firstValue;
        
        for (int i = 0; i < qtd.length; i++) {
            answer[i +1] = qtd[i];
        }
        
        return answer;
    }
    
    private Optional<Unit> getBestSiegeUnit(List <Unit> units) {
        Unit best = null;
        
        for (Unit unit : units) {
            if (unit.getType() == UnitType.CATAPULT) {
                if ((best == null) || best.getTier() < unit.getTier()) {
                    best = unit;
                } 
            }            
        }
        
        return Optional.ofNullable(best);
    }
}
