package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.message.SilverRequest;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.player.task.shared.Resource;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class Donate {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    private static final GameStateService gameStateService = new GameStateService();
    private static final PlayerStateService playerStateService = new PlayerStateService();

    public Donate(Player player) {
        this.player = player;
    }
    
    public void donate() {
        
        Player target = playerInNeed().orElse(null);
        if (target == null) {
            log.info("No other player needs help");
            return;
        }
        
        Resource resource = selectResourceToDonate(target);
        resource = Resource.SILVER;
        log.info("Trying to donate {} to {}", resource.name(), target.name());

        NavigationUtil.switchToMapIfNeeded();
        NavigationUtil.zoomInIfNeeded();

        Point position = NavigationUtil.goToMapPosition(target.getPosition());

        robot.leftClick(position);
        robot.sleep(300);
        
        Navigate.builder()
                .resourceName("player/friend/title_players_city.png")
                .areaName(Area.PLAYERS_CITY_TITLE)
                .waitLimit(3000)
                .build().ensureExistence();

        Navigate.builder()
                .resourceName("player/friend/button_caravan.png")
                .areaName(Area.PLAYERS_CITY_CARAVAN_BUTTON)
                .waitLimit(3000)
                .build()
                .leftClick();

        Navigate buttonStartMarch = Navigate.builder()
                .resourceName("player/watchtower/button_start_march.png")
                .areaName(Area.POPUP_MINE_START_MARCH_BUTTON)
                .waitLimit(3000)
                .build()
                .ensureExistence();
        
        Transformation transformation = Transformation.builder()
                .real(buttonStartMarch.getPoint())
                .reference(Point.of(1090, 877))
                .build();
        
        Navigate icon = Navigate.builder()
                .resourceName(resource.getResource())
                .area(transformation.transform(Point.of(786, 400), Point.of(885, 805)))
                .waitLimit(1000)
                .build();
        
        int verticalScroll = 215;
        
        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                Point lastPosition = transformation.transform(buttonStartMarch.getPoint()).move(126,-452).move(0, (i - 1) * verticalScroll);
                robot.leftClick(lastPosition);
                robot.mouseDrag(lastPosition, 0, i * verticalScroll);
            }

            if (icon.searchAgain().isPresent()) {
                break;
            }
        }
        
        if (!icon.exist()) {
            log.info("Resource not found!");
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            
            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);
            return;
        }
        
        robot.mouseDrag(icon.getPoint().move(88, 36), 314, 0);
        robot.sleep(200);

        buttonStartMarch.leftClick();

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }

    private Optional<Player> playerInNeed() {
        Player answer = null;
        for (Player it: Player.values()) {
            if (it.getPriority() >= player.getPriority()) {
                continue;
            }
            PlayerState playerState = playerStateService.getState(it);
            if (playerState != null && playerState.getResourcesTarget() != null) {
                if (playerState.getResourcesTarget().getLumber() > playerState.getLumber() ||
                        playerState.getResourcesTarget().getIron() > playerState.getIron() ||
                        playerState.getResourcesTarget().getStone() > playerState.getStone()) {
                    if (answer == null || it.getPriority() < answer.getPriority()) {
                        answer = it;
                    }
                }
            }
        }
        return Optional.ofNullable(answer);
    }

    private Resource selectResourceToDonate(Player it) {
        List<Resource> resources = new ArrayList<>(); 
        PlayerState playerState = playerStateService.getState(it);
        if (playerState.getResourcesTarget().getLumber() > playerState.getLumber()) {
            resources.add(Resource.LUMBER);
        }
        if (playerState.getResourcesTarget().getIron() > playerState.getIron()) {
            resources.add(Resource.IRON);
        }
        if (playerState.getResourcesTarget().getStone() > playerState.getStone()) {
            resources.add(Resource.STONE);
        }

        return resources.get(ThreadLocalRandom.current().nextInt(resources.size()));
    }
}
