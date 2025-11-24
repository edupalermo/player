package org.palermo.totalbattle.player;

import org.openqa.selenium.WebDriver;
import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.player.task.AttackArena;
import org.palermo.totalbattle.player.task.BuildArmy;
import org.palermo.totalbattle.player.task.CaptainSelector;
import org.palermo.totalbattle.player.task.ClanContribution;
import org.palermo.totalbattle.player.task.FreeSale;
import org.palermo.totalbattle.player.task.MineSilver;
import org.palermo.totalbattle.player.task.Quests;
import org.palermo.totalbattle.player.task.SummoningCircle;
import org.palermo.totalbattle.player.task.Telescope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerRunnable implements Runnable {
    
    private LockService lockService = new LockService();
    
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

            (new FreeSale(player)).freeSale();

            (new Quests(player)).evaluate();
            (new ClanContribution(player)).helpClanMembers();
            (new ClanContribution(player)).collectChests();

            (new Telescope(player)).findArena();
            (new Telescope(player)).findSilverMines();

            (new BuildArmy(player)).buildArmy();

            (new AttackArena(player)).attackArena();
            (new MineSilver(player)).mine();

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
