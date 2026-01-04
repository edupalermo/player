package org.palermo.totalbattle.repository;

import org.palermo.totalbattle.entity.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<PropertyEntity, String> {
    
    
}
