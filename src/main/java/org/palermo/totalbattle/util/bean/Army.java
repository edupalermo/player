package org.palermo.totalbattle.util.bean;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Army {

    private int leadership;
    private int dominance;
    private int waves;
}
