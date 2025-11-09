package org.palermo.totalbattle.service.wait;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class CooldownBean {
    
    private String playerName;
    private LocalDateTime cooldownTime;
    private String scenario;
}
