package org.palermo.totalbattle.selenium.leadership;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

import java.awt.*;

@Getter
@Builder
@JsonDeserialize(builder = Area.AreaBuilder.class)
public class Area {

    private final int x;
    private final int y;
    
    private final int width;
    private final int height;

    @JsonPOJOBuilder(withPrefix = "") // Lombok builder uses methods x(..), y(..) not setX(..)
    public static class AreaBuilder {}

    public static Area of(Point real, Point reference, Point start, Point finish) {
        return Area.builder()
                .x(real.getX() + start.getX() - reference.getX())
                .y(real.getY() + start.getY() - reference.getY())
                .width(Math.abs(finish.getX() - start.getX()))
                .height(Math.abs(finish.getY() - start.getY()))
                .build();
    }

    public static Area of(Point real, int moveX, int moveY, int width, int height) {
        return Area.builder()
                .x(real.getX() + moveX)
                .y(real.getY() + moveY)
                .width(width)
                .height(height)
                .build();
    }

    public static Area of(int x, int y, int width, int height) {
        return Area.builder()
                .x(x)
                .y(y)
                .width(width)
                .height(height)
                .build();
    }

    public static Area of(double x, double y, double width, double height) {
        return Area.of((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height));
    }

    public static Area fromTwoPoints(int x1, int y1, int x2, int y2) {
        return Area.builder()
                .x(Math.min(x1, x2))
                .y(Math.min(y1, y2))
                .width(Math.abs(x1 - x2))
                .height(Math.abs(y1 - y2))
                .build();
    }
    
    public static Area fromTwoPoints(Point p1, Point p2) {
        return fromTwoPoints(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    
    public boolean contain(Point point) {
        return this.x <= point.getX() 
                && this.y <= point.getY()
                && this.x + this.width > point.getX()
                && this.y + this.height > point.getY();
    }

    public Rectangle toRectangle() {
        return new Rectangle(x, y, width, height);
    }

    public boolean intersect(Area other) {
        return this.x < other.x + other.width &&
                this.x + this.width > other.x &&
                this.y < other.y + other.height &&
                this.y + this.height > other.y;
    }    
}
