package org.palermo.totalbattle.player.state;

import lombok.Getter;
import lombok.Setter;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.state.location.Crypt;
import org.palermo.totalbattle.player.state.location.Location;
import org.palermo.totalbattle.player.state.location.Mine;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.stacking.Captain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PlayerState {

    private int commonTar;
    private int silver;
    private Point exploringCrypt;
    
    private Army army = new Army();
    
    private Map<Scenario, LocalDateTime> locks = new HashMap<>();
    
    private List<Captain> captains = new ArrayList<>();
    
}
