package org.palermo.totalbattle.repository;

import org.palermo.totalbattle.entity.PlayerPropertyEntity;
import org.palermo.totalbattle.entity.PlayerPropertyEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerPropertyRepository extends JpaRepository<PlayerPropertyEntity, PlayerPropertyEntityId> {
    
    
}
