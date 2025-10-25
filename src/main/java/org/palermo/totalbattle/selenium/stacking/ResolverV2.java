package org.palermo.totalbattle.selenium.stacking;

import java.util.List;

public class ResolverV2 {

    public static int[] resolveLeadership(List<UnitWrapper> units, int leadership) {
        if (units == null || units.size() == 0) {
            throw new RuntimeException("Units cannot be null or empty");
        }

        if (units.size() == 1) {
            return new int[] {leadership / units.get(0).getHeadCount()};
        }

        int[] quantity = new int[units.size()];
        int sumLeaderships = 0;
        
        do {
            
            int index = units.size() - 1;
            boolean proceed = true;
            
            do {
                if (index == 0) {
                    if (sumLeaderships + units.get(index).getUnit().getHeadCount() <= leadership) {
                        sumLeaderships += units.get(index).getUnit().getHeadCount();
                        quantity[index]++;
                    }
                    proceed = false;
                }
                else {
                    if (previousIsEquivalent(units, index) && quantity[index - 1] * units.get(index - 1).getUnit().getHealth() >=
                            (quantity[index] + 1) * units.get(index).getUnit().getHealth()) {
                        sumLeaderships += units.get(index).getUnit().getHeadCount();
                        quantity[index]++;
                        proceed = false;
                    }
                    else if (quantity[index - 1] * units.get(index - 1).getUnit().getHealth() >
                            (quantity[index] + 1) * units.get(index).getUnit().getHealth()) {
                        sumLeaderships += units.get(index).getUnit().getHeadCount();
                        quantity[index]++;
                        proceed = false;
                    }
                    else {
                        index = index - 1;
                    }
                }
            } while (proceed);
            
            
        } while ((leadership - sumLeaderships) >= units.get(0).getUnit().getHeadCount());
        
        return quantity;
    }

    public static int[] resolveByPool(List<UnitWrapper> units, int headCountLimit, int healthLimit) {
        if (units == null || units.size() == 0) {
            return new int[0];
        }

        if (units.size() == 1) {
            int quantity = (healthLimit - 1) / units.get(0).getUnit().getHealth();
            if (quantity * units.get(0).getUnit().getHeadCount() > headCountLimit) {
                quantity = headCountLimit / units.get(0).getUnit().getHeadCount();
            }            
            return new int[] {quantity};
        }

        if (healthLimit < getLowestHealth(units)) { 
            return new int[units.size()];
        }

        int[] quantity = new int[units.size()];
        int sumHeadCound = 0;

        do {

            int index = getIndexToBeAdded(units, quantity, healthLimit);
            if (index == -1) {
                break;
            }
            
            sumHeadCound += units.get(index).getUnit().getHeadCount();
            quantity[index]++;
            
        } while ((headCountLimit - sumHeadCound) >= getLowestHeadCount(units) &&
                !addUnitWillViolateHealthLimit(units, quantity, healthLimit));

        return quantity;
    }
    
    private static int getIndexToBeAdded(List<UnitWrapper> units, int[] quantity, int healthLimit) {
        int best = -1;
        int lowestHealth = Integer.MAX_VALUE;
        
        
        for (int i = units.size() - 1; i >= 0; i--) {
            Unit unit = units.get(i).getUnit();
            int addedHealth = unit.getHealth() * (quantity[i] + 1);
            if (addedHealth <= healthLimit && lowestHealth >= addedHealth && !violatePreviousTier(units, i, quantity)) {
                best = i;
                lowestHealth = addedHealth;
            }
        }
        return best;
    }
    
    private static boolean violatePreviousTier(List<UnitWrapper> units, int index, int[] quantity) {
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getUnit().getTier() < units.get(index).getUnit().getTier()) {

                int lowerTierHealth = units.get(i).getUnit().getHealth() * quantity[i];                
                int currentUnitHealth = units.get(index).getUnit().getHealth() * (quantity[index] + 1);

                if (currentUnitHealth >= lowerTierHealth) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean addUnitWillViolateHealthLimit(List<UnitWrapper> units, int[] quantity, int healthLimit) {
        boolean violated = true;
        for (int i = 0; i < units.size(); i++) {
            Unit unit = units.get(i).getUnit();
            if (unit.getHealth() * (quantity[i] + 1) <= healthLimit) {
                return false;
            }
        }
        
        return violated;
    }
    
    private static int getLowestHealthSum(List<UnitWrapper> units, int[] quantity) {
        int lower = Integer.MAX_VALUE;
        
        for (int i = 0; i < units.size(); i++) {
            if (quantity[i] * units.get(i).getHealth() <= lower) {
                lower = quantity[i] * units.get(i).getHealth();
            }
        }
        return lower;
    }

    private static int getLowestHealth(List<UnitWrapper> units) {
        int lower = Integer.MAX_VALUE;

        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getHealth() <= lower) {
                lower = units.get(i).getUnit().getHealth();
            }
        }
        return lower;
    }
    
    private static int getLowestHeadCount(List<UnitWrapper> units) {
        int lower = Integer.MAX_VALUE;

        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getHealth() <= lower) {
                lower = units.get(i).getUnit().getHeadCount();
            }
        }
        return lower;
    }

    private static boolean previousIsEquivalent(List<UnitWrapper> units, int index) {
        return (units.get(index - 1).getUnit().getHeadCount() == units.get(index).getUnit().getHeadCount()) &&
                (units.get(index - 1).getUnit().getTier() == units.get(index).getUnit().getTier());
    }
}
