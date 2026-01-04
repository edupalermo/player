package org.palermo.totalbattle.service.property;

import org.palermo.totalbattle.entity.PlayerEntity;
import org.palermo.totalbattle.entity.PlayerPropertyEntity;
import org.palermo.totalbattle.entity.PlayerPropertyEntityId;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.repository.PlayerPropertyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PlayerPropertyService {

    private PlayerPropertyRepository repository;
    private Converter converter;
    
    public PlayerPropertyService(PlayerPropertyRepository playerPropertyRepository, Converter converter) {
        this.repository = playerPropertyRepository;
        this.converter = converter;
    }

    public void set(PlayerEntity playerEntity, String propertyName, Object propertyValue) {
        PlayerPropertyEntityId playerPropertyEntityId = PlayerPropertyEntityId.builder()
                .playerId(playerEntity.getId())
                .name(propertyName)
                .build();

        PlayerPropertyEntity playerPropertyEntity = repository.findById(playerPropertyEntityId)
                .orElse(null);
        
        if (playerPropertyEntity == null) {
            playerPropertyEntity = new PlayerPropertyEntity();
            playerPropertyEntity.setId(playerPropertyEntityId);
            playerPropertyEntity.setValue(converter.toString(propertyValue));
        }
        else {
            playerPropertyEntity.setValue(converter.toString(propertyValue));
        }
        
        repository.save(playerPropertyEntity); 
    }
    
    public <T> Optional<T> get(PlayerEntity playerEntity, String propertyName, Class<T> type) {
        PlayerPropertyEntityId playerPropertyEntityId = PlayerPropertyEntityId.builder()
                .playerId(playerEntity.getId())
                .name(propertyName)
                .build();
        
        String rawValue = repository.findById(playerPropertyEntityId)
                .map(PlayerPropertyEntity::getValue)
                .orElse(null);

        return Optional
                .ofNullable(converter.to(rawValue, type));
    }
    
    public boolean isLocked(PlayerEntity playerEntity, Scenario scenario) {
        LocalDateTime value = get(playerEntity, scenario.name(),  LocalDateTime.class).orElse(null);
        
        if (value == null) {
            return false;
        }
        
        return value.isBefore(LocalDateTime.now());
    }

    public void lock(PlayerEntity playerEntity, Scenario scenario, LocalDateTime value) {
        set(playerEntity, scenario.name(), value);
    }
    
    public void clear(PlayerEntity playerEntity, Scenario scenario) {
        PlayerPropertyEntityId playerPropertyEntityId = PlayerPropertyEntityId.builder()
                .playerId(playerEntity.getId())
                .name(scenario.name())
                .build();
        repository.deleteById(playerPropertyEntityId);
    }
}
