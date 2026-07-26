package com.r3m.spaceshooter.system;

import java.awt.Rectangle;

public class CollisionManager {
    public boolean isColliding(Rectangle a, Rectangle b) {
        return a.intersects(b);
    }
}