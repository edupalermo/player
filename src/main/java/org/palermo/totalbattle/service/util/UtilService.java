package org.palermo.totalbattle.service.util;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.entity.PlayerEntity;
import org.palermo.totalbattle.entity.UnitEntity;
import org.palermo.totalbattle.player.Clan;
import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.repository.PlayerRepository;
import org.palermo.totalbattle.repository.UnitRepository;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.service.player.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UtilService {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private PlayerService playerService;

    public void initialize() {
        //createPalermo();
        createLorven();

        //PlayerEntity playerEntity = playerService.findPlayerToPlay();
        //playerService.finishPlaying(playerEntity);
    }

    private void createPalermo() {
        PlayerEntity playerEntity = new PlayerEntity();
        playerEntity.setPlayerName(PlayerName.PALERMO);
        playerEntity.setUsername("fp2268@gmail.com");
        playerEntity.setProfileFolder("chrome-profiles/palermo");
        playerEntity.setHasHelen(true);
        playerEntity.setCommonCryptLevel(25);
        playerEntity.setCommonTarRequired(65800);
        playerEntity.setCitadelLevel(20);
        playerEntity.setPriority(1);
        playerEntity.setPositionX(380);
        playerEntity.setPositionY(480);
        playerEntity.setKingdom(83);
        playerEntity.setClan(Clan.TWG);
        playerEntity.setPlaying(false);

        playerEntity = playerRepository.save(playerEntity);

        unitRepository.save(createUnitEntity(playerEntity, Unit.S3_SWORDSMAN, 0));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G3_RANGED, 1));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G3_MELEE, 2));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G3_MOUNTED, 3));

        unitRepository.save(createUnitEntity(playerEntity, Unit.S4_SWORDSMAN, 4));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G4_RANGED, 5));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G4_MELEE, 6));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G4_MOUNTED, 7));

        unitRepository.save(createUnitEntity(playerEntity, Unit.G5_RANGED, 8));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G5_MELEE, 9));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G5_MOUNTED, 10));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G5_GRIFFIN, 11));

        unitRepository.save(createUnitEntity(playerEntity, Unit.EMERALD_DRAGON, 12));
        unitRepository.save(createUnitEntity(playerEntity, Unit.WATER_ELEMENTAL, 13));
        unitRepository.save(createUnitEntity(playerEntity, Unit.STONE_GARGOYLE, 14));
        unitRepository.save(createUnitEntity(playerEntity, Unit.BATTLE_BOAR, 15));

        unitRepository.save(createUnitEntity(playerEntity, Unit.MAGIC_DRAGON, 16));
        unitRepository.save(createUnitEntity(playerEntity, Unit.ICE_PHOENIX, 17));
        unitRepository.save(createUnitEntity(playerEntity, Unit.MANY_ARMED_GUARDIAN, 18));
        unitRepository.save(createUnitEntity(playerEntity, Unit.GORGON_MEDUSA, 19));

        unitRepository.save(createUnitEntity(playerEntity, Unit.DESERT_VANQUISER, 20));
        unitRepository.save(createUnitEntity(playerEntity, Unit.FLAMING_CENTAUR, 21));
        unitRepository.save(createUnitEntity(playerEntity, Unit.ETTIN, 22));
        unitRepository.save(createUnitEntity(playerEntity, Unit.FEARSOME_MANTICORE, 23));
    }

    private void createLorven() {
        PlayerEntity playerEntity = new PlayerEntity();
        playerEntity.setPlayerName(PlayerName.LORVEN);
        playerEntity.setUsername("edupalermo+04@gmail.com");
        playerEntity.setProfileFolder("chrome-profiles/lorven");
        playerEntity.setHasHelen(false);
        playerEntity.setCommonCryptLevel(5);
        playerEntity.setCommonTarRequired(65800); //TODO Check
        playerEntity.setCitadelLevel(0);  //TODO Check
        playerEntity.setPriority(6);
        playerEntity.setPositionX(351);
        playerEntity.setPositionY(485);
        playerEntity.setKingdom(83);
        playerEntity.setClan(Clan.TWG_RESOURCES);
        playerEntity.setPlaying(false);

        playerEntity = playerRepository.save(playerEntity);

        unitRepository.save(createUnitEntity(playerEntity, Unit.S1_SWORDSMAN, 0));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G1_RANGED, 1));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G1_MELEE, 2));
        unitRepository.save(createUnitEntity(playerEntity, Unit.G1_MOUNTED, 3));
    }

    private UnitEntity createUnitEntity(PlayerEntity playerEntity, Unit unit, int order) {
        UnitEntity unitEntity = new UnitEntity();
        unitEntity.setPlayerEntity(playerEntity);
        unitEntity.setUnit(unit);
        unitEntity.setPriority(order);
        return unitEntity;
    }
    
    /*
    PALERMO(cfg("Palermo")
            .hasHelen(true)
            .commonCryptLevel(25)
            .commonTarRequired(65800)
            .citadelLevel(20)
            .profileFolder("chrome-profiles/palermo")
            .username("fp2268@gmail.com")
            .priority(1)
            .position(380, 480)
            .bestSiegeUnit(Unit.EC5_ENGINEER)),
    
    PETER(cfg("Peter")
            .hasHelen(false)
            .commonCryptLevel(20)
            .commonTarRequired(34000)
            .citadelLevel(15)
            .profileFolder("chrome-profiles/peter")
            .username("edupalermo@gmail.com")
            .priority(2)
            .position(381, 479)
            .bestSiegeUnit(Unit.EC4_ENGINEER)),

    MIGHTSHAPER(cfg("Mightshaper")
            .hasHelen(false)
            .commonCryptLevel(20)
            .commonTarRequired(40000)
            .citadelLevel(15)
            .profileFolder("chrome-profiles/mightshaper")
            .username("edupalermo+01@gmail.com")
            .priority(3)
            .position(379, 481)
            .bestSiegeUnit(Unit.EC4_ENGINEER)),

    GRIRANA(cfg("Grirana")
            .hasHelen(false)
            .commonCryptLevel(15)
            .commonTarRequired(13400)
            .citadelLevel(15)
            .profileFolder("chrome-profiles/grirana")
            .username("edupalermo+02@gmail.com")
            .priority(4)
            .position(381, 481)
            .bestSiegeUnit(Unit.EC3_ENGINEER)),

    ELANIN(cfg("Elanin")
            .hasHelen(false)
            .commonCryptLevel(10)
            .commonTarRequired(3800)
            .citadelLevel(10)
            .profileFolder("chrome-profiles/elanin")
            .username("edupalermo+03@gmail.com")
            .priority(5)
            .position(379, 479)
            .bestSiegeUnit(Unit.EC2_ENGINEER)),

    LORVEN(cfg("Lorven")
            .hasHelen(false)
            .commonCryptLevel(5)
            .citadelLevel(10)
            .profileFolder("chrome-profiles/lorven")
            .username("edupalermo+04@gmail.com")
            .priority(6)
            .position(351, 485)
            .bestSiegeUnit(Unit.EC1_ENGINEER));
     */

}
