package com.r3m.spaceshooter.system;

import java.awt.Shape;
import java.awt.geom.Area;

/** Performs geometric collision tests between arbitrary shapes. */
public class CollisionManager {

    /** Creates a collision manager. */
    public CollisionManager() {
    }

    /**
     * Tests whether two shapes have a non-empty intersection.
     *
     * @param a first shape
     * @param b second shape
     * @return {@code true} when the shapes overlap
     */
    public boolean isColliding(Shape a, Shape b) {
        Area intersection = new Area(a);
        intersection.intersect(new Area(b));

        return !intersection.isEmpty();
    }
}
