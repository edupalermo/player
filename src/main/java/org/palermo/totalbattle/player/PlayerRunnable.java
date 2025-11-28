package org.palermo.totalbattle.player;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.player.task.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.palermo.totalbattle.internalservice.GameStateService.PROPERTY_NEXT;

public class PlayerRunnable implements Runnable {

    private LockService lockService = new LockService();
    private GameStateService gameStateService = new GameStateService();
    
    private static List<Player> players = new ArrayList<>();
    static {
        players.add(Player.PALERMO);
        players.add(Player.PETER);
        players.add(Player.MIGHTSHAPER);
        players.add(Player.GRIRANA);
        players.add(Player.ELANIN);
    }

    @Override
    public void run() {
        System.out.println("Player Thread running");
        
        int counter = 0;
        
        while (true) {
            try {
                Player player = players.get(counter % players.size());
                if (!SharedData.INSTANCE.isLocked(player)) {
                    play(player);
                }

                String playerName = gameStateService.getProperty(GameStateService.PROPERTY_NEXT);
                if (StringUtils.isNoneBlank(playerName)) {
                    Player adHocPlayer = Player.findPlayerByName(playerName).orElse(null);
                    if (adHocPlayer != null) {
                        play(adHocPlayer);
                        gameStateService.removeProperty(GameStateService.PROPERTY_NEXT);
                    }
                }
                    
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            counter++;
        }
    }
    
    
    private void play(Player player) {
        WebDriver driver = null;
        try {
            driver = Task.openBrowser(player);
            Task.login(player);

            if (SharedData.INSTANCE.shouldHalt(player)) {
                Task.showPauseDialog("Click on the button to continue");
                SharedData.INSTANCE.removeHalt(player);
            }

            (new CaptainSelector(player)).updatePlayerState();
            (new InfoGather(player)).evaluate();

            (new FreeSale(player)).freeSale();

            (new Quests(player)).evaluate();
            (new ClanContribution(player)).helpClanMembers();
            (new ClanContribution(player)).collectChests();

            (new Telescope(player)).findArena();
            (new Telescope(player)).findSilverMines();

            (new BuildArmy(player)).buildArmy();

            (new AttackArena(player)).attackArena();
            
            (new MineSilver(player)).mine();

            (new DonateSilver(player)).donate();

            if (!isSummoningCircleFree(player)) {
                (new SummoningCircle(SharedData.INSTANCE.robot, player)).evaluate();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (driver != null) {
                driver.quit();

                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    try {
                        new ProcessBuilder("taskkill", "/IM", "chrome.exe", "/F").start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                // new ProcessBuilder("pkill", "chrome").start();
            }
        }

    }

    public boolean isSummoningCircleFree(Player player) {
        return lockService.isLocked(player, Scenario.SUMMONING_CIRCLE_ARTIFACT_FRAGMENT) &&
                lockService.isLocked(player, Scenario.SUMMONING_CIRCLE_COMMON_CAPTAIN_FRAGMENT)  &&
                lockService.isLocked(player, Scenario.SUMMONING_CIRCLE_ELITE_CAPTAIN_FRAGMENT);
    }
}
