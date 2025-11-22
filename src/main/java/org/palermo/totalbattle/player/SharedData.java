package org.palermo.totalbattle.player;

import org.palermo.totalbattle.player.state.AutomationState;
import org.palermo.totalbattle.player.state.TroopQuantity;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Pool;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.util.IoUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    private final File AUTOMATION_STATE_FILE = new File("automation_state.json");
    private AutomationState automationState = IoUtil.readJson(AUTOMATION_STATE_FILE, AutomationState.class);
    
    public void addArena(Point point) {
        this.arenas.add(point);
    }

    public Optional<Point> getArena() {
        if (arenas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.arenas.get(0));
    }

    public AutomationState getAutomationState() {
        return this.automationState;
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
        return lock.contains(player.getName());        
    }

    public void setWait(Player player, Scenario scenario, LocalDateTime dateTime) {
        Map<Scenario, LocalDateTime> map = wait.computeIfAbsent(player.getName(), (k) -> new HashMap<>());
        map.put(scenario, dateTime);
    }
    
    public void clearWait(Player player, Scenario scenario) {
        Map<Scenario, LocalDateTime> map = wait.computeIfAbsent(player.getName(), (k) -> new HashMap<>());
        map.remove(scenario);
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
    
    final Comparator<TroopQuantity> UNIT_QUANTITY_COMPARATOR = (u1, u2) -> {
        if (u1.getUnit().getPool() != u2.getUnit().getPool()) { // LEADERSHIP should go first
            return u1.getUnit().getPool() == Pool.LEADERSHIP ? -1 : 1;
        }
        if (u1.getUnit().getTier() != u2.getUnit().getTier()) { // Higher tier should go first
            return u2.getUnit().getTier() - u1.getUnit().getTier();
        }
        int result = (int) ((u2.getTarget() * u2.getUnit().getHeadCount()) // Troops with bit gap should go first
                - (u1.getTarget() * u1.getUnit().getHeadCount()));

        if (result != 0) {
            return result;
        }

        return u1.getUnit().name().compareToIgnoreCase(u2.getUnit().name()); // User anything...
    };
    
    public void saveAutomationState() {
        IoUtil.writeJson(AUTOMATION_STATE_FILE, this.automationState);
    }
}
