package org.palermo.totalbattle.player;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.task.BuildArmy;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.CdpUtil;
import org.palermo.totalbattle.util.ServerFacade;
import org.slf4j.MDC;

import java.io.IOException;

@Slf4j
public class PlayerRunnable implements Runnable {

    private ServerFacade serverFacade = new ServerFacade();

    @Override
    public void run() {
        log.info("Player Thread running");

        Player player = null;
        
        while (true) {
            try {
                player = serverFacade.startPlaying().orElse(null);
                if (player == null) {
                    log.warn("Couldn't retrieve a player to play, waiting 10 seconds");
                    Thread.sleep(10000);
                    continue;
                }
                play(player);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            finally {
                if (player != null) {
                    serverFacade.stopPlaying(player);
                }
            }
        }
    }
    
    
    private void play(Player player) {
        Process process = null;
        try {
            MDC.put("playerName", player.getName());

            log.info("Started new player");
            process = Task.openOrdinaryBrowser(player);

            SharedData.INSTANCE.robot.sleep(1500);
            // CdpUtil.closeAllTabsExceptOne();// This is probably not needed anymore!

            Task.login(player);
            
            (new BuildArmy(player)).buildArmy();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (process != null && process.isAlive()) {
                process.destroy();

                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    try {
                        Process killerProcess = new ProcessBuilder("powershell", "Stop-Process", "-Name", "chrome").start();
                        // Process killerProcess = new ProcessBuilder("taskkill", "/IM", "chrome.exe", "/F").start();
                        killerProcess.waitFor();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
