package org.palermo.totalbattle.player;

import com.google.common.collect.ImmutableSet;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Attribute;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Pool;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.selenium.stacking.UnitType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum SharedData {

    private static final String PALERMO = "Palermo";
    private static final String PETER_II = "Peter II";
    private static final String MIGHTSHAPER = "Mightshaper";
    private static final String GRIRANA = "Grirana";
    private static final String ELANIN = "Elanin";

    INSTANCE;

    private final List<Point> arenas = new ArrayList<Point>();

    // Prevent the user to be played automatically
    private final Set<String> lock = new HashSet<>();

    // Store scenarios when the user needs to wait for the activity
    private final Map<String, Map<Scenario, LocalDateTime>> wait = new HashMap<>();

    // Troop training target
    private final Map<String, Map<Unit, Long>> troopTarget = new HashMap<>();

    public void addArena(Point point) {
        this.arenas.add(point);
    }

    public Optional<Point> getArena() {
        if (arenas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.arenas.get(0));
    }

    public void removeArena(Point point) {
        arenas.remove(point);
    }

    public void lock(String playerName) {
        lock.add(playerName);
    }

    public void clearLock() {
        lock.clear();
    }

    public Set<String> getLock() {
        return lock;
    }
    
    public boolean isLocked(Player player) {
        return lock.contains(player.getName());        
    }

    public void setWait(Player player, Scenario scenario, LocalDateTime dateTime) {
        Map<Scenario, LocalDateTime> map = wait.computeIfAbsent(player.getName(), (k) -> new HashMap<>());
        map.put(scenario, dateTime);
    }

    public Optional<LocalDateTime> getWait(Player player, Scenario scenario) {
        return Optional.ofNullable(wait
                .computeIfAbsent(player.getName(), (k) -> new HashMap<>())
                .get(scenario));
    }

    public boolean shouldWait(Player player, Scenario scenario) {
        LocalDateTime waitUntil = getWait(player, scenario).orElse(null);
        if (waitUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(waitUntil);
    }

    public Map<Unit, Long> getTroopTarget(Player player) {
        return troopTarget.get(player.getName());
    }
    
    public boolean hasTroopBuildPlan(Player player) {
        return troopTarget.get(player.getName()) != null;
    }

    public void setTroopTarget(Player player, Unit unit, Long quantity) {
        Map<Unit, Long> map = troopTarget.computeIfAbsent(player.getName(), (k) -> new HashMap<>());
        map.put(unit, quantity);
    }

    {
        setArmy(PALERMO, 3, 25828, 6457, 12914);
        setArmy(PETER_II, 3, 13158, 3353, 6706);
        setArmy(MIGHTSHAPER, 3, 12258, 3073, 6125);
        setArmy(GRIRANA, 3, 2625, 636, 1260);
        setArmy(ELANIN, 3, 2725, 670, 1340);
    }
    
    private void setArmy(String name, int waves, int leadership, int dominance, int authority) {
        Player player = Player.builder()
                .name(name)
                .build();

        List<Unit> units = getUnits(name);

        ConfigurationBuilder builder = Configuration.builder()
                .leadership(leadership)
                .dominance(dominance)
                .authority(authority)
                .wave(3);

        for (Unit unit: units) {
            builder.addUnit(unit);
        }

        int[] qtds = builder.build().resolve();

        for (int i = 0; i < qtds.length; i++) {
            setTroopTarget(player, units.get(i), (long) computeWaves(qtds[i], 3));
        }
    }
    
    private List<Unit> getUnits(String name) {
        
        List<Unit> units = new ArrayList<>();
        
        switch (name) {
            case PALERMO:
                units.add(Unit.S3_SWORDSMAN);
                units.add(Unit.G3_RANGED);
                units.add(Unit.G3_MELEE);
                units.add(Unit.G3_MOUNTED);

                units.add(Unit.S4_SWORDSMAN);
                units.add(Unit.G4_RANGED);
                units.add(Unit.G4_MELEE);
                units.add(Unit.G4_MOUNTED);
                
                units.add(Unit.G5_RANGED);
                units.add(Unit.G5_MELEE);
                units.add(Unit.G5_MOUNTED);
                units.add(Unit.G5_GRIFFIN);

                units.add(Unit.EMERALD_DRAGON);
                units.add(Unit.WATER_ELEMENTAL);
                units.add(Unit.STONE_GARGOYLE);
                units.add(Unit.BATTLE_BOAR);

                units.add(Unit.MAGIC_DRAGON);
                units.add(Unit.ICE_PHOENIX);
                units.add(Unit.MANY_ARMED_GUARDIAN);
                units.add(Unit.GORGON_MEDUSA);

                units.add(Unit.DESERT_VANQUISER);
                units.add(Unit.FLAMING_CENTAUR);
                units.add(Unit.ETTIN);
                units.add(Unit.FEARSOME_MANTICORE);
                break;

            case PETER_II, MIGHTSHAPER:
                units.add(Unit.S2_SWORDSMAN);
                units.add(Unit.G2_RANGED);
                units.add(Unit.G2_MELEE);
                units.add(Unit.G2_MOUNTED);

                units.add(Unit.G3_RANGED);
                units.add(Unit.G3_MELEE);
                units.add(Unit.G3_MOUNTED);

                units.add(Unit.G4_RANGED);
                units.add(Unit.G4_MELEE);
                units.add(Unit.G4_MOUNTED);

                units.add(Unit.EMERALD_DRAGON);
                units.add(Unit.WATER_ELEMENTAL);
                units.add(Unit.STONE_GARGOYLE);
                units.add(Unit.BATTLE_BOAR);

                units.add(Unit.MAGIC_DRAGON);
                units.add(Unit.ICE_PHOENIX);
                units.add(Unit.MANY_ARMED_GUARDIAN);
                units.add(Unit.GORGON_MEDUSA);
                break;
            case GRIRANA, ELANIN:
                units.add(Unit.S1_SWORDSMAN);
                units.add(Unit.G1_RANGED);
                units.add(Unit.G1_MELEE);
                units.add(Unit.G1_MOUNTED);

                units.add(Unit.G2_RANGED);
                units.add(Unit.G2_MELEE);
                units.add(Unit.G2_MOUNTED);

                units.add(Unit.G3_RANGED);
                units.add(Unit.G3_MELEE);
                units.add(Unit.G3_MOUNTED);

                units.add(Unit.EMERALD_DRAGON);
                units.add(Unit.WATER_ELEMENTAL);
                units.add(Unit.STONE_GARGOYLE);
                units.add(Unit.BATTLE_BOAR);
                break;
            default:
                throw new RuntimeException("Not Implemented for " + name);
        }
        
        return units;
    }

    private int computeWaves(int quantity, int wave) {
        double factor = 0;

        for (int i = 0; i < wave; i++) {
            factor += Math.pow(1.06, i);
        }

        return (int) Math.round(quantity * factor);
    }
    
}
