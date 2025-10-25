package org.palermo.totalbattle.selenium.leadership;

import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.Serializable;

@Getter
public class Point implements Serializable {

    private final int x;
    private final int y;
    
    private Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Point of(int x, int y) {
        return new Point(x, y);
    }
    
    public static Point of(long x, long y) {
        return new Point((int) x, (int) y);
    }

    public static Point of(Point real, Point reference, Point relative) {
        return new Point(real.getX() + relative.getX() - reference.getX(), 
                real.getY() + relative.getY() - reference.getY());
    }

    public Point move(int dx, int dy) {
        return Point.of(this.x + dx, this.y + dy);
    }
    
    public Point toTheMiddleOf(BufferedImage image) {
        return Point.of(this.x + Math.round(image.getWidth() / 2d), this.y +  + Math.round(image.getHeight() / 2d));
    }

    @Override
    public String toString() {
        return "Point{ x=" + x + ", y=" + y + " }";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }
}
