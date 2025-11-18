package org.palermo.totalbattle.player.bean;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonDeserialize(builder = ArmyBean.ArmyBeanBuilder.class)
public class ArmyBean {

    private final String playerName;
    private final int waves;
    private final int leadership;
    private final int dominance;
    private final int authority;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ArmyBeanBuilder {}
}
