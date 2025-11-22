package org.palermo.totalbattle.selenium.stacking;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Configuration {

    private final int leadership;
    private final int dominance;
    private final int authority;
    private final List<UnitWrapper> units;
    
    Configuration(int leadership, int dominance, int authority, List<UnitWrapper> units) {
        this.leadership = leadership;
        this.dominance = dominance;
        this.authority = authority;
        this.units = units;
    }
    
    
    public static ConfigurationBuilder builder() {
        return new ConfigurationBuilder();
    }

    public int[] resolve() {

        List<UnitWrapper> filteredUnits = new ArrayList<>();
        List<Integer> relations = new ArrayList<>();
        
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getUnit().getPool() == Pool.LEADERSHIP) {
                filteredUnits.add(units.get(i));
                relations.add(i);
            }
        }
        int[] troops = ResolverV2.resolveLeadership(filteredUnits, leadership);
        
        int[] answer = new int[units.size()];
        for (int i = 0; i < troops.length; i++) {
            answer[relations.get(i)] = troops[i];
        }

        System.out.println("Leadership: " + sumHeadCount(answer, Pool.LEADERSHIP) + "/" + leadership);
        System.out.println("=======================================");

        for (int i = 0; i < units.size(); i++) {
            Unit unit = units.get(i).getUnit();
            if (unit.getPool() == Pool.LEADERSHIP) {
                System.out.println(unit.name() + "\t" + answer[i] + "\t" + (units.get(i).getHealth() * answer[i]));
            }
        }
        
        // Handle Dominance

        filteredUnits = new ArrayList<>();
        relations = new ArrayList<>();

        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getUnit().getPool() == Pool.DOMINANCE) {
                filteredUnits.add(units.get(i));
                relations.add(i);
            }
        }

        troops = ResolverV2.resolveByPool(filteredUnits, dominance, getLowerHealth(answer, Pool.LEADERSHIP));
        for (int i = 0; i < troops.length; i++) {
            answer[relations.get(i)] = troops[i];
        }

        System.out.println("\nDominance: " + sumHeadCount(answer, Pool.DOMINANCE) + "/" + dominance);
        System.out.println("=======================================");

        for (int i = 0; i < units.size(); i++) {
            Unit unit = units.get(i).getUnit();
            if (unit.getPool() == Pool.DOMINANCE) {
                System.out.println(unit.name() + "\t" + answer[i] + "\t" + (units.get(i).getHealth() * answer[i]));
            }
        }
        
        // Handle Authority
        filteredUnits = new ArrayList<>();
        relations = new ArrayList<>();

        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getUnit().getPool() == Pool.AUTHORITY) {
                filteredUnits.add(units.get(i));
                relations.add(i);
            }
        }

        troops = ResolverV2.resolveByPool(filteredUnits, authority, getLowerHealth(answer, Pool.DOMINANCE));
        for (int i = 0; i < troops.length; i++) {
            answer[relations.get(i)] = troops[i];
        }

        System.out.println("\nAuthority: " + sumHeadCount(answer, Pool.AUTHORITY) + "/" + authority);
        System.out.println("=======================================");

        for (int i = 0; i < units.size(); i++) {
            Unit unit = units.get(i).getUnit();
            if (unit.getPool() == Pool.AUTHORITY) {
                System.out.println(unit.name() + "\t" + answer[i] + "\t" + (units.get(i).getHealth() * answer[i]));
            }
        }


        return answer;
    }

    public static int computeWaves(int quantity, int wave) {
        double factor = 0;

        for (int i = 0; i < wave; i++) {
            factor += Math.pow(1.06, i);
        }

        return (int) Math.round(quantity * factor);
    }


    public int getLowerHealth(int[] troops, Pool pool) {
        int lower = Integer.MAX_VALUE;
        for (int i = 0; i < troops.length; i++) {
            Unit unit = units.get(i).getUnit();
            
            if (unit.getPool() == pool) {
                int current = unit.getHealth() * troops[i];
                if (current < lower) {
                    lower = current;
                }
            }
            
        }
        return lower;
    }

    private int getWeakestInRound(List<DeployedUnit> units) {
        int round = getLowerRound(units);
        double lower = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < units.size(); i++) {
            DeployedUnit unit = units.get(i);
            if (unit.isAlive() && unit.getRound() == round) {
                double totalStrength = unit.getQuantity() * unit.getUnit().getStrength();
                if (lower > totalStrength) {
                    lower = totalStrength;
                    index = i;
                }
            }
        }
        return index;
    }


    private int getLowerRound(List<DeployedUnit> units) {
        int lower = Integer.MAX_VALUE;
        for (int i = 0; i < units.size(); i++) {
            DeployedUnit unit = units.get(i);
            if (unit.isAlive()) {
                if (lower > unit.getRound()) {
                    lower = unit.getRound();
                }
            }
        }
        return lower;
    }


    private int getHigherHealthAlive(List<DeployedUnit> units) {
        double higher = 0;
        int index = -1;
        for (int i = 0; i < units.size(); i++) {
            DeployedUnit unit = units.get(i);
            if (unit.isAlive()) {
                double totalHealth = unit.getUnit().getHealth() * unit.getQuantity();
               if (totalHealth > higher) {
                   higher = totalHealth;
                   index = i;
               }
            }
        }
        return index;
    }


    private boolean hasTroopAlive(List<DeployedUnit> units) {
        for (DeployedUnit unit: units) {
            if (unit.isAlive()) {
                return true;
            }
        }
        return false;
    }
    
    
    private int sumHeadCount(int qty[], Pool pool) {
        int total = 0;
        for (int i = 0; i < units.size(); i++) {
            Unit unit = units.get(i).getUnit();
            if (unit.getPool() == pool) {
                total += qty[i] * units.get(i).getHeadCount();
            }
        }
        return total;
    }
    
    private double[] getArrayOfSumOfTroops() {
        double[] result = new double[units.toArray().length];
        for (int i = 0; i < units.size(); i++) {
            result[i] = this.units.get(0).getHeadCount();
        }
        return result;
    }

    private double[] getRelationOfHealth(int index) {
        double[] result = new double[units.toArray().length];
        result[0] = this.units.get(0).getHealth();
        result[index] = -1 * this.units.get(index).getHealth();
        return result;
    }

    private int getTroopWithHighestHealth(List<Integer> troops, Set<Integer> removed) {
        double higherHealthValue = 0;
        int higherHealthIndex = -1;
        
        if (troops.size() == removed.size()) {
            return -1;
        }

        for (int i = 0; i < troops.size(); i++) {
            if (removed.contains(i)) {
                continue;
            }

            double totalHealth = troops.get(i) * units.get(i).getHealth();
            if (totalHealth >= higherHealthValue) {
                higherHealthValue = totalHealth;
                higherHealthIndex = i;
            }
        }
        return  higherHealthIndex;
    }
    
    private int getSmallestLeadership() {
        int smaller = Integer.MAX_VALUE;
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getUnit().getHeadCount() < smaller) {
                smaller = units.get(i).getUnit().getHeadCount();
            }
        }
        return smaller;
    }
}
