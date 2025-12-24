package org.palermo.totalbattle.service.resources;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResourcesBean {

    private final String playerName;
    private final int lumber;
    private final int iron;
    private final int stone;
    private final int silver;
}


