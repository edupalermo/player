package org.palermo.totalbattle.internalservice;

import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.player.bean.ArmyBean;
import org.palermo.totalbattle.player.bean.UnitQuantity;
import org.palermo.totalbattle.player.state.Army;
import org.palermo.totalbattle.player.state.ArmyTarget;
import org.palermo.totalbattle.player.state.AutomationState;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.player.state.TroopQuantity;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Pool;
import org.palermo.totalbattle.selenium.stacking.Unit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ArmyService extends AbstractService {

    private LockService lockService = new  LockService();

    private SharedData sharedData = SharedData.INSTANCE;

    private static final Comparator<TroopQuantity> UNIT_QUANTITY_COMPARATOR = (u1, u2) -> {
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
    
    
    public boolean shouldBuildArmy(Player player) {
        PlayerState playerState = getPlayerState(player);
        Army army = playerState.getArmy();
        
        if (army == null || army.getTarget() == null) {
            return false;
        }
        
        if (!lockService.isLocked(player, Scenario.BUILD_TROOPS_REEVALUATE)) {
            this.evaluateProductionOrder(player);
        }

        return army.getProductionOrder().size() > 0;
    }
    
    /**
     * Gets a shallow copy of the list 
     */
    public List<TroopQuantity> getProductionList(Player player) {
        AutomationState automationState = SharedData.INSTANCE.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates().get(player);
        Army army = playerState.getArmy();

        if (army == null) {
            return Collections.emptyList();
        }
        
        return new ArrayList<>(army.getProductionOrder());
    }

    public void setCurrentTroopQuantity(Player player, Unit unit, int quantity) {
        AutomationState automationState = SharedData.INSTANCE.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates().get(player);
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
        
        // Create a new List        
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
    
    public boolean shouldCheckTroopQuantities(Player player) {
        AutomationState automationState = SharedData.INSTANCE.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates().get(player);
        Army army = playerState.getArmy();

        if (army == null) {
            throw new RuntimeException("No army defined");
        }
        
        return !army.isCheckedExistingQuantity();
    }

    public void checkedTroopQuantities(Player player) {
        AutomationState automationState = SharedData.INSTANCE.getAutomationState();
        PlayerState playerState = automationState.getPlayerStates().get(player);
        Army army = playerState.getArmy();

        if (army == null) {
            throw new RuntimeException("No army defined");
        }

        army.setCheckedExistingQuantity(true);
        SharedData.INSTANCE.saveAutomationState();
    }
    
    public void setArmy(ArmyBean armyBean) {
        Player player = armyBean.getPlayer();

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
                .computeIfAbsent(player, (p) -> new PlayerState());

        if (playerState.getArmy() == null) {
            playerState.setArmy(new Army());
        }
        playerState.getArmy().setTarget(armyTarget);


        evaluateProductionOrder(player);

        sharedData.saveAutomationState();
    }
    
    private void evaluateProductionOrder(Player player) {

        PlayerState playerState = getPlayerState(player);
        ArmyTarget armyTarget = playerState.getArmy().getTarget();
        
        List<Unit> units = getUnits(player);

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

        unitQuantities = incrementLastLayer(unitQuantities, player);

        unitQuantities = addSpies(unitQuantities, player);

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

        lockService.lock(player, Scenario.BUILD_TROOPS_REEVALUATE,
                LocalDateTime.now().plusHours(1));
    }

    private List<UnitQuantity> addMiners(List<UnitQuantity> input) {
        List<UnitQuantity> output = new ArrayList<>();
        boolean found = false;
        for (UnitQuantity unitQuantity: input) {
            if (unitQuantity.getUnit() == Unit.G1_MELEE) {
                output.add(unitQuantity.withQuantity(unitQuantity.getQuantity() + 3500));
                found = true;
            }
            else {
                output.add(unitQuantity);
            }
        }

        if (!found) {
            output.add(UnitQuantity.builder()
                    .unit(Unit.G1_MELEE)
                    .quantity(3500).build());
        }
        return output;
    }

    private List<UnitQuantity> addSpies(List<UnitQuantity> input, Player player) {

        List<UnitQuantity> output = input;

        switch (player) {
            case PALERMO:
                output = increase(output, Unit.S4_SPY, 1000);
                output = increase(output, Unit.S3_SPY, 2000);
                break;
            case PETER, MIGHTSHAPER:
                output = increase(output, Unit.S3_SPY, 1000);
                output = increase(output, Unit.S2_SPY, 2000);
                break;
            case GRIRANA, ELANIN:
                output = increase(output, Unit.S2_SPY, 1000);
                output = increase(output, Unit.S1_SPY, 2000);
                break;

            default:
                throw new RuntimeException("Not Implemented");
        }

        return output;
    }


    private List<UnitQuantity> incrementLastLayer(List<UnitQuantity> input, Player player) {

        List<UnitQuantity> output = input;

        switch (player) {
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
            case GRIRANA, ELANIN:
                output = increase(output, Unit.G3_MOUNTED, 2000);
                output = increase(output, Unit.G3_RANGED, 4000);
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


    private List<Unit> getUnits(Player player) {

        List<Unit> units = new ArrayList<>();

        switch (player) {
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

            case PETER, MIGHTSHAPER:
                units.add(Unit.S2_SWORDSMAN);
                units.add(Unit.G2_RANGED);
                units.add(Unit.G2_MELEE);
                units.add(Unit.G2_MOUNTED);

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
            case GRIRANA, ELANIN:
                units.add(Unit.S1_SWORDSMAN);
                units.add(Unit.G1_RANGED);
                units.add(Unit.G1_MELEE);
                units.add(Unit.G1_MOUNTED);

                units.add(Unit.G2_RANGED);
                units.add(Unit.G2_MELEE);
                units.add(Unit.G2_MOUNTED);

                units.add(Unit.G3_RANGED);
                units.add(Unit.G3_MELEE);
                units.add(Unit.G3_MOUNTED);

                units.add(Unit.EMERALD_DRAGON);
                units.add(Unit.WATER_ELEMENTAL);
                units.add(Unit.STONE_GARGOYLE);
                units.add(Unit.BATTLE_BOAR);
                break;
            default:
                throw new RuntimeException("Not Implemented for " + player.getName());
        }
        return units;
    }
}
