package org.palermo.totalbattle.player.bean;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ArmyBean {

    private final String playerName;
    private final int waves;
    private final int leadership;
    private final int dominance;
    private final int authority;
}
