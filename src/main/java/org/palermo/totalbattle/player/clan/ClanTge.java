package org.palermo.totalbattle.player.clan;

import org.palermo.totalbattle.selenium.leadership.Point;

import java.util.ArrayList;
import java.util.List;

public class ClanTge {
    
    private static List<Point> polygon = new ArrayList<>();
    
    
    static {
        add(350, 498);
        add(371, 507);
        add(383, 519);
        add(385, 529);
        add(388, 534);
        add(403, 547);
        add(399, 569);
        add(338, 572);
        add(328, 542);
        add(307, 517);
        add(320, 490);
        add(335, 499);
    }
    
    private static void add(int x, int y) {
        polygon.add(Point.of(x, y));
    }

    public static boolean contains(Point p) {
        final double EPS = 1e-9;

        int n = polygon.size();
        if (n < 3) return false; // not a polygon

        // 1. Check if point is on any edge
        for (int i = 0; i < n; i++) {
            Point a = polygon.get(i);
            Point b = polygon.get((i + 1) % n);
            if (pointOnSegment(p, a, b, EPS)) {
                return true; // on boundary → inside according to your rule
            }
        }

        // 2. Ray casting
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = polygon.get(i);
            Point pj = polygon.get(j);

            // Check if edge (pj, pi) crosses the horizontal ray to the right from p
            boolean intersect = ((pi.getY() > p.getY()) != (pj.getY() > p.getY())) &&
                    (p.getX() < (pj.getX() - pi.getX()) * (p.getY() - pi.getY()) / (pj.getY() - pi.getY()) + pi.getX());

            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }

    private static boolean pointOnSegment(Point p, Point a, Point b, double eps) {
        double cross = (b.getX() - a.getX()) * (p.getY() - a.getY()) - (b.getY() - a.getY()) * (p.getX() - a.getX());
        if (Math.abs(cross) > eps) return false;

        double dot = (p.getX() - a.getX()) * (b.getX() - a.getX()) + (p.getY() - a.getY()) * (b.getY() - a.getY());
        if (dot < 0) return false;

        double lenSq = (b.getX() - a.getX()) * (b.getX() - a.getX()) + (b.getY() - a.getY()) * (b.getY() - a.getY());
        if (dot > lenSq) return false;

        return true;
    }    
    
    public static void main(String[] args) {
        System.out.println(ClanTge.contains(Point.of(381, 371)));
        System.out.println(ClanTge.contains(Point.of(313, 515)));
        System.out.println(ClanTge.contains(Point.of(324, 496)));
    }
}
