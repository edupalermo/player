package org.palermo.totalbattle.player;

import org.openqa.selenium.WebDriver;
import org.palermo.totalbattle.player.task.AttackArena;
import org.palermo.totalbattle.player.task.BuildArmy;
import org.palermo.totalbattle.player.task.CaptainSelector;
import org.palermo.totalbattle.player.task.ClanContribution;
import org.palermo.totalbattle.player.task.FreeSale;
import org.palermo.totalbattle.player.task.SummoningCircle;
import org.palermo.totalbattle.player.task.Telescope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerRunnable implements Runnable {
    
    private static List<Player> players = new ArrayList<>();
    static {
        players.add(Player.PALERMO);
        players.add(Player.PETER);
        /*
        players.add(Player.MIGHTSHAPER);
        players.add(Player.GRIRANA);
        players.add(Player.ELANIN);
         */
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

            (new AttackArena(player)).attackArena();

            (new FreeSale(player)).freeSale();
            
            Task.quests(player);
            (new ClanContribution(player)).helpClanMembers();
            (new ClanContribution(player)).collectChests();

            (new Telescope(player)).findArena();

            (new BuildArmy(player)).buildArmy();
            
            if (!SharedData.INSTANCE.shouldWaitForSummoningCircle(player)) {
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
}
