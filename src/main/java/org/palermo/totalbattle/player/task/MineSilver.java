package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.player.state.location.Mine;
import org.palermo.totalbattle.player.state.location.MineType;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Captain;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;

@Slf4j
public class MineSilver {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final PlayerName playerName;
    
    private final GameStateService gameStateService = new GameStateService();

    public MineSilver(PlayerName playerName) {
        this.playerName = playerName;
    }
    
    public void mine() {
        Mine mine = gameStateService.getMine(MineType.SILVER).orElse(null);
        if (mine == null) {
            log.info("No Mine available");
            return;
        }

        Navigate.builder()
                .areaName(Area.MAIN_ONGOING_OPERATIONS)
                .resourceName("player/icon_expand_ongoing_operations.png")
                .build()
                .leftClickIfExists();
        
        if (Navigate.builder()
                .areaName(Area.MAIN_ONGOING_OPERATIONS)
                .resourceName("player/ongoing_tasks/label_march_stror.png")
                .build()
                .exist()
            ||
                Navigate.builder()
                        .areaName(Area.MAIN_ONGOING_OPERATIONS)
                        .resourceName("player/ongoing_tasks/label_resource_gathering.png")
                        .build()
                        .exist()) {
            log.info("Stror is busy, he can't Mine");
            return;
        }

        NavigationUtil.switchToMapIfNeeded();

        NavigationUtil.zoomInIfNeeded();

        Point minePosition = NavigationUtil.goToMapPosition(mine.getPosition());

        /*
        if (NavigationUtil.belongsToAnotherClan(minePosition)) {
            log.info("Mine belong to another clan");
            gameStateService.removeLocationAt(mine.getPosition());

            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }
         */

        robot.leftClick(minePosition);

        Point titleVillagePoint = Navigate.builder()
                .resourceName("player/watchtower/title_village.png")
                .areaName(Area.TELESCOPE_VILLAGE_TITLE)
                .waitLimit(3000)
                .build()
                .search().orElse(null);
        
        // Silver mine is not available anymore
        if (titleVillagePoint == null) {
            log.info("Mine is not available anymore");
            gameStateService.removeLocationAt(mine.getPosition());

            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }
        
        Navigate buttonCapture = Navigate.builder()
                .resourceName("player/watchtower/button_capture.png")
                .areaName(Area.POPUP_SILVER_VILLAGE_CAPTURE_BUTTON)
                .build();
        
        if (!buttonCapture.exist()) {
            gameStateService.removeLocationAt(mine.getPosition()); // Not sure if I should remove

            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }
        
        buttonCapture.leftClick();

        Navigate buttonStartMarch = Navigate.builder()
                .resourceName("player/watchtower/button_start_march.png")
                .areaName(Area.POPUP_MINE_START_MARCH_BUTTON)
                .waitLimit(3000)
                .build();
        
        if (!buttonStartMarch.exist()) {
            gameStateService.removeLocationAt(mine.getPosition()); // Not sure if I should remove

            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }
        
        Navigate strorNavigate = Navigate.builder()
                .resourceName("player/captain/stror_72.png")
                .area(Area.of(buttonStartMarch.getPoint(), Point.of(1090, 877), Point.of(835, 433), Point.of(1145, 521)))
                .waitLimit(1000)
                .build();
        
        if (!strorNavigate.exist()) {
            (new CaptainSelector(playerName)).select(Captain.STROR);
            strorNavigate.searchAgain();
        }

        if (!strorNavigate.exist()) {
            log.info("It was not possible to find Stror");

            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }
        
        Point checkmarkPoint = strorNavigate.getPoint().move(27, 57);        

        Navigate iconCheckmark = Navigate.builder()
                .area(Area.of(checkmarkPoint.getX(), checkmarkPoint.getY(), 26, 26))
                .resourceName("player/icon_checkmark.png")
                .build();
        if (!iconCheckmark.exist()) {
            robot.leftClick(checkmarkPoint.move(13, 13));
            robot.sleep(300);
            iconCheckmark.searchAgain();
        }
        
        if (!iconCheckmark.exist()) {
            log.info("Stror is build and cannot Mine");
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }
        
        // Scroll to the bottom!
        
        robot.mouseDrag(Point.of(buttonStartMarch.getPoint(), Point.of(1090, 877), Point.of(1216, 426)), 0, 310);
        robot.sleep(1000);

        Navigate meleeUnit = Navigate.builder()
                .area(Area.of(buttonStartMarch.getPoint(), Point.of(1090, 877), Point.of(780, 400), Point.of(1090, 676)))
                .resourceName("leadership/melee_i.png")
                .waitLimit(1000)
                .build();
        
        if (!meleeUnit.exist()) {
            log.info("No Melee units available to Mine");
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }

        robot.leftClick(meleeUnit.getPoint().move(135, 44));
        robot.typeString("2000");
        robot.sleep(500);

        buttonStartMarch.leftClick();
        robot.sleep(500);

        gameStateService.removeLocationAt(mine.getPosition()); // Not sure if I should remove

        robot.type(KeyEvent.VK_ESCAPE); // Scape is not needed but I will click it anyway
        robot.sleep(300);
    }
}
