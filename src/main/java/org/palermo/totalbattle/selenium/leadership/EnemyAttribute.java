package org.palermo.totalbattle.selenium.leadership;

import lombok.Builder;
import lombok.Getter;
import org.palermo.totalbattle.selenium.stacking.Attribute;

import java.awt.image.BufferedImage;
import java.util.Set;

@Getter
@Builder
public class EnemyAttribute {
    
    private BufferedImage icon;
    private Set<Attribute> attributes;
}
