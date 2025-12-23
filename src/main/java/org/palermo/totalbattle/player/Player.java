package org.palermo.totalbattle.player;

import lombok.Getter;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Unit;

import java.util.Optional;

@Getter
public enum Player {

    PALERMO(cfg("Palermo")
            .hasHelen(true)
            .commonCryptLevel(25)
            .commonTarRequired(65800)
            .citadelLevel(20)
            .profileFolder("chrome-profiles/palermo")
            .username("fp2268@gmail.com")
            .priority(1)
            .position(380, 480)
            .bestSiegeUnit(Unit.EC5_ENGINEER)),
    
    PETER(cfg("Peter")
            .hasHelen(false)
            .commonCryptLevel(20)
            .commonTarRequired(34000)
            .citadelLevel(15)
            .profileFolder("chrome-profiles/peter")
            .username("edupalermo@gmail.com")
            .priority(2)
            .position(381, 479)
            .bestSiegeUnit(Unit.EC4_ENGINEER)),

    MIGHTSHAPER(cfg("Mightshaper")
            .hasHelen(false)
            .commonCryptLevel(20)
            .commonTarRequired(40000)
            .citadelLevel(15)
            .profileFolder("chrome-profiles/mightshaper")
            .username("edupalermo+01@gmail.com")
            .priority(3)
            .position(379, 481)
            .bestSiegeUnit(Unit.EC4_ENGINEER)),

    GRIRANA(cfg("Grirana")
            .hasHelen(false)
            .commonCryptLevel(15)
            .commonTarRequired(13400)
            .citadelLevel(15)
            .profileFolder("chrome-profiles/grirana")
            .username("edupalermo+02@gmail.com")
            .priority(4)
            .position(381, 481)
            .bestSiegeUnit(Unit.EC3_ENGINEER)),

    ELANIN(cfg("Elanin")
            .hasHelen(false)
            .commonCryptLevel(10)
            .commonTarRequired(3800)
            .citadelLevel(10)
            .profileFolder("chrome-profiles/elanin")
            .username("edupalermo+03@gmail.com")
            .priority(5)
            .position(379, 479)
            .bestSiegeUnit(Unit.EC2_ENGINEER)),

    LORVEN(cfg("Lorven")
            .hasHelen(false)
            .commonCryptLevel(5)
            .citadelLevel(10)
            .profileFolder("chrome-profiles/lorven")
            .username("edupalermo+04@gmail.com")
            .priority(6)
            .position(351, 485)
            .bestSiegeUnit(Unit.EC1_ENGINEER));

    private String name;
    private boolean hasHelen;
    private int citadelLevel;
    private int commonCryptLevel;
    private int commonTarRequired;
    private String profileFolder;
    private String username;
    private String password;
    private int priority;
    private Point position;
    private Unit bestSiegeUnit;

    Player(Config cfg) {
        this.name = cfg.name;
        this.hasHelen = cfg.hasHelen;
        this.commonCryptLevel = cfg.commonCryptLevel;
        this.commonTarRequired = cfg.commonTarRequired;
        this.profileFolder = cfg.profileFolder;
        this.username = cfg.username;
        this.password = System.getenv("TOTAL_BATTLE_PASSWORD");
        this.priority = cfg.priority;
        this.position = cfg.position;
        this.citadelLevel = cfg.citadelLevel;
        this.bestSiegeUnit = cfg.bestSiegeUnit;
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
        private int commonCryptLevel;
        private int citadelLevel;
        private int commonTarRequired;
        private String profileFolder;
        private String username;
        private int priority;
        private Point position;
        private Unit bestSiegeUnit;

        private Config(String name) {
            this.name = name;
        }

        Config hasHelen(boolean hasHelen) {
            this.hasHelen = hasHelen;
            return this;
        }

        Config commonCryptLevel(int commonCryptLevel) {
            this.commonCryptLevel = commonCryptLevel;
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

        Config citadelLevel(int level) {
            this.citadelLevel = level;
            return this;
        }
        
        Config bestSiegeUnit(Unit unit) {
            this.bestSiegeUnit = unit;
            return this;
        }
    }
}
