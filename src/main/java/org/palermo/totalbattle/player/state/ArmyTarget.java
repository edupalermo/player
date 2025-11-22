package org.palermo.totalbattle.player.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArmyTarget {

    private String goal;
    private int waves;
    private int leadership;
    private int dominance;
    private int authority;
}
