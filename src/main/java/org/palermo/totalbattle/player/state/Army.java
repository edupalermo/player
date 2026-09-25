package org.palermo.totalbattle.player.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Army {

    @Builder.Default
    private int productionReduction = 1;
    @Builder.Default
    private boolean checkedExistingQuantity = false;
    @Builder.Default
    private List<TroopQuantity> productionOrder = new ArrayList<>();
    private ArmyTarget target;
}

