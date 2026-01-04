package org.palermo.totalbattle.service.player;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.entity.PlayerEntity;
import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.repository.PlayerRepository;
import org.palermo.totalbattle.repository.UnitRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service @Slf4j
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final UnitRepository unitRepository;

    public PlayerService(PlayerRepository playerRepository,
                         UnitRepository unitRepository) {
        this.playerRepository = playerRepository;
        this.unitRepository = unitRepository;
    }
    
    @Transactional
    public PlayerEntity findFreePlayerToPlay() {
        PlayerEntity playerEntity = null;
        
        while (playerEntity == null) {
            playerEntity = playerRepository.findPlayerToPlay(PageRequest.of(0, 1))
                    .stream().findFirst().orElse(null);
            
            if (playerEntity == null) {
                log.info("No player available! Waiting 5 seconds...");
                SharedData.INSTANCE.robot.sleep(5000);
                continue;
            }
            playerEntity.setPlaying(true);
            playerRepository.save(playerEntity);
        }
        
        return playerEntity;
    }

    @Transactional
    public void finishPlaying(PlayerEntity playerEntity) {
        playerEntity.setPlaying(false);
        playerEntity.setLastLogout(LocalDateTime.now());
        playerRepository.save(playerEntity);
    }
    
    public Optional<PlayerEntity> findByPlayerName(PlayerName playerName) {
        return playerRepository.findByPlayerName(playerName);
    }
    
    public List<PlayerEntity> findAll() {
        return playerRepository.findAll();
    }
    
    public PlayerEntity update(PlayerEntity playerEntity) {
        if (playerEntity.getId() == null) {
            throw new RuntimeException("We should not create new players here!");
        }
        return playerRepository.save(playerEntity);
    }
}
