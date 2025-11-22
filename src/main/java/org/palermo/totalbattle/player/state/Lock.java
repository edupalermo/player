package org.palermo.totalbattle.player.state;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.palermo.totalbattle.player.Scenario;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class Lock {
    
    private Scenario scenario;
    private LocalDateTime until;
}
