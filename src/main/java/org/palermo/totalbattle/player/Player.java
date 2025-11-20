package org.palermo.totalbattle.player;

import lombok.Getter;

@Getter
public enum Player {

    PALERMO("Palermo", true, 25, "chrome-profiles/palermo", "fp2268@gmail.com"),
    PETER("Peter", false, 20, "chrome-profiles/peter", "edupalermo@gmail.com"),
    MIGHTSHAPER("Mightshaper", false, 20, "chrome-profiles/mightshaper", "edupalermo+01@gmail.com"),
    GRIRANA("Grirana", false, 10, "chrome-profiles/grirana", "edupalermo+02@gmail.com"),
    ELANIN("Elanin", false, 10, "chrome-profiles/elanin", "edupalermo+03@gmail.com"),
    LORVEN("Lorven", false, 5, "chrome-profiles/lorven", "edupalermo+04@gmail.com");

    private String name;
    private boolean hasHelen;
    private int miningLevel;
    private String profileFolder;
    private String username;
    private String password;
    
    Player(String name, boolean hasHelen, int mineLevel, String profileFolder, String username) {
        this.name = name;
        this.hasHelen = hasHelen;
        this.miningLevel = mineLevel;
        this.profileFolder = profileFolder;
        this.username = username;
        this.password = System.getenv("TOTAL_BATTLE_PASSWORD");
    }
    
    boolean hasHelen() {
        return hasHelen;
    }
    
    public static Player getPlayerByName(String name) {
        for (Player player : Player.values()) {
            if (player.name.equalsIgnoreCase(name)) {
                return player;
            }
        }
        throw new RuntimeException("Cannot find player with name " + name);
    }
}
