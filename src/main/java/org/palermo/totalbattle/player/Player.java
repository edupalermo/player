package org.palermo.totalbattle.player;

import lombok.Getter;
import org.palermo.totalbattle.selenium.leadership.Point;

import java.util.Optional;

@Getter
public enum Player {

    PALERMO(cfg("Palermo")
            .hasHelen(true)
            .miningLevel(25)
            .commonTarRequired(65800)
            .profileFolder("chrome-profiles/palermo")
            .username("fp2268@gmail.com")
            .priority(1)
            .position(380, 480)),
    
    PETER(cfg("Peter")
            .hasHelen(false)
            .miningLevel(20)
            .commonTarRequired(34000)
            .profileFolder("chrome-profiles/peter")
            .username("edupalermo@gmail.com")
            .priority(2)
            .position(381, 479)),

    MIGHTSHAPER(cfg("Mightshaper")
            .hasHelen(false)
            .miningLevel(20)
            .commonTarRequired(40000)
            .profileFolder("chrome-profiles/mightshaper")
            .username("edupalermo+01@gmail.com")
            .priority(3)
            .position(379, 481)),

    GRIRANA(cfg("Grirana")
            .hasHelen(false)
            .miningLevel(10)
            .commonTarRequired(3700)
            .profileFolder("chrome-profiles/grirana")
            .username("edupalermo+02@gmail.com")
            .priority(4)
            .position(381, 481)),

    ELANIN(cfg("Elanin")
            .hasHelen(false)
            .miningLevel(10)
            .commonTarRequired(3800)
            .profileFolder("chrome-profiles/elanin")
            .username("edupalermo+03@gmail.com")
            .priority(5)
            .position(379, 479)),

    LORVEN(cfg("Lorven")
            .hasHelen(false)
            .miningLevel(5)
            .profileFolder("chrome-profiles/lorven")
            .username("edupalermo+04@gmail.com")
            .priority(6)
            .position(351, 485));

    private String name;
    private boolean hasHelen;
    private int miningLevel;
    private int commonTarRequired;
    private String profileFolder;
    private String username;
    private String password;
    private int priority;
    private Point position;

    Player(Config cfg) {
        this.name = cfg.name;
        this.hasHelen = cfg.hasHelen;
        this.miningLevel = cfg.miningLevel;
        this.commonTarRequired = cfg.commonTarRequired;
        this.profileFolder = cfg.profileFolder;
        this.username = cfg.username;
        this.password = System.getenv("TOTAL_BATTLE_PASSWORD");
        this.priority = cfg.priority;
        this.position = cfg.position;
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

    public static Optional<Player> findPlayerByName(String name) {
        for (Player player : Player.values()) {
            if (player.name.equalsIgnoreCase(name)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    private static Config cfg(String name) {
        return new Config(name);
    }
    // simple builder-like helper used only at enum init
    private static class Config {
        private final String name;
        private boolean hasHelen;
        private int miningLevel;
        private int commonTarRequired;
        private String profileFolder;
        private String username;
        private int priority;
        private Point position;

        private Config(String name) {
            this.name = name;
        }

        Config hasHelen(boolean hasHelen) {
            this.hasHelen = hasHelen;
            return this;
        }

        Config miningLevel(int miningLevel) {
            this.miningLevel = miningLevel;
            return this;
        }

        Config profileFolder(String profileFolder) {
            this.profileFolder = profileFolder;
            return this;
        }

        Config username(String username) {
            this.username = username;
            return this;
        }

        Config priority(int priority) {
            this.priority = priority;
            return this;
        }

        Config position(int x, int y) {
            this.position = Point.of(x, y);
            return this;
        }
        
        Config commonTarRequired(int tarQtd) {
            this.commonTarRequired = tarQtd;
            return this;
        }
    }
}
