package org.palermo.totalbattle.selenium.leadership.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Exclusion {

    @Builder.Default
    private boolean ranged = false;
    @Builder.Default
    private boolean melee = false;
    @Builder.Default
    private boolean mounted = false;
    @Builder.Default
    private boolean dragon = false;
    @Builder.Default
    private boolean elemental = false;
    @Builder.Default
    private boolean giant = false;
    @Builder.Default
    private boolean beast = false;
    @Builder.Default
    private boolean flying = false;
    @Builder.Default
    private boolean siege = false;
}
