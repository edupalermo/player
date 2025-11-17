package org.palermo.totalbattle.player;

import org.palermo.totalbattle.player.bean.UnitQuantity;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Unit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum SharedData {

    INSTANCE;

    private final List<Point> arenas = new ArrayList<Point>();

    // Prevent the user to be played automatically
    private final Set<String> lock = new HashSet<>();

    // Prevent the user to be played automatically
    private final Set<String> halt = new HashSet<>();

    // Store scenarios when the user needs to wait for the activity
    private final Map<String, Map<Scenario, LocalDateTime>> wait = new HashMap<>();

    // Troop training target
    private final Map<String, Map<Unit, Long>> troopTarget = new HashMap<>();
    
    public final MyRobot robot = MyRobot.INSTANCE; 

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

    public void halt(Player player) {
        halt.add(player.getName());
    }

    public boolean shouldHalt(Player player) {
        return halt.contains(player.getName());
    }

    public boolean removeHalt(Player player) {
        return halt.remove(player.getName());
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
        Map<Scenario, LocalDateTime> map = this.wait.get(player.getName());
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

    public boolean shouldWaitForSummoningCircle(Player player) {
        return shouldWait(player, Scenario.SUMMONING_CIRCLE_ARTIFACT_FRAGMENT) &&
                shouldWait(player, Scenario.SUMMONING_CIRCLE_COMMON_CAPTAIN_FRAGMENT)  &&
                shouldWait(player, Scenario.SUMMONING_CIRCLE_ELITE_CAPTAIN_FRAGMENT);
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
        // The Hero
        setArmy(Player.PALERMO, 3, 35689, 8922, 17844);
        setArmy(Player.PETER_II, 3, 13893, 3460, 6961);
        setArmy(Player.MIGHTSHAPER, 3, 12532, 3116, 6191);
        setArmy(Player.GRIRANA, 3, 5477, 1353, 2680);
        setArmy(Player.ELANIN, 3, 4700, 1140, 2280);
        
        /* One Captain
        setArmy(Player.PALERMO, 3, 26532, 6621, 13148);
        setArmy(Player.PETER_II, 3, 13757, 3548, 7049);
        setArmy(Player.MIGHTSHAPER, 3, 12805, 3259, 6431);
        setArmy(Player.GRIRANA, 3, 3592, 878, 1740);
        setArmy(Player.ELANIN, 3, 3475, 850, 1700);
         */
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

        List<UnitQuantity> unitQuantities = new ArrayList();
        for (int i = 0; i < qtds.length; i++) {
            unitQuantities.add(UnitQuantity.builder()
                    .unit(units.get(i))
                    .quantity(computeWaves(qtds[i], waves))
                    .build());
        }

        unitQuantities = addMiners(unitQuantities);

        unitQuantities = incrementLastLayer(unitQuantities, player);

        for (UnitQuantity unitQuantity: unitQuantities) {
            setTroopTarget(player, unitQuantity.getUnit(), unitQuantity.getQuantity());
        }
    }
    
    private List<UnitQuantity> addMiners(List<UnitQuantity> input) {
        List<UnitQuantity> output = new ArrayList<>();
        boolean found = false;
        for (UnitQuantity unitQuantity: input) {
            if (unitQuantity.getUnit() == Unit.G1_MELEE) {
                output.add(unitQuantity.withQuantity(unitQuantity.getQuantity() + 3500));
                found = true;
            }
            else {
                output.add(unitQuantity);
            }
        }
        
        if (!found) {
            output.add(UnitQuantity.builder()
                    .unit(Unit.G1_MELEE)
                    .quantity(3500).build());
        }
        return output;
    }

    private List<UnitQuantity> incrementLastLayer(List<UnitQuantity> input, Player player) {
        
        List<UnitQuantity> output = input;
        
        switch (player.getName()) {
            case Player.PALERMO:
                output = increase(output, Unit.G5_MOUNTED, 4000);
                output = increase(output, Unit.G5_RANGED, 8000);
                output = increase(output, Unit.G5_MELEE, 8000);
                output = increase(output, Unit.G5_GRIFFIN, 400);
                break;
            case Player.PETER_II, Player.MIGHTSHAPER:
                output = increase(output, Unit.G4_MOUNTED, 4000);
                output = increase(output, Unit.G4_RANGED, 8000);
                output = increase(output, Unit.G4_MELEE, 8000);
                break;
            case Player.GRIRANA, Player.ELANIN:
                output = increase(output, Unit.G3_MOUNTED, 2000);
                output = increase(output, Unit.G3_RANGED, 4000);
                output = increase(output, Unit.G3_MELEE, 4000);
                break;

            default:
                throw new RuntimeException("Not Implemented");
        }
        
        return output; 
    }
    
    private List<UnitQuantity> increase(List<UnitQuantity> input, Unit unit, int qtd) {
        List<UnitQuantity> output = new ArrayList<>();
        boolean found = false;
        for (UnitQuantity unitQuantity: input) {
            if (unitQuantity.getUnit() == unit) {
                output.add(unitQuantity.withQuantity(unitQuantity.getQuantity() + qtd));
                found = true;
            }
            else {
                output.add(unitQuantity);
            }
        }

        if (!found) {
            output.add(UnitQuantity.builder()
                    .unit(unit)
                    .quantity(qtd).build());
        }
        return output;

    }


    private List<Unit> getUnits(String name) {
        
        List<Unit> units = new ArrayList<>();
        
        switch (name) {
            case Player.PALERMO:
                
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

            case Player.PETER_II, Player.MIGHTSHAPER:
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
            case Player.GRIRANA, Player.ELANIN:
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
