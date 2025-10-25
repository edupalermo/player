package org.palermo.totalbattle.player;

import java.util.HashMap;
import java.util.Map;

public class PlayerRunnable implements Runnable {


    private static Map<String, Player> players = new HashMap<>();
    static {
        /*
        players.put("Palermo", Player.builder()
                .name("Palermo")
                .profileFolder("chrome-profiles/palermo")
                .username("fp2268@gmail.com")
                .password("Alemanha79")
                .build());                
         */
        players.put("Peter II", Player.builder()
                .name("Peter II")
                .profileFolder("chrome-profiles/peter_ii")
                .username("edupalermo@gmail.com")
                .password("Alemanha79")
                .build());
        players.put("Mightshaper", Player.builder()
                .name("Mightshaper")
                .profileFolder("chrome-profiles/mightshaper")
                .username("edupalermo+01@gmail.com")
                .password("Alemanha79")
                .build());
        players.put("Grirana", Player.builder()
                .name("Grirana")
                .profileFolder("chrome-profiles/grirana")
                .username("edupalermo+02@gmail.com")
                .password("Alemanha79")
                .build());
        players.put("Elanin", Player.builder()
                .name("Elanin")
                .profileFolder("chrome-profiles/elanin")
                .username("edupalermo+03@gmail.com")
                .password("Alemanha79")
                .build());
    }


    @Override
    public void run() {
        System.out.println("Player Thread running");
        
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
