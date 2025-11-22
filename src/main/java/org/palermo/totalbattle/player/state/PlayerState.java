package org.palermo.totalbattle.player.state;

import lombok.Getter;
import lombok.Setter;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.selenium.stacking.Captain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PlayerState {
    
    private Army army = new Army();
    
    private Map<Scenario, LocalDateTime> locks = new HashMap<>();
    
    private List<Captain> captains = new ArrayList<>();
}
