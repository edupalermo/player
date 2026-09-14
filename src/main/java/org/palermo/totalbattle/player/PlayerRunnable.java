package org.palermo.totalbattle.player;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.task.BuildArmy;
import org.palermo.totalbattle.player.task.ClanContribution;
import org.palermo.totalbattle.player.task.Quests;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.server.model.FlagInfo;
import org.palermo.totalbattle.server.model.FlagScenario;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.CdpUtil;
import org.palermo.totalbattle.util.FlagUtil;
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
                    try {
                        serverFacade.updatePlayer(player);
                        serverFacade.stopPlaying(player);
                    }
                    catch(Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }
    
    private void play(Player player) {
        Process process = null;
        try {
            MDC.put("playerName", player.getName());

            ConfigurationMode mode = SheetUtil.getConfiguration(SheetUtil.CONF_MODE, ConfigurationMode.class);
            if (mode == ConfigurationMode.BUILD_TROOPS) {
                if (FlagUtil.isActive(player, FlagScenario.SKIP_BUILDING_TROOPS)) {
                    FlagInfo flagInfo = player.getFlags().get(FlagScenario.SKIP_BUILDING_TROOPS);
                    FlagUtil.log(player, FlagScenario.SKIP_BUILDING_TROOPS);
                    return;
                }                
            }
            else {
                if (FlagUtil.isActive(player, FlagScenario.SKIP_BUILDING_TROOPS) &&
                        FlagUtil.isActive(player, FlagScenario.FREEZE_DAILY_JOB_EVALUATION)) {
                    FlagUtil.log(player, FlagScenario.SKIP_BUILDING_TROOPS);
                    FlagUtil.log(player, FlagScenario.FREEZE_DAILY_JOB_EVALUATION);
                    return;
                }
            }
            
            log.info("Started new player");
            process = Task.openOrdinaryBrowser(player);
            
            Task.login(player);

            if (mode == ConfigurationMode.NORMAL) {
                (new Quests(player)).evaluate();
            }

            (new BuildArmy(player)).buildArmy();

            (new ClanContribution(player)).helpClanMembers();
            (new ClanContribution(player)).collectChests();

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
