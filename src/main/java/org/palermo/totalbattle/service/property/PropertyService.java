package org.palermo.totalbattle.service.property;

import org.palermo.totalbattle.entity.PlayerEntity;
import org.palermo.totalbattle.entity.PlayerPropertyEntity;
import org.palermo.totalbattle.entity.PlayerPropertyEntityId;
import org.palermo.totalbattle.entity.PropertyEntity;
import org.palermo.totalbattle.repository.PlayerPropertyRepository;
import org.palermo.totalbattle.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PropertyService {

    private PropertyRepository repository;
    private Converter converter;

    public PropertyService(PropertyRepository playerPropertyRepository, Converter converter) {
        this.repository = playerPropertyRepository;
        this.converter = converter;
    }

    public void set(String propertyName, Object propertyValue) {
        PropertyEntity propertyEntity = repository.findById(propertyName)
                .orElse(null);

        if (propertyEntity == null) {
            propertyEntity = new PropertyEntity();
            propertyEntity.setName(propertyName);
        }
        propertyEntity.setValue(converter.toString(propertyValue));

        repository.save(propertyEntity);
    }

    public <T> Optional<T> get(String propertyName, Class<T> type) {
        String rawValue = repository.findById(propertyName)
                .map(PropertyEntity::getValue)
                .orElse(null);

        return Optional
                .ofNullable(converter.to(rawValue, type));
    }
}
