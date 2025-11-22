package org.palermo.totalbattle.player.bean;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import org.palermo.totalbattle.player.Player;

@Builder
@Getter
@JsonDeserialize(builder = ArmyBean.ArmyBeanBuilder.class)
public class ArmyBean {

    private final String goal;
    private final Player player;
    private final int waves;
    private final int leadership;
    private final int dominance;
    private final int authority;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ArmyBeanBuilder {}
}
