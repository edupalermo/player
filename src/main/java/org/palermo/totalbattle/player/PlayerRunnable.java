package org.palermo.totalbattle.player;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.palermo.totalbattle.entity.PlayerEntity;
import org.palermo.totalbattle.internalservice.GameStateService;
import org.palermo.totalbattle.internalservice.LockService;
import org.palermo.totalbattle.internalservice.PlayerStateService;
import org.palermo.totalbattle.player.task.*;
import org.palermo.totalbattle.service.player.PlayerService;
import org.palermo.totalbattle.util.CdpUtil;
import org.palermo.totalbattle.util.WhatsappUtil;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PlayerRunnable {

    private LockService lockService = new LockService();
    private GameStateService gameStateService = new GameStateService();
    private PlayerStateService playerStateService = new PlayerStateService();

    @Autowired
    private PlayerService playerService;

    @Autowired
    private InfoGather infoGather;

    private static List<PlayerName> playerNames = new ArrayList<>();
    static {
        playerNames.add(PlayerName.PALERMO);
        playerNames.add(PlayerName.PETER);
        playerNames.add(PlayerName.MIGHTSHAPER);
        playerNames.add(PlayerName.GRIRANA);
        playerNames.add(PlayerName.ELANIN);
    }

    public void run() {
        log.info("Player Thread running");

        while (true) {
            PlayerEntity playerEntity = null;
            try {
                playerEntity = playerService.findFreePlayerToPlay();
            } catch (RuntimeException e) {
                log.error(e.getMessage(), e);
            }
            finally {
                playerService.finishPlaying(playerEntity);
            }
        }
    }
    
    
    private void play(PlayerEntity playerEntity) {
        Process process = null;
        try {
            MDC.put("playerName", playerEntity.getPlayerName().name());
            process = Task.openOrdinaryBrowser(playerEntity);

            SharedData.INSTANCE.robot.sleep(1500);
            CdpUtil.closeAllTabsExceptOne();

            Task.login(playerEntity);
            

            /*
            if (SharedData.INSTANCE.shouldHalt(playerName)) {
                Task.showPauseDialog("Click on the button to continue");
                SharedData.INSTANCE.removeHalt(playerName);
            }
            */

            if ((new CheckHeroHealth(playerEntity)).isDead()) {
                WhatsappUtil.send(String.format("Player %s is dead", playerEntity.getPlayerName().name()));
                return;
            }

            infoGather.evaluate(playerEntity);
            
            /*
            (new FixBrokenArmor(playerName)).fix();

            (new FreeSale(playerName)).freeSale();

            (new Quests(playerName)).evaluate();
            (new ClanContribution(playerName)).helpClanMembers();
            (new ClanContribution(playerName)).collectChests();

            (new Telescope(playerName)).findArena();
            (new Telescope(playerName)).findSilverMines();
            (new Telescope(playerName)).findCitadels();
            (new Telescope(playerName)).findCrypts();

            (new BuildArmy(playerName)).buildArmy();

            (new AttackCitadel(playerName)).attack();
            (new AttackArena(playerName)).attack();
            (new MineSilver(playerName)).mine();
            (new ExploreCrypt(playerName)).explore();
            (new Donate(playerName)).evaluate();

            (new PayTaxes(playerName)).pay();
            
            (new SummoningCircle(SharedData.INSTANCE.robot, playerName)).evaluate();
             */

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
