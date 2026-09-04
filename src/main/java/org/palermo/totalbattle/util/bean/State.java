package org.palermo.totalbattle.util.bean;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class State {

    private int defaultLeadership;
    private int citadel;
    private int crypt;
    private int gMeleeLevel;
    private int gRangedLevel;
    private int gMountedLevel;
    private int gFlyingLevel;
    private int spyLevel;
    private int sMeleeLevel;
    private int sRangedLevel;
    private int sMountedLevel;
    private int sFlyingLevel;
    private int monsterLevel;
    private int ecLevel;
    private int mercLevel;
    private String building;
}
