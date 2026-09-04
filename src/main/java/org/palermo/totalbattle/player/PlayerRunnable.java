package org.palermo.totalbattle.player;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.task.BuildArmy;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.server.model.FlagInfo;
import org.palermo.totalbattle.server.model.FlagScenario;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.CdpUtil;
import org.palermo.totalbattle.util.ServerFacade;
import org.palermo.totalbattle.util.SheetUtil;
import org.palermo.totalbattle.util.bean.ConfigurationMode;
import org.slf4j.MDC;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class PlayerRunnable implements Runnable {

    private static final MyRobot robot = MyRobot.INSTANCE;
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
                    // It could be only one service!
                    serverFacade.updatePlayer(player);
                    serverFacade.stopPlaying(player);
                }
            }
        }
    }

    public static String duration(FlagInfo flagInfo) {
        Duration duration = Duration.between(LocalDateTime.now(), flagInfo.getExpiration());

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(" d ");
        }

        if (hours > 0 || days > 0) {
            result.append(hours).append(" h ");
        }

        if (minutes > 0 || hours > 0 || days > 0) {
            result.append(minutes).append(" m ");
        }

        result.append(seconds).append(" s");

        return result.toString();
    }
    
    private void play(Player player) {
        Process process = null;
        try {
            MDC.put("playerName", player.getName());


            ConfigurationMode mode = SheetUtil.getConfiguration(SheetUtil.CONF_MODE, ConfigurationMode.class);
            if (mode == ConfigurationMode.BUILD_TROOPS) {
                FlagInfo flagInfo = player.getFlags().get(FlagScenario.SKIP_BUILDING_TROOPS);
                if (flagInfo != null && flagInfo.getExpiration().isAfter(LocalDateTime.now())) {
                    log.info("Skipped Mode is BUILD_TROOPS and SKIP_BUILDING_TROOPS: " + duration(flagInfo) + " " + flagInfo.getMessage());
                    return;
                }                
            }
            
            log.info("Started new player");
            process = Task.openOrdinaryBrowser(player);
            
            Task.login(player);
            
            (new BuildArmy(player)).buildArmy();

            // log.info("Waiting 120 seconds for no reason! :)");
            // robot.sleep(120000);
            
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
    
    private static void login() {
        if (CdpUtil.evaluate("""
                (() => {
                            const element = document.querySelector('span[data-id="login"]');
                
                            if (!element) {
                                return false;
                            }
                
                            element.click();
                            return true;
                        })()                    """)) {
            System.out.println("Clicked in the login button!");
        }

    }
}
