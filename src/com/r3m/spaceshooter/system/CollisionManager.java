package com.r3m.spaceshooter.system;

import java.awt.Shape;
import java.awt.geom.Area;

public class CollisionManager {

    public boolean isColliding(Shape a, Shape b) {
        Area intersection = new Area(a);
        intersection.intersect(new Area(b));

        return !intersection.isEmpty();
    }
}