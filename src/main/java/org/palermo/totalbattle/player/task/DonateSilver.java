package org.palermo.totalbattle.player.task;

import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.message.SilverRequest;
import org.palermo.totalbattle.player.task.shared.NavigationUtil;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

public class DonateSilver {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    private static final GameStateService gameStateService = new GameStateService();
    private static final PlayerStateService playerStateService = new PlayerStateService();

    public DonateSilver(Player player) {
        this.player = player;
    }
    
    public void donate() {

        /*
        SilverRequest silverRequest = gameStateService.shouldDonateSilver(player).orElse(null);
        
        if (silverRequest == null) {
            return;
        }
         */

        SilverRequest silverRequest =   SilverRequest.builder()
                .target(Player.PALERMO)
                .expirationDate(LocalDateTime.now().plusHours(3))
                .build();

        NavigationUtil.switchToMapIfNeeded();
        
        NavigationUtil.zoomInIfNeeded();

        Point position = NavigationUtil.goToMapPosition(silverRequest.getTarget().getPosition())
                .move(0, -12);

        robot.leftClick(position);
        robot.sleep(300);
        
        Navigate playersCity = Navigate.builder()
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
        
        Area resourcesArea = Area.of(buttonStartMarch.getPoint(), Point.of(1090, 877), Point.of(786, 400), Point.of(885, 805));
        BufferedImage screen = robot.captureScreen();

        Navigate iconSilver = Navigate.builder()
                .resourceName("player/icon_silver.png")
                .area(resourcesArea)
                .build();
        
        robot.mouseDrag(Point.of(iconSilver.getPoint(), Point.of(816, 599), Point.of(907, 646)), 314, 0);
        robot.sleep(200);

        buttonStartMarch.leftClick();
        
        gameStateService.remove(silverRequest);
    }
}
