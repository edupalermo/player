package org.palermo.totalbattle.player.state.location;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.palermo.totalbattle.selenium.leadership.Point;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@Getter @Setter
@SuperBuilder
@NoArgsConstructor
public abstract class Location {

    private Point position;
}

