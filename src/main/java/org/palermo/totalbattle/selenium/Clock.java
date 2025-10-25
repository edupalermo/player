package org.palermo.totalbattle.selenium;

public class Clock {
    
    private final long start = System.currentTimeMillis();

    private static final double MINUTE = 60 * 1000;    
    private static final double SECOND = 1000;


    private Clock() {
        
    }
    
    public static Clock start() {
        return new Clock();
    }
    
    public String elapsedTime() {
        double difference = System.currentTimeMillis() - start;
        if (difference >= MINUTE) {
            return String.format("%.2f min", ((double) difference) / MINUTE);
        }
        else if (difference >= SECOND) {
            return String.format("%.2f sec", ((double) difference) / SECOND);
        }
        else {
            return String.format("%.0f ms", difference);
        }
    }
    
}
