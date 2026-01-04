package org.palermo.totalbattle.internalservice;

import org.palermo.totalbattle.entity.PlayerEntity;
import org.palermo.totalbattle.entity.UnitEntity;
import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.bean.ArmyBean;
import org.palermo.totalbattle.player.bean.UnitQuantity;
import org.palermo.totalbattle.player.state.Army;
import org.palermo.totalbattle.player.state.ArmyTarget;
import org.palermo.totalbattle.player.state.AutomationState;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.player.state.TroopQuantity;
import org.palermo.totalbattle.selenium.stacking.Attribute;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Pool;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.selenium.stacking.UnitType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ArmyService {

    private LockService lockService = new  LockService();

    private SharedData sharedData = SharedData.INSTANCE;

    private static final Comparator<TroopQuantity> UNIT_QUANTITY_COMPARATOR = (u1, u2) -> {
        if (u1.getUnit() == Unit.G1_MELEE || u2.getUnit() == Unit.G1_MELEE) { // Melee should come first to collect silver
            return u1.getUnit() == Unit.G1_MELEE ? -1 : 1;
        }
        if (u1.getUnit().getPool() != u2.getUnit().getPool()) { // LEADERSHIP should go first
            return u1.getUnit().getPool() == Pool.LEADERSHIP ? -1 : 1;
        }
        if (u1.getUnit().getTier() != u2.getUnit().getTier()) { // Higher tier should go first
            return u2.getUnit().getTier() - u1.getUnit().getTier();
        }
        int result = ((u2.getTarget() * u2.getUnit().getHeadCount()) // Troops with bit gap should go first
                - (u1.getTarget() * u1.getUnit().getHeadCount()));

        if (result != 0) {
            return result;
        }

        return u1.getUnit().name().compareToIgnoreCase(u2.getUnit().name()); // User anything...
    };
    
    /**
     * Gets a shallow copy of the list 
     */
    public List<TroopQuantity> getProductionList(PlayerName playerName) {
        AutomationState automationState = SharedData.INSTANCE.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates().get(playerName);
        Army army = playerState.getArmy();

        if (army == null) {
            return Collections.emptyList();
        }
        
        return new ArrayList<>(army.getProductionOrder());
    }

    public void setCurrentTroopQuantity(PlayerName playerName, Unit unit, int quantity) {
        AutomationState automationState = SharedData.INSTANCE.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates().get(playerName);
        Army army = playerState.getArmy();

        if (army == null) {
            throw new RuntimeException("No army defined");
        }

        // Update current troop quantity
        TroopQuantity troopQuantity = army.getProductionOrder()
                .stream()
                .filter((it) -> it.getUnit() == unit)
                .findAny()
                .orElse(null);
        
        if (troopQuantity != null) {
            troopQuantity.setCurrent(quantity);
        }
        
        // Create a new List - it will remove troops that don't need to train anything        
        List<TroopQuantity> newList = new ArrayList();
        for (TroopQuantity troopOrder: army.getProductionOrder()) {
            if (troopOrder.getCurrent() < troopOrder.getTarget()) {
                newList.add(troopOrder);
            }
        }
        newList.sort(UNIT_QUANTITY_COMPARATOR);
        army.setProductionOrder(newList);
        SharedData.INSTANCE.saveAutomationState();
    }
    
    public boolean shouldCheckTroopQuantities(PlayerName playerName) {
        AutomationState automationState = SharedData.INSTANCE.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates().get(playerName);
        Army army = playerState.getArmy();

        if (army == null) {
            throw new RuntimeException("No army defined");
        }
        
        return !army.isCheckedExistingQuantity();
    }

    public void checkedTroopQuantities(PlayerName playerName) {
        AutomationState automationState = SharedData.INSTANCE.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates().get(playerName);
        Army army = playerState.getArmy();

        if (army == null) {
            throw new RuntimeException("No army defined");
        }

        army.setCheckedExistingQuantity(true);
        SharedData.INSTANCE.saveAutomationState();
    }
    
    public void setArmy(ArmyBean armyBean) {
        PlayerName playerName = armyBean.getPlayerName();

        // Save Army Target
        ArmyTarget armyTarget = ArmyTarget.builder()
                .goal(armyBean.getGoal())
                .waves(armyBean.getWaves())
                .leadership(armyBean.getLeadership())
                .dominance(armyBean.getDominance())
                .authority(armyBean.getAuthority())
                .build();

        AutomationState automationState = sharedData.getAutomationState();
        PlayerState playerState = automationState
                .getPlayerStates()
                .computeIfAbsent(playerName, (p) -> new PlayerState());

        if (playerState.getArmy() == null) {
            playerState.setArmy(new Army());
        }
        playerState.getArmy().setTarget(armyTarget);


        setProductionOrder(playerName);

        sharedData.saveAutomationState();
    }
    
    public void setProductionOrder(PlayerEntity playerEntity) {

        PlayerState playerState = getPlayerState(playerName);
        ArmyTarget armyTarget = playerState.getArmy().getTarget();
        
        List<Unit> units = getUnits(playerName);

        ConfigurationBuilder builder = Configuration.builder()
                .leadership(armyTarget.getLeadership())
                .dominance(armyTarget.getDominance())
                .authority(armyTarget.getAuthority());

        for (Unit unit: units) {
            builder.addUnit(unit);
        }

        int[] qtds = builder.build().resolve();

        List<UnitQuantity> unitQuantities = new ArrayList();
        for (int i = 0; i < qtds.length; i++) {
            unitQuantities.add(UnitQuantity.builder()
                    .unit(units.get(i))
                    .quantity(Configuration.computeWaves(qtds[i], armyTarget.getWaves()))
                    .build());
        }

        unitQuantities = addMiners(unitQuantities);

        unitQuantities = incrementLastLayer(unitQuantities, playerName);

        unitQuantities = addSpies(unitQuantities, playerName);
        
        unitQuantities = prepareForCitadel(unitQuantities, playerName);

        Army army = playerState.getArmy();
        army.getProductionOrder().clear();
        army.setCheckedExistingQuantity(false);

        for (UnitQuantity unitQuantity: unitQuantities) {
            playerState
                .getArmy()
                .getProductionOrder()
                .add(TroopQuantity.builder()
                        .unit(unitQuantity.getUnit())
                        .target(unitQuantity.getQuantity())
                        .build());
        }

        playerState
            .getArmy()
            .getProductionOrder()
            .sort(UNIT_QUANTITY_COMPARATOR);

        lockService.lock(playerName, Scenario.BUILD_TROOPS_REEVALUATE,
                LocalDateTime.now().plusHours(1));
    }

    private List<UnitQuantity> addMiners(List<UnitQuantity> input) {
        List<UnitQuantity> output = new ArrayList<>();
        boolean found = false;
        for (UnitQuantity unitQuantity: input) {
            if (unitQuantity.getUnit() == Unit.G1_MELEE) {
                output.add(unitQuantity.withQuantity(unitQuantity.getQuantity() + 2000));
                found = true;
            }
            else {
                output.add(unitQuantity);
            }
        }

        if (!found) {
            output.add(UnitQuantity.builder()
                    .unit(Unit.G1_MELEE)
                    .quantity(2000).build());
        }
        return output;
    }

    private List<UnitQuantity> addSpies(List<UnitQuantity> input, PlayerName playerName) {

        List<UnitQuantity> output = input;

        switch (playerName) {
            case PALERMO:
                output = increase(output, Unit.S4_SPY, 1000);
                output = increase(output, Unit.S3_SPY, 2000);
                break;
            case PETER, MIGHTSHAPER:
                output = increase(output, Unit.S2_SPY, 1000);
                output = increase(output, Unit.S1_SPY, 2000);
                break;
            case GRIRANA, ELANIN:
                output = increase(output, Unit.S1_SPY, 1500);
                break;

            default:
                throw new RuntimeException("Not Implemented");
        }

        return output;
    }


    private List<UnitQuantity> prepareForCitadel(List<UnitQuantity> input, PlayerName playerName) {

        List<UnitQuantity> output = input;

        switch (playerName) {
            case PALERMO: // Should defeat Level 20 citadel
                output = increase(output, Unit.G3_MELEE, 1500);
                output = increase(output, Unit.G4_MELEE, 1000);
                output = increase(output, Unit.G5_MELEE, 500);
                output = increase(output, Unit.G5_GRIFFIN, 500);
                output = increase(output, Unit.EC5_ENGINEER, 25);
                break;
            case PETER, MIGHTSHAPER:  // Should defeat Level 15 citadel
                output = increase(output, Unit.G2_MOUNTED, 750);
                output = increase(output, Unit.G3_MOUNTED, 500);
                output = increase(output, Unit.G4_MOUNTED, 270);
                output = increase(output, Unit.EC4_ENGINEER, 167);
                break;
            case GRIRANA:  // Should defeat Level 15 citadel
                output = increase(output, Unit.G1_MOUNTED, 1500);
                output = increase(output, Unit.G2_MOUNTED, 1000);
                output = increase(output, Unit.G3_MOUNTED, 550);
                output = increase(output, Unit.EC3_ENGINEER, 400);
                break;
            case ELANIN:   // Should defeat Level 10 citadel
                output = increase(output, Unit.G2_RANGED, 1500);
                output = increase(output, Unit.G3_RANGED, 1000);
                output = increase(output, Unit.G4_RANGED, 500);
                output = increase(output, Unit.EC2_ENGINEER, 290);
                break;
            default:
                throw new RuntimeException("Not Implemented");
        }

        return output;
    }



    private List<UnitQuantity> incrementLastLayer(List<UnitQuantity> input, PlayerName playerName) {

        List<UnitQuantity> output = input;

        switch (playerName) {
            case PALERMO:
                output = increase(output, Unit.G5_MOUNTED, 4000);
                output = increase(output, Unit.G5_RANGED, 8000);
                output = increase(output, Unit.G5_MELEE, 8000);
                output = increase(output, Unit.G5_GRIFFIN, 400);
                break;
            case PETER, MIGHTSHAPER:
                output = increase(output, Unit.G4_MOUNTED, 4000);
                output = increase(output, Unit.G4_RANGED, 8000);
                output = increase(output, Unit.G4_MELEE, 8000);
                break;
            case GRIRANA:
                output = increase(output, Unit.G3_MOUNTED, 2000);
                output = increase(output, Unit.G4_RANGED, 2500);
                output = increase(output, Unit.G3_MELEE, 4000);
                break;
            case ELANIN:
                output = increase(output, Unit.G3_MOUNTED, 2000);
                output = increase(output, Unit.G4_RANGED, 2000);
                output = increase(output, Unit.G3_MELEE, 4000);
                break;

            default:
                throw new RuntimeException("Not Implemented");
        }

        return output;
    }

    private List<UnitQuantity> increase(List<UnitQuantity> input, Unit unit, int qtd) {
        List<UnitQuantity> output = new ArrayList<>();
        boolean found = false;
        for (UnitQuantity unitQuantity: input) {
            if (unitQuantity.getUnit() == unit) {
                output.add(unitQuantity.withQuantity(unitQuantity.getQuantity() + qtd));
                found = true;
            }
            else {
                output.add(unitQuantity);
            }
        }

        if (!found) {
            output.add(UnitQuantity.builder()
                    .unit(unit)
                    .quantity(qtd).build());
        }
        return output;

    }

    public List<Unit> removeWeakPointForCitadel(List<Unit> units, int level) {
        Set<Attribute> exclusions = getWeakPointsForMine(level);
        
        return units
                .stream()
                .filter(unit -> !unit.wasExcluded(exclusions))
                .toList();
    }
    
    private Set<Attribute> getWeakPointsForMine(int level) {
        HashSet<Attribute> exclusions = new HashSet<>();
        
        switch (level) {
            case 10:
                exclusions.add(Attribute.MOUNTED);
                exclusions.add(Attribute.MELEE);
                exclusions.add(Attribute.ELEMENTAL);
                exclusions.add(Attribute.DRAGON);
                return exclusions;
            case 15:
                exclusions.add(Attribute.RANGED);
                exclusions.add(Attribute.DRAGON);
                exclusions.add(Attribute.MELEE);
                return exclusions;
            default:
                throw new RuntimeException("Not implemented");
        }
    }
    private Unit getBestSiegeUnit(PlayerEntity playerEntity) {
        Unit best = null;
        
        for (UnitEntity unitEntity: playerEntity.getUnitEntities()) {
            Unit unit = unitEntity.getUnit();
            if (best == null || 
                    (unit.getType() == UnitType.CATAPULT && unit.getTier() > best.getTier())) {
                best = unit;
            }
        }
        return best;
    }

    public int getQtdSiegesForCitadel(PlayerEntity playerEntity) {
        switch (playerEntity.getCitadelLevel()) {
            case 10:
                switch (getBestSiegeUnit(playerEntity)) {
                    case EC2_ENGINEER:
                        return 290;
                    default:
                        throw new RuntimeException("Not implemented");
                }
            case 15:
                switch (getBestSiegeUnit(playerEntity)) {
                    case EC3_ENGINEER:
                        return 380;
                    case EC4_ENGINEER:
                        return 167;
                    default:
                        throw new RuntimeException("Not implemented");
                }
            default:
                throw new RuntimeException("Not implemented");
        }
    }


    public List<Unit> getUnits(PlayerEntity playerEntity) {

        List<Unit> units = new ArrayList<>();

        switch (playerEntity.getPlayerName()) {
            case PALERMO:
                units.add(Unit.S3_SWORDSMAN);
                units.add(Unit.G3_RANGED);
                units.add(Unit.G3_MELEE);
                units.add(Unit.G3_MOUNTED);

                units.add(Unit.S4_SWORDSMAN);
                units.add(Unit.G4_RANGED);
                units.add(Unit.G4_MELEE);
                units.add(Unit.G4_MOUNTED);

                units.add(Unit.G5_RANGED);
                units.add(Unit.G5_MELEE);
                units.add(Unit.G5_MOUNTED);
                units.add(Unit.G5_GRIFFIN);

                units.add(Unit.EMERALD_DRAGON);
                units.add(Unit.WATER_ELEMENTAL);
                units.add(Unit.STONE_GARGOYLE);
                units.add(Unit.BATTLE_BOAR);

                units.add(Unit.MAGIC_DRAGON);
                units.add(Unit.ICE_PHOENIX);
                units.add(Unit.MANY_ARMED_GUARDIAN);
                units.add(Unit.GORGON_MEDUSA);

                units.add(Unit.DESERT_VANQUISER);
                units.add(Unit.FLAMING_CENTAUR);
                units.add(Unit.ETTIN);
                units.add(Unit.FEARSOME_MANTICORE);
                break;

            case PETER:
                units.add(Unit.S2_SWORDSMAN);
                units.add(Unit.G2_RANGED);
                units.add(Unit.G2_MELEE);
                units.add(Unit.G2_MOUNTED);

                units.add(Unit.S3_SWORDSMAN);
                units.add(Unit.G3_RANGED);
                units.add(Unit.G3_MELEE);
                units.add(Unit.G3_MOUNTED);

                units.add(Unit.G4_RANGED);
                units.add(Unit.G4_MELEE);
                units.add(Unit.G4_MOUNTED);

                units.add(Unit.EMERALD_DRAGON);
                units.add(Unit.WATER_ELEMENTAL);
                units.add(Unit.STONE_GARGOYLE);
                units.add(Unit.BATTLE_BOAR);

                units.add(Unit.MAGIC_DRAGON);
                units.add(Unit.ICE_PHOENIX);
                units.add(Unit.MANY_ARMED_GUARDIAN);
                units.add(Unit.GORGON_MEDUSA);
                break;
            case MIGHTSHAPER:
                units.add(Unit.S2_SWORDSMAN);
                units.add(Unit.G2_RANGED);
                units.add(Unit.G2_MELEE);
                units.add(Unit.G2_MOUNTED);

                units.add(Unit.S3_SWORDSMAN);
                units.add(Unit.G3_RANGED);
                units.add(Unit.G3_MELEE);
                units.add(Unit.G3_MOUNTED);

                units.add(Unit.G4_RANGED);
                units.add(Unit.G4_MELEE);
                units.add(Unit.G4_MOUNTED);

                units.add(Unit.EMERALD_DRAGON);
                units.add(Unit.WATER_ELEMENTAL);
                units.add(Unit.STONE_GARGOYLE);
                units.add(Unit.BATTLE_BOAR);

                units.add(Unit.MAGIC_DRAGON);
                units.add(Unit.ICE_PHOENIX);
                units.add(Unit.MANY_ARMED_GUARDIAN);
                units.add(Unit.GORGON_MEDUSA);
                break;
            case GRIRANA:
                units.add(Unit.S1_SWORDSMAN);
                units.add(Unit.G1_MOUNTED);

                units.add(Unit.S2_SWORDSMAN);
                units.add(Unit.G2_RANGED);
                units.add(Unit.G2_MELEE);
                units.add(Unit.G2_MOUNTED);

                units.add(Unit.G3_RANGED);
                units.add(Unit.G3_MELEE);
                units.add(Unit.G3_MOUNTED);

                units.add(Unit.G4_RANGED);
                units.add(Unit.G4_MELEE);

                units.add(Unit.EMERALD_DRAGON);
                units.add(Unit.WATER_ELEMENTAL);
                units.add(Unit.STONE_GARGOYLE);
                units.add(Unit.BATTLE_BOAR);
                break;
            case ELANIN:
                units.add(Unit.S1_SWORDSMAN);
                units.add(Unit.G1_MELEE);
                units.add(Unit.G1_MOUNTED);

                units.add(Unit.S2_SWORDSMAN);
                units.add(Unit.G2_RANGED);
                units.add(Unit.G2_MELEE);
                units.add(Unit.G2_MOUNTED);

                units.add(Unit.G3_RANGED);
                units.add(Unit.G3_MELEE);
                units.add(Unit.G3_MOUNTED);

                units.add(Unit.G4_RANGED);

                units.add(Unit.EMERALD_DRAGON);
                units.add(Unit.WATER_ELEMENTAL);
                units.add(Unit.STONE_GARGOYLE);
                units.add(Unit.BATTLE_BOAR);
                break;
            default:
                throw new RuntimeException("Not Implemented for " + playerName.name());
        }
        return units;
    }
}
