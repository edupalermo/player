package org.palermo.totalbattle.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NamedImage {

    private final long id;
    private final String name;
    private final List<int[][]> images;
}
