package org.palermo.totalbattle.internalservice;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.bean.UnitQuantity;
import org.palermo.totalbattle.player.state.PlayerState;
import org.palermo.totalbattle.player.state.TroopQuantity;
import org.palermo.totalbattle.selenium.stacking.Attribute;
import org.palermo.totalbattle.selenium.stacking.Configuration;
import org.palermo.totalbattle.selenium.stacking.ConfigurationBuilder;
import org.palermo.totalbattle.selenium.stacking.Pool;
import org.palermo.totalbattle.selenium.stacking.Unit;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.bean.Army;
import org.palermo.totalbattle.util.bean.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class ArmyService {

    private static final Comparator<UnitQuantity> UNIT_QUANTITY_COMPARATOR = (u1, u2) -> {
        if (u1.getUnit() == Unit.G1_MELEE || u2.getUnit() == Unit.G1_MELEE) { // Melee should come first to collect silver
            return u1.getUnit() == Unit.G1_MELEE ? -1 : 1;
        }
        if (u1.getUnit().getPool() != u2.getUnit().getPool()) { // LEADERSHIP should go first
            return u1.getUnit().getPool() == Pool.LEADERSHIP ? -1 : 1;
        }
        if (u1.getUnit().getTier() != u2.getUnit().getTier()) { // Higher tier should go first
            return u2.getUnit().getTier() - u1.getUnit().getTier();
        }
        return u1.getUnit().name()
                .compareToIgnoreCase(u2.getUnit().name()); // User anything...
    };
    
    //Working!!!
    public List<UnitQuantity> getProductionOrder(State state, Army army) {

        List<Unit> units = getUnits(state);

        ConfigurationBuilder builder = Configuration.builder()
                .leadership(army.getLeadership())
                .dominance(army.getDominance())
                .authority(1_000_000); // If aut

        for (Unit unit: units) {
            builder.addUnit(unit);
        }

        int[] qtds = builder.build().resolve();

        List<UnitQuantity> unitQuantities = new ArrayList();
        for (int i = 0; i < qtds.length; i++) {
            unitQuantities.add(UnitQuantity.builder()
                    .unit(units.get(i))
                    .quantity(Configuration.computeWaves(units.get(i), qtds[i], army.getWaves()))
                    .build());
        }

        unitQuantities.sort(UNIT_QUANTITY_COMPARATOR);
        return unitQuantities;
    }

    private void print(List<UnitQuantity> list, Unit unit) {
        long qtd = list.stream().filter(item -> item.getUnit() == unit)
                .findAny()
                .map(UnitQuantity::getQuantity)
                .orElse(0L);
        System.out.println(unit.name() + " - " + qtd);
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
    
    public List<Unit> getUnits(State state) {
        List<Unit> answer =  new ArrayList<>();
        
        if (state.getMonsterLevel() >= 3) {
            for (Unit unit: Unit.values()) {
                if (unit.getPool() == Pool.DOMINANCE && 
                        unit.getTier() <= state.getMonsterLevel() && 
                        unit.getTier() > state.getMonsterLevel() - 3) {
                    answer.add(unit);
                }
            }
        } 

        List<Integer> leadershipLevels = List.of(state.getGRangedLevel(), state.getGMeleeLevel(), state.getGMountedLevel(), state.getGFlyingLevel(),
                state.getSRangedLevel(), state.getSMeleeLevel(), state.getSMountedLevel(), state.getSFlyingLevel());
        int maxLevel = Collections.max(leadershipLevels);
        for (Unit unit: Unit.values()) {
            if (unit.getPool() == Pool.LEADERSHIP && unit.getTier() > maxLevel - 3) {

                int typeMaxLevel = -1;

                if (unit.getAttributes().contains(Attribute.GUARDSMAN) && unit.getAttributes().contains(Attribute.MELEE)) {
                    typeMaxLevel = state.getGMeleeLevel();
                }
                else if (unit.getAttributes().contains(Attribute.GUARDSMAN) && unit.getAttributes().contains(Attribute.RANGED)) {
                    typeMaxLevel = state.getGRangedLevel();
                }
                else if (unit.getAttributes().contains(Attribute.GUARDSMAN) && unit.getAttributes().contains(Attribute.MOUNTED)) {
                    typeMaxLevel = state.getGMountedLevel();
                }
                else if (unit.getAttributes().contains(Attribute.GUARDSMAN) && unit.getAttributes().contains(Attribute.FLYING)) {
                    typeMaxLevel = state.getGFlyingLevel();
                }
                else if (unit.getAttributes().contains(Attribute.SPECIALIST) && unit.getAttributes().contains(Attribute.MELEE)) {
                    typeMaxLevel = state.getSMeleeLevel();
                }
                else if (unit.getAttributes().contains(Attribute.SPECIALIST) && unit.getAttributes().contains(Attribute.RANGED)) {
                    typeMaxLevel = state.getSRangedLevel();
                }
                else if (unit.getAttributes().contains(Attribute.SPECIALIST) && unit.getAttributes().contains(Attribute.MOUNTED)) {
                    typeMaxLevel = state.getSMountedLevel();
                }
                else if (unit.getAttributes().contains(Attribute.SPECIALIST) && unit.getAttributes().contains(Attribute.FLYING)) {
                    typeMaxLevel = state.getSFlyingLevel();
                }
                
                if (typeMaxLevel > -1 && unit.getTier() <= typeMaxLevel) {
                    answer.add(unit);
                }
            }
        }

        return answer;
    }
}
