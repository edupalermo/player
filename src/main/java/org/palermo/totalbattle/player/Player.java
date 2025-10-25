package org.palermo.totalbattle.player;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class Player {
    
    private String name;
    private String profileFolder;
    private String username;
    private String password;
}
