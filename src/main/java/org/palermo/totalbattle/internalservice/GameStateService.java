package org.palermo.totalbattle.internalservice;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.message.Message;
import org.palermo.totalbattle.player.message.SilverRequest;
import org.palermo.totalbattle.player.state.AutomationState;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.player.state.location.Location;
import org.palermo.totalbattle.player.state.location.Mine;
import org.palermo.totalbattle.player.state.location.MineType;
import org.palermo.totalbattle.selenium.leadership.Point;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GameStateService extends AbstractService {

    public static final String PROPERTY_NEXT = "NEXT";
    public static final String PROPERTY_MANUAL_OCR = "MANUAL_OCR";

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

    public Optional<Mine> getMine(MineType type) {
        AutomationState automationState = getAutomationState();
        List<Location> locations = automationState.getLocations();

        return locations.stream().filter(Mine.class::isInstance)
                .map(Mine.class::cast)
                .filter((mine) -> mine.getType() == type)
                .findFirst();
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
    
    public int countMines(MineType type) {
        AutomationState automationState = getAutomationState();
        List<Location> locations = automationState.getLocations();
        
        return (int) locations.stream()
                .filter(Mine.class::isInstance)
                .map(Mine.class::cast)
                .filter((loc) -> loc.getType() == MineType.SILVER)
                .count();
    }

    public String getProperty(String key) {
        return getAutomationState().getProperties().get(key);
    }
    
    public boolean getPropertyAsBoolean(String key) {
        String value = getAutomationState().getProperties().get(key);
        if (value == null) {
            return false;
        }
        return value.equals("1") || value.equalsIgnoreCase("true");
    }

    public String removeProperty(String key) {
        return getAutomationState().getProperties().remove(key);
    }
    
    
    private void excludeExpiredMessages() {
        getAutomationState()
                .getMessages()
                .removeIf(message -> LocalDateTime.now().isAfter(message.getExpirationDate()));    
    }
    
    public Optional<SilverRequest> shouldDonateSilver(Player player) {
        excludeExpiredMessages();
        
        Set<Message> messages = getAutomationState().getMessages();
        SilverRequest silverRequest = null;
        
        // Get Silver request with the highest priority
        for (Message message: messages) {
            if (message instanceof SilverRequest) {
                if (silverRequest == null || 
                        ((SilverRequest) message).getTarget().getPriority() < silverRequest.getTarget().getPriority()) {
                    silverRequest = (SilverRequest) message;
                }
            }
        }
        if (silverRequest == null) {
            return Optional.empty();
        }

        Player richerPlayer = null;
        for (Map.Entry<Player, PlayerState> entry: getAutomationState().getPlayerStates().entrySet()) {
            if (richerPlayer == null) {
                if (entry.getKey().getPriority() > silverRequest.getTarget().getPriority() &&
                        entry.getValue().getSilver() > 2_000_000) {
                    richerPlayer = entry.getKey();
                }
            } else if (entry.getKey().getPriority() > silverRequest.getTarget().getPriority() &&
                    entry.getValue().getSilver() > 2_000_000 &&
                    entry.getValue().getSilver() > getPlayerState(richerPlayer).getSilver()) {
                richerPlayer = entry.getKey();
            }
        }
        
        if (richerPlayer == player) {
            return Optional.of(silverRequest);
        }
        else {
            return Optional.empty();
        }
    }
    
    public void remove(Message message) {
        getAutomationState().getMessages().remove(message);
    }
    
    public void publishMessage(Message message) {
        getAutomationState().getMessages().add(message);
    }
}
