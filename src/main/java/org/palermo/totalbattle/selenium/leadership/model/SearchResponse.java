package org.palermo.totalbattle.selenium.leadership.model;

import lombok.Builder;
import lombok.Getter;
import org.palermo.totalbattle.selenium.leadership.Point;

@Builder
@Getter
public class SearchResponse {
    
    private final Point point;
    private final Double difference;
}
