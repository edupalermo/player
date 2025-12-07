package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.ArmyService;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.selenium.leadership.model.TroopQuantity;
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
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AttackCitadel {
    
    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    private PlayerStateService playerStateService = new PlayerStateService();
    private LockService lockService = new LockService();
    private GameStateService gameStateService = new GameStateService();
    private ArmyService armyService = new ArmyService();

    public AttackCitadel(Player player) {
        this.player = player;
    }
    
    public void attack() {
        /*
        gameStateService.add(
                Citadel.builder()
                        .level(10)
                        .position(Point.of(407, 503))
                        .build()
        );
         */
        
        Point citadelLocation = gameStateService
                .getLocation(Citadel.class)
                .filter((c) -> c.getLevel() == player.getCitadelLevel())
                .map(Citadel::getPosition)
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
        switch(player.getCitadelLevel()) {
            case 10:
                captainConfigured = (new CaptainSelector(player)).select(Captain.AYDAE, Captain.MINAMOTO, Captain.UNKNOW);
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

        int[] headCount = Backend.getHeadCount(robot);
        
        // System.out.println(headCount[0] + " - " + headCount[1] + " - " + headCount[2]);
        
        List<Unit> units = armyService.getUnits(player);
        units = armyService.removeWeakPointForCitadel(units, player.getCitadelLevel());
        
        final int siegeQtd = armyService.getQtdSiegesForCitadel(player, player.getCitadelLevel());
        
        ConfigurationBuilder builder = Configuration.builder()
                .leadership(headCount[0] - (player.getBestSiegeUnit().getHeadCount() * siegeQtd))
                .dominance(headCount[1])
                .authority(headCount[2]);

        for (int i = 0; i < units.size(); i++) {
            builder.addUnit(units.get(i));
        }
        
        int[] qtd = builder.build().resolve();
        
        List<TroopQuantity> quantities = new ArrayList<>();
        quantities.add(TroopQuantity.builder()
                .unit(player.getBestSiegeUnit())
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

        speedUpMarch();

        lockService.clear(player, Scenario.BUILD_TROOPS_REEVALUATE);
        lockService.clear(player, Scenario.FINISHED_TRAINING_ALL_TROOPS);
        lockService.clear(player, Scenario.FINISHED_TRAINING_NON_MONSTERS);
        armyService.setProductionOrder(player);
    }
    
    private void speedUpMarch() {

        Navigate.builder()
                .areaName(Area.MAIN_ONGOING_OPERATIONS)
                .resourceName("player/icon_expand_ongoing_operations.png")
                .build()
                .leftClickIfExists();

        Navigate march = Navigate.builder()
                .areaName(Area.MAIN_ONGOING_OPERATIONS)
                .resourceName("player/ongoing_tasks/label_march.png")
                .waitLimit(1000)
                .build();

        if (!march.exist()) {
            return;
        }
        
        // Clicar no SpeedUps
        robot.leftClick(march.getPoint().move(255, 8)); 
        robot.sleep(300);


        Navigate speedUpsTitle = Navigate.builder()
                .resourceName("player/speed_up/title_speed_ups.png")
                .area(Area.fromTwoPoints(910, 325, 1066, 361))
                .waitLimit(1000)
                .build();
        
        if (!speedUpsTitle.exist()) {
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
        }
        
        Transformation transformation = Transformation.builder()
                .real(speedUpsTitle.getPoint())
                .reference(Point.of(958, 346))
                .build();

        Navigate speedUp = Navigate.builder()
                .area(transformation.transform(Point.of(755, 483), Point.of(798, 638)))
                .resourceName("player/ongoing_tasks/speed_up_50_perc.png")
                .waitLimit(1000)
                .build();

        for (int i = 0; i < 5; i++) {
            if (speedUp.searchAgain().isPresent()) {
                robot.leftClick(speedUp.getPoint().move(402, 57));
                robot.sleep(300);
            }
            else {
                break;
            }
        }
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
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
