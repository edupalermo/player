package org.palermo.totalbattle.selenium.leadership;

import lombok.Builder;

@Builder
public class Transformation {
    
    private Point real;
    private Point reference;

    public Point transform(int x, int y) {
        return Point.of(real, reference, Point.of(x, y));
    }

    public Point transform(Point point) {
        return Point.of(real, reference, point);
    }

    public Area transform(Point p1, Point p2) {
        return Area.of(real, reference, p1, p2);
    }

    public int transformX(int x) {
        return Point.of(real, reference, Point.of(x, 100)).getX();
    }
}
