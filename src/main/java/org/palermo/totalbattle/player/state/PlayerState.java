package org.palermo.totalbattle.player.state;

import lombok.Getter;
import lombok.Setter;
import org.palermo.totalbattle.player.Scenario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class PlayerState {
    
    private Army army = new Army();
    
    private Map<Scenario, LocalDateTime> locks = new HashMap<>();
}
