package org.palermo.totalbattle.player;

import org.palermo.totalbattle.selenium.leadership.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum SharedData {
    
    INSTANCE;
    
    private final List<Point> arenas = new ArrayList<Point>();

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
}
