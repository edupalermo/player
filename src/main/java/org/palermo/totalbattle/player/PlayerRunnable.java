package org.palermo.totalbattle.player;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.task.*;
import org.palermo.totalbattle.util.CdpUtil;
import org.palermo.totalbattle.util.WhatsappUtil;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PlayerRunnable implements Runnable {

    private LockService lockService = new LockService();
    private GameStateService gameStateService = new GameStateService();
    private PlayerStateService playerStateService = new PlayerStateService();
    
    private final static boolean BUILD_ARMY = true;
    
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
        log.info("Player Thread running");

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
                log.error(e.getMessage(), e);
            }
            counter++;
        }
    }
    
    
    private void play(Player player) {
        Process process = null;
        try {
            MDC.put("playerName", player.getName());
            process = Task.openOrdinaryBrowser(player);

            SharedData.INSTANCE.robot.sleep(1500);
            CdpUtil.closeAllTabsExceptOne();

            Task.login(player);
            

            if (SharedData.INSTANCE.shouldHalt(player)) {
                Task.showPauseDialog("Click on the button to continue");
                SharedData.INSTANCE.removeHalt(player);
            }

            if ((new CheckHeroHealth(player)).isDead()) {
                WhatsappUtil.send(String.format("Player %s is dead", player.name()));
                return;
            }

            (new InfoGather(player)).evaluate();
            (new FixBrokenArmor(player)).fix();
            
            (new FreeSale(player)).freeSale();
            (new Quests(player)).evaluate();
            (new ClanContribution(player)).helpClanMembers();

            if (!BUILD_ARMY) {
                (new ClanContribution(player)).collectChests();

                (new Telescope(player)).findArena();
                (new Telescope(player)).findSilverMines();
                (new Telescope(player)).findCitadels();
                (new Telescope(player)).findCrypts();
            }

            (new BuildArmy(player)).buildArmy();

            if (!BUILD_ARMY) {
                (new AttackCitadel(player)).attack();
                (new AttackArena(player)).attack();
                (new MineSilver(player)).mine();
                (new ExploreCrypt(player)).explore();
            }
            
            (new Donate(player)).evaluate();

            if (!BUILD_ARMY) {
                (new PayTaxes(player)).pay();
                (new SummoningCircle(SharedData.INSTANCE.robot, player)).evaluate();
            }

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
