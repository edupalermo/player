package org.palermo.totalbattle.selenium.stacking;

import org.apache.commons.math3.linear.*;

import java.util.List;

public class Resolver {
    
    public static int resolveLast(List<UnitWrapper> units, int leadership) {
        
        if (units.size() == 1) {
            return leadership;
        }

        double[][] coefficients = new double[units.size()][];

        coefficients[0] = getArrayOfSumOfTroops(units); // Soma de todas as tropas deve dar o leadership
        for (int i = 1; i < units.size(); i++) {
            coefficients[i] = getRelationOfHealth(units, i);
        }

        // Right-hand side vector B
        double[] constants = new double[units.size()];
        constants[0] = leadership;
        for (int i = 1; i < units.size(); i++) {
            constants[i] = units.get(i - 1).getHealth();
            
            if (units.get(i).getHeadCount() < units.get(i - 1).getHeadCount()) {
                int times = (int) Math.round((double) units.get(i - 1).getHeadCount() / (double)units.get(i).getHeadCount());
                constants[i] += times * units.get(i).getHealth();
            }
        }

        RealMatrix a = MatrixUtils.createRealMatrix(coefficients);
        RealVector b = MatrixUtils.createRealVector(constants);

        DecompositionSolver solver = new LUDecomposition(a).getSolver();
        RealVector solution = solver.solve(b);
        
        return (int) solution.getEntry(units.size() - 1); 
    }

    private static double[] getArrayOfSumOfTroops(List<UnitWrapper> units) {
        double[] result = new double[units.toArray().length];
        for (int i = 0; i < units.size(); i++) {
            result[i] = units.get(i).getHeadCount();
        }
        return result;
    }

    //* starts with 1
    private static double[] getRelationOfHealth(List<UnitWrapper> units, int index) {
        double[] result = new double[units.toArray().length];
        // result[0] = units.get(0).getHealth();
        result[index - 1] = units.get(index - 1).getHealth();
        result[index] = -1 * units.get(index).getHealth();
        return result;
    }
}
