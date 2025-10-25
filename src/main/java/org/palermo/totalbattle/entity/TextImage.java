package org.palermo.totalbattle.entity;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TextImage {

    private final long id;
    private final String text;
    private final int[][] image;
}
