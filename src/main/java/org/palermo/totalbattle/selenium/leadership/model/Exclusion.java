package org.palermo.totalbattle.selenium.leadership.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Exclusion {
    
    private boolean ranged = false;
    private boolean melee = false;
    private boolean mounted = false;
    private boolean dragon = false;
    private boolean elemental = false;
    private boolean giant = false;
    private boolean beast = false;
    private boolean flying = false;
    private boolean siege = false;
}
