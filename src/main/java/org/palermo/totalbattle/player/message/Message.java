package org.palermo.totalbattle.player.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        property = "type"
)
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class Message {

    public LocalDateTime expirationDate;
}
