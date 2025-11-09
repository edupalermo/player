package org.palermo.totalbattle.player;

import org.openqa.selenium.WebDriver;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Unit;

import java.util.ArrayList;
import java.util.List;

public class PlayerRunnable implements Runnable {


    private static List<Player> players = new ArrayList<>();
    static {
        /*
        players.put("Palermo", Player.builder()
                .name("Palermo")
                .profileFolder("chrome-profiles/palermo")
                .username("fp2268@gmail.com")
                .password("Alemanha79")
                .build());
         */
        players.add(Player.builder()
                .name("Peter II")
                .profileFolder("chrome-profiles/peter_ii")
                .username("edupalermo@gmail.com")
                .password("Alemanha79")
                .build());
        players.add(Player.builder()
                .name("Mightshaper")
                .profileFolder("chrome-profiles/mightshaper")
                .username("edupalermo+01@gmail.com")
                .password("Alemanha79")
                .build());
        players.add(Player.builder()
                .name("Grirana")
                .profileFolder("chrome-profiles/grirana")
                .username("edupalermo+02@gmail.com")
                .password("Alemanha79")
                .build());
        players.add(Player.builder()
                .name("Elanin")
                .profileFolder("chrome-profiles/elanin")
                .username("edupalermo+03@gmail.com")
                .password("Alemanha79")
                .build());




        Player player = players.stream()
                .filter((p) -> p.getName().equals("Grirana"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Could not find player"));
        SharedData.INSTANCE.setTroopTarget(player, Unit.G3_MOUNTED, 522L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G3_RANGED, 1047L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G3_MELEE, 1047L);

        SharedData.INSTANCE.setTroopTarget(player, Unit.G2_MOUNTED, 933L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G2_RANGED, 1875L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G2_MELEE, 1875L);

        SharedData.INSTANCE.setTroopTarget(player, Unit.G1_MOUNTED, 1690L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G1_RANGED, 3384L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G1_MELEE, 3384L);

        player = players.stream()
                .filter((p) -> p.getName().equals("Elanin"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Could not find player"));
        SharedData.INSTANCE.setTroopTarget(player, Unit.G3_MOUNTED, 439L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G3_RANGED, 882L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G3_MELEE, 882L);

        SharedData.INSTANCE.setTroopTarget(player, Unit.G2_MOUNTED, 786L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G2_RANGED, 1579L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G2_MELEE, 1579L);

        SharedData.INSTANCE.setTroopTarget(player, Unit.G1_MOUNTED, 1423L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G1_RANGED, 2853L);
        SharedData.INSTANCE.setTroopTarget(player, Unit.G1_MELEE, 2856L);

    }


    @Override
    public void run() {
        System.out.println("Player Thread running");
        
        int counter = 0;
        
        while (true) {
            try {
                play(players.get(counter % players.size()));
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
            
            Point arenaLocation = SharedData.INSTANCE.getArena().orElse(null);
            if (arenaLocation != null) {
                if (!Task.attackArena(arenaLocation)) {
                    SharedData.INSTANCE.removeArena(arenaLocation);
                }
            }
            if (!SharedData.INSTANCE.shouldWait(player, Scenario.BONUS_SALES_FREE)) {
                Task.freeSale(player);
            }
            Task.quests();
            Task.collectChests();
            Task.helpClanMembers();

            if (SharedData.INSTANCE.hasTroopBuildPlan(player)) {
                Task.buildArmy(player);
            }
            

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (driver != null) {
                driver.quit();
            }
        }

    }
}
