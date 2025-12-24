package org.palermo.totalbattle.player.task.shared;

import lombok.Getter;

@Getter
public enum Resource {
    LUMBER("player/top_menu/icon_lumber.png", "player/icon_lumber.png"),
    IRON("player/top_menu/icon_iron.png", "player/icon_iron.png"),
    STONE("player/top_menu/icon_stone.png", "player/icon_stone.png"),
    SILVER("player/top_menu/icon_silver.png", "player/icon_silver.png"),
    DRAGON_COIN("player/top_menu/icon_dragon_coin.png", "player/icon_dragon_coin.png"),
    COMMON_TAR("player/top_menu/icon_common_tar.png", "player/icon_common_tar.png");

    private String icon;
    private String resource;

    Resource(String icon, String resource) {
        this.icon = icon;
        this.resource = resource;
    }
}
