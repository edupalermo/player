package org.palermo.totalbattle.internalservice;

import lombok.extern.slf4j.Slf4j;
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
import org.palermo.totalbattle.selenium.stacking.Attribute;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Pool;
import org.palermo.totalbattle.selenium.stacking.Unit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ArmyService extends AbstractService {

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
    
    
    public boolean shouldBuildArmy(Player player) {
        PlayerState playerState = getPlayerState(player);
        Army army = playerState.getArmy();
        
        if (army == null || army.getTarget() == null) {
            log.info("Player has no army definition");
            return false;
        }
        
        if (!lockService.isLocked(player, Scenario.BUILD_TROOPS_REEVALUATE)) {
            this.setProductionOrder(player);
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


        setProductionOrder(player);

        sharedData.saveAutomationState();
    }
    
    public void setProductionOrder(Player player) {

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
                    .quantity(Configuration.computeWaves(units.get(i), qtds[i], armyTarget.getWaves()))
                    .build());
        }

        unitQuantities = addMiners(unitQuantities);

        unitQuantities = incrementLastGuardsmanLayer(unitQuantities, player);

        unitQuantities = addSpies(unitQuantities, player);

        unitQuantities = prepareForCitadel(unitQuantities, player);

        unitQuantities = prepareForPvP(unitQuantities, player);
        
        unitQuantities = prepareQuickReplacementAfterUpgrade(unitQuantities, player);

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

    private List<UnitQuantity> addSpies(List<UnitQuantity> input, Player player) {
        List<UnitQuantity> output = input;

        switch (player) {
            case PALERMO:
                output = increase(output, Unit.S6_SPY, 1000);
                output = increase(output, Unit.S5_SPY, 2000);
                break;
            case PETER, MIGHTSHAPER:
                output = increase(output, Unit.S3_SPY, 1000);
                output = increase(output, Unit.S2_SPY, 2000);
                break;
            case GRIRANA, ELANIN:
                output = increase(output, Unit.S1_SPY, 1500);
                break;
            case LORVEN:
                output = increase(output, Unit.S1_SPY, 100);
                break;
            default:
                throw new RuntimeException("Not Implemented");
        }

        return output;
    }


    private List<UnitQuantity> prepareForCitadel(List<UnitQuantity> input, Player player) {

        List<UnitQuantity> output = input;

        switch (player) {
            case PALERMO: // Should defeat Level 20 citadel
                output = topUp(output, Unit.G5_MELEE, 1500);
                output = topUp(output, Unit.G6_MELEE, 1000);
                output = topUp(output, Unit.S6_MELEE, 1000);
                output = topUp(output, Unit.G7_MELEE, 500);
                output = topUp(output, Unit.G7_GRIFFIN, 500);
                output = topUp(output, Unit.EC7_ENGINEER, 3000);
                break;
            case PETER:  // Should defeat Level 15 citadel
                output = increase(output, Unit.G3_MOUNTED, 750);
                output = increase(output, Unit.G4_MOUNTED, 500);
                output = increase(output, Unit.G5_MOUNTED, 270);
                output = topUp(output, Unit.EC5_ENGINEER, 167);
                break;
            case MIGHTSHAPER:  // Should defeat Level 15 citadel
                output = increase(output, Unit.G3_MOUNTED, 750);
                output = increase(output, Unit.G4_MOUNTED, 500);
                output = increase(output, Unit.G5_MOUNTED, 270);
                output = topUp(output, Unit.EC5_ENGINEER, 167);
                break;
            case GRIRANA:  // Should defeat Level 15 citadel
                output = increase(output, Unit.G2_MOUNTED, 1500);
                output = increase(output, Unit.G3_MOUNTED, 1000);
                output = increase(output, Unit.G4_MOUNTED, 550);
                output = topUp(output, Unit.EC4_ENGINEER, 400);
                break;
            case ELANIN:   // Should defeat Level 10 citadel
                output = increase(output, Unit.G2_RANGED, 1500);
                output = increase(output, Unit.G3_RANGED, 1000);
                output = increase(output, Unit.G4_RANGED, 550);
                output = topUp(output, Unit.EC4_ENGINEER, 400);
                break;
            case LORVEN:
                break;
            default:
                throw new RuntimeException("Not Implemented");
        }

        return output;
    }

    private List<UnitQuantity> prepareQuickReplacementAfterUpgrade(List<UnitQuantity> input, Player player) {
        List<UnitQuantity> output = input;

        switch (player) {
            case PALERMO:
                output = topUp(output, Unit.G4_MOUNTED, 1500);
                break;
            case PETER, MIGHTSHAPER:
                output = topUp(output, Unit.G2_MOUNTED, 1000);
                break;
            case GRIRANA:
                break;
            case ELANIN:
                break;
            case LORVEN:
                break;
            default:
                throw new RuntimeException("Not Implemented");
        }
        return output;
    }

    private List<UnitQuantity> prepareForPvP(List<UnitQuantity> input, Player player) {

        List<UnitQuantity> output = input;

        switch (player) {
            case PALERMO:
                output = topUp(output, Unit.EC7_ENGINEER, 1000);
                break;
            case PETER:  
                output = topUp(output, Unit.EC5_ENGINEER, 1000);
                break;
            case MIGHTSHAPER:
                output = topUp(output, Unit.EC5_ENGINEER, 1000);
                break;
            case GRIRANA:
                break;
            case ELANIN:
                break;
            case LORVEN:
                break;
            default:
                throw new RuntimeException("Not Implemented");
        }

        return output;
    }

    private int halfOfLeaderShip(int leadership, Unit unit) {
        return (int) Math.round(((double) leadership / 2d * (double) unit.getHeadCount()) );
    }

    private List<UnitQuantity> incrementLastGuardsmanLayer(List<UnitQuantity> input, Player player) {

        List<UnitQuantity> output = input;

        int aydaeLeadership;

        switch (player) {
            case PALERMO:
                aydaeLeadership = 72000;
                output = topUp(output, Unit.G8_RANGED, halfOfLeaderShip(aydaeLeadership, Unit.G8_RANGED));
                output = topUp(output, Unit.G7_MELEE, halfOfLeaderShip(aydaeLeadership, Unit.G7_MELEE));
                output = topUp(output, Unit.G7_MOUNTED, halfOfLeaderShip(aydaeLeadership, Unit.G7_MOUNTED));
                output = topUp(output, Unit.G7_GRIFFIN, halfOfLeaderShip(aydaeLeadership, Unit.G7_GRIFFIN));
                break;
            case PETER:
                output = increase(output, Unit.G5_RANGED, 8000);
                output = increase(output, Unit.G5_MELEE, 8000);
                output = increase(output, Unit.G5_MOUNTED, 4000);
                output = increase(output, Unit.G5_GRIFFIN, 400);
                break;
            case MIGHTSHAPER:
                output = increase(output, Unit.G5_RANGED, 8000);
                output = increase(output, Unit.G5_MELEE, 8000);
                output = increase(output, Unit.G5_MOUNTED, 4000);
                output = increase(output, Unit.G5_GRIFFIN, 400);
                break;
            case GRIRANA, ELANIN:
                output = increase(output, Unit.G4_MOUNTED, 2000);
                output = increase(output, Unit.G4_RANGED, 2000);
                output = increase(output, Unit.G4_MELEE, 4000);
                break;
            case LORVEN:
                break;
            default:
                throw new RuntimeException("Not Implemented");
        }

        return output;
    }

    private List<UnitQuantity> topUp(List<UnitQuantity> input, Unit unit, int qtd) {
        List<UnitQuantity> output = new ArrayList<>();
        boolean found = false;
        for (UnitQuantity unitQuantity: input) {
            if (unitQuantity.getUnit() == unit) {
                output.add(unitQuantity.withQuantity(Math.max(unitQuantity.getQuantity(), qtd)));
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

    public int getQtdSiegesForCitadel(Player player, int level) {
        switch (level) {
            case 10:
                switch (player.getBestSiegeUnit()) {
                    case EC2_ENGINEER:
                        return 290;
                    default:
                        throw new RuntimeException("Not implemented");
                }
            case 15:
                switch (player.getBestSiegeUnit()) {
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


    public List<Unit> getUnits(Player player) {

        List<Unit> units = new ArrayList<>();

        switch (player) {
            case PALERMO:
                units.add(Unit.S5_SWORDSMAN);
                units.add(Unit.S5_VULTURE);
                units.add(Unit.S5_LION_RIDER);
                
                units.add(Unit.G5_MELEE);
                units.add(Unit.G5_MOUNTED);
                units.add(Unit.G5_GRIFFIN);

                units.add(Unit.S6_RANGED);
                units.add(Unit.S6_MELEE);
                units.add(Unit.S6_FLYING);
                units.add(Unit.S6_MOUNTED);
                
                units.add(Unit.G6_RANGED);
                units.add(Unit.G6_MELEE);
                units.add(Unit.G6_MOUNTED);
                units.add(Unit.G6_GRIFFIN);

                units.add(Unit.G7_RANGED);
                units.add(Unit.G7_MELEE);
                units.add(Unit.G7_MOUNTED);
                units.add(Unit.G7_GRIFFIN);

                units.add(Unit.G8_RANGED);

                units.add(Unit.DRAGON_V);
                units.add(Unit.ELEMENTAL_V);
                units.add(Unit.GIANT_V);
                units.add(Unit.BEAST_V);

                units.add(Unit.DRAGON_VI);
                units.add(Unit.ELEMENTAL_VI);
                units.add(Unit.GIANT_VI);
                units.add(Unit.BEAST_VI);

                units.add(Unit.DRAGON_VII);
                units.add(Unit.ELEMENTAL_VII);
                units.add(Unit.GIANT_VII);
                units.add(Unit.BEAST_VII);
                break;

            case PETER:
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

                units.add(Unit.DRAGON_III);
                units.add(Unit.ELEMENTAL_III);
                units.add(Unit.GIANT_III);
                units.add(Unit.BEAST_III);

                units.add(Unit.DRAGON_IV);
                units.add(Unit.ELEMENTAL_IV);
                units.add(Unit.GIANT_IV);
                units.add(Unit.BEAST_IV);

                units.add(Unit.DRAGON_V);
                units.add(Unit.ELEMENTAL_V);
                units.add(Unit.GIANT_V);
                units.add(Unit.BEAST_V);
                break;
            case MIGHTSHAPER:
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

                units.add(Unit.DRAGON_III);
                units.add(Unit.ELEMENTAL_III);
                units.add(Unit.GIANT_III);
                units.add(Unit.BEAST_III);

                units.add(Unit.DRAGON_IV);
                units.add(Unit.ELEMENTAL_IV);
                units.add(Unit.GIANT_IV);
                units.add(Unit.BEAST_IV);

                units.add(Unit.DRAGON_V);
                units.add(Unit.ELEMENTAL_V);
                units.add(Unit.GIANT_V);
                units.add(Unit.BEAST_V);
                break;
            case GRIRANA:
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
                
                units.add(Unit.DRAGON_III);
                units.add(Unit.ELEMENTAL_III);
                units.add(Unit.GIANT_III);
                units.add(Unit.BEAST_III);

                units.add(Unit.DRAGON_IV);
                units.add(Unit.ELEMENTAL_IV);
                units.add(Unit.GIANT_IV);
                units.add(Unit.BEAST_IV);
                break;
            case ELANIN:
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

                units.add(Unit.DRAGON_III);
                units.add(Unit.ELEMENTAL_III);
                units.add(Unit.GIANT_III);
                units.add(Unit.BEAST_III);

                units.add(Unit.DRAGON_IV);
                units.add(Unit.ELEMENTAL_IV);
                units.add(Unit.GIANT_IV);
                units.add(Unit.BEAST_IV);
                break;
            case LORVEN:
                units.add(Unit.S1_SWORDSMAN);
                units.add(Unit.G1_RANGED);
                units.add(Unit.G1_MELEE);
                units.add(Unit.G1_MOUNTED);
                break;

            default:
                throw new RuntimeException("Not Implemented for " + player.getName());
        }
        return units;
    }
}
