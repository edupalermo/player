package org.palermo.totalbattle.repository;

import org.palermo.totalbattle.entity.PlayerPropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public class PlayerPropertyRepository extends JpaRepository<PlayerPropertyEntity, Name> {
}
