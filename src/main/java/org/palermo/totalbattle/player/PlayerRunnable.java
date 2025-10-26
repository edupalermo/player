package org.palermo.totalbattle.player;

import org.openqa.selenium.WebDriver;
import org.palermo.totalbattle.selenium.leadership.Point;

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
    }


    @Override
    public void run() {
        System.out.println("Player Thread running");
        
        int counter = 0;
        
        while (true) {
            try {
                play(players.get(counter % players.size()));
                counter++;
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
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
            Task.helpClanMembers();
            Task.collectChests();

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
