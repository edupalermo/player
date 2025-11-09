package org.palermo.totalbattle.player;

import org.palermo.totalbattle.selenium.leadership.Point;
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
        ;
    }

    public void clearLock() {
        lock.clear();
    }

    public Set<String> getLock() {
        return lock;
    }

    public void setWait(Player player, Scenario scenario, LocalDateTime dateTime) {
        Map<Scenario, LocalDateTime> map = wait.computeIfAbsent(player.getName(), (k) -> new HashMap<>());
        map.put(scenario, dateTime);
    }

    public Optional<LocalDateTime> getWait(Player player, Scenario scenario) {
        return Optional.of(wait
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
}
