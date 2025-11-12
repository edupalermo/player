package org.palermo.totalbattle.player;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class Player {

    public static final String PALERMO = "Palermo";
    public static final String PETER_II = "Peter II";
    public static final String MIGHTSHAPER = "Mightshaper";
    public static final String GRIRANA = "Grirana";
    public static final String ELANIN = "Elanin";

    private String name;
    private String profileFolder;
    private String username;
    private String password;
}
