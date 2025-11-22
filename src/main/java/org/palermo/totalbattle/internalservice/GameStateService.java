package org.palermo.totalbattle.internalservice;

import org.palermo.totalbattle.player.state.AutomationState;
import org.palermo.totalbattle.player.state.location.Location;
import org.palermo.totalbattle.selenium.leadership.Point;

import java.util.List;
import java.util.Optional;

public class GameStateService extends AbstractService {

    public void add(Location location) {
        AutomationState automationState = getAutomationState();
        automationState.locations.add(location);
        saveGameState();
    }
    
    public <T> Optional<T> getLocation(Class<T> clazz) {
        AutomationState automationState = getAutomationState();
        List<Location> locations = automationState.getLocations();
        
        for (Location location : locations) {
            if (clazz.isInstance(location)) {
                return Optional.of(clazz.cast(location));
            }
        }
        return Optional.empty();
    }

    public void remove(Location location) {
        AutomationState automationState = getAutomationState();
        List<Location> locations = automationState.getLocations();

        int index = -1;
        for (int i = 0; i < locations.size(); i++) {
            Location it = locations.get(i);
            
            if (location.getPosition().equals(it.getPosition())) {
                index = i;
                break;
            }
        }
        
        if (index != -1) {
            locations.remove(index);
        }
        saveGameState();
    }

    public void removeLocationAt(Point point) {
        AutomationState automationState = getAutomationState();
        List<Location> locations = automationState.getLocations();

        int indexToBeRemoved = -1;
        for (int i = 0; i < locations.size(); i++) {
            Location it = locations.get(i);

            if (it.getPosition().equals(point)) {
                indexToBeRemoved = i;
                break;
            }
        }

        if (indexToBeRemoved != -1) {
            locations.remove(indexToBeRemoved);
        }
        saveGameState();
    }
}
