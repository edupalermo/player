package org.palermo.totalbattle.repository;

import org.palermo.totalbattle.entity.PlayerEntity;
import org.palermo.totalbattle.player.PlayerName;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    Optional<PlayerEntity> findByPlayerName(PlayerName playerName);

    @Query("""
    SELECT p
    FROM PlayerEntity p
    WHERE 
        (p.playing IS NULL OR p.playing = false)
    and
        (p.lock IS NULL or p.lock < :limit)
    ORDER BY p.lastLogout ASC
    """)
    List<PlayerEntity> findPlayerToPlay(@Param("limit") LocalDateTime limit, Pageable pageable);
    
    boolean existsByPlayerName(String playerName);
}
