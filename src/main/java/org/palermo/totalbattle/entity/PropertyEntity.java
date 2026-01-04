package org.palermo.totalbattle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PropertyEntity {

    @Id
    @Setter
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Setter
    @Column(name = "value", nullable = false, length = 255)
    private String value ;
}
