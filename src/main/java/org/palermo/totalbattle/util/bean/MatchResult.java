package org.palermo.totalbattle.util.bean;

public record MatchResult(
        boolean found,
        double confidence,
        int x,
        int y
) {}